/*
 * Copyright (C) 2012 Li Cong, forlong401@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zapple.evshare.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.zapple.evshare.R;
import com.zapple.evshare.data.GetCitiesResult;
import com.zapple.evshare.data.NewFragmentInfo;
import com.zapple.evshare.data.Store;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.support.v4.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class ReservationServiceFragment extends Fragment {
    private static final String TAG = ReservationServiceFragment.class.getSimpleName();
    private static final boolean DEBUG = true;	
    private final static long MINUTES_MAX_30 = 30*60*1000;
	private static final int QUERY_SUCCESS = 2;
	private static final int QUERY_FAILURE = 3;
	
    // date and time
    private int mTakeYear;
    private int mTakeMonth;
    private int mTakeDay;
    private int mTakeHour;
    private int mTakeMinute;
    private Callback mCallback = EmptyCallback.INSTANCE;
    
    // UI Support
    private Activity mActivity;
    
    private TextView mCityTextView;
    private TextView mStoreTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;
    private Button mNextButton;
    
    private ArrayAdapter mCityAdapter;
    private ArrayAdapter mStoreAdapter;
	private String mSelectedCity;
	private String mSelectedStoreName;
	private Store mSelectedStore;
	private ArrayList<String> mCityList;
	private ArrayList<String> mStoreNameList;
	private ArrayList<Store> mStoreList;
	private HashMap<String, ArrayList<Store>> mCityStoreHashMap = new HashMap<String, ArrayList<Store>>();
	private HashMap<String, ArrayList<String>> mCityStoreNameHashMap = new HashMap<String, ArrayList<String>>();
    private ProgressDialog mGetStoreDialog;
    private Thread mGetStoreThread = null;    
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			if (DEBUG) Log.e(TAG, "handleMessage.what." + msg.what);
			switch (msg.what) {
	            case QUERY_SUCCESS: {
	            	mCityAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, mCityList);
//	            	loadData();
	                Toast.makeText(mActivity, R.string.get_store_success_label, 
	               		 Toast.LENGTH_SHORT).show();
	               break;
	           }			
	           case QUERY_FAILURE: {
		           	String failureReason;
		           	if (msg.obj == null) {
		           		failureReason = getString(R.string.get_store_failure_label);
		           	} else {
		           		failureReason = (String) msg.obj;
		           	}
	               Toast.makeText(mActivity, failureReason, 
	               		Toast.LENGTH_SHORT).show();
	               break;
	           }   
			}
		}
	};
    private DatePickerDialog.OnDateSetListener mTakeDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mTakeYear = year;
                    mTakeMonth = monthOfYear + 1;
                    mTakeDay = dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    final Calendar c = Calendar.getInstance();
                    c.set(mTakeYear, monthOfYear, mTakeDay);
//                    Date date = new Date();
//                    date.setYear(year);
//                    date.setDate(dayOfMonth);
//                    date.setMonth(monthOfYear);
                    String date1 = sdf.format(c.getTime());
                    mDateTextView.setText(date1);                    
                }
            };

    private TimePickerDialog.OnTimeSetListener mTakeTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mTakeHour = hourOfDay;
                    mTakeMinute = minute;
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    final Calendar c = Calendar.getInstance();
                    c.set(mTakeYear, mTakeMonth - 1, mTakeDay, mTakeHour, mTakeMinute, 0);
//                    Date date = new Date();
//                    date.setHours(hourOfDay);
//                    date.setMinutes(minute);
                    String time = sdf.format(c.getTime());        
                    mTimeTextView.setText(time);                      
                }
            };    
            
    /**
     * Create a new instance with initialization parameters.
     *
     * This fragment should be created only with this method.  (Arguments should always be set.)
     *
     * @param accountId The ID of the account we want to view
     * @param initialCurrentMailboxId ID of the mailbox of interest.
     *        Pass {@link Mailbox#NO_MAILBOX} to show top-level mailboxes.
     * @param enableHighlight {@code true} if highlighting is enabled on the current screen
     *        configuration.  (We don't highlight mailboxes on one-pane.)
     */
    public static ReservationServiceFragment newInstance(NewFragmentInfo info) {
        final ReservationServiceFragment instance = new ReservationServiceFragment();
        instance.setArguments(info.mArgs);
        return instance;
    }              
    
    /**
     * Called when a fragment is first attached to its activity.
     * {@link #onCreate(Bundle)} will be called after this.
     */
    @Override
    public void onAttach(Activity activity) {
    	if (DEBUG) Log.v(TAG, "onAttach");
    	super.onAttach(activity);
    }
    
    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * 
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(Bundle)}.
     * 
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	if (DEBUG) Log.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * 
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     * 
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     * 
     * @return Return the View for the fragment's UI, or null.
     */    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	if (DEBUG) Log.v(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.reservation_service_layout, container, false);
        return v;
    }
    
    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    	if (DEBUG) Log.v(TAG, "onViewCreated");
    }
    
    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onStart()}.
     * 
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	if (DEBUG) Log.v(TAG, "onActivityCreated");
    	super.onActivityCreated(savedInstanceState);
    	View v = getView();
        mCityTextView = (TextView) v.findViewById(R.id.city_text_view);
        mStoreTextView = (TextView) v.findViewById(R.id.store_text_view);
        mDateTextView = (TextView) v.findViewById(R.id.date_text_view);
        mTimeTextView = (TextView) v.findViewById(R.id.time_text_view);    
        mNextButton = (Button) v.findViewById(R.id.next_button);
        
        mCityTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mCityTextView." + mCityList);
				if (mCityList != null && mCityList.size() > 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle(R.string.my_location_label)
					.setIcon(R.drawable.my_location_default)
					.setCancelable(true)
					.setAdapter(mCityAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSelectedCity = mCityList.get(which);
							mCityTextView.setText(mSelectedCity);
							mStoreList = mCityStoreHashMap.get(mSelectedCity);
							mStoreNameList = mCityStoreNameHashMap.get(mSelectedCity);				
							if (mStoreNameList == null || mStoreNameList.size() == 0) {
								mStoreAdapter = null;
								mSelectedStoreName = null;
								mSelectedStore = null;								
							} else {
								mSelectedStoreName = mStoreNameList.get(0);
								mSelectedStore = mStoreList.get(0);		
								mStoreTextView.setText(mSelectedStoreName);
							}						
						}
					})
					.show();					
				}
			}
        });
        
        mStoreTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mStoreTextView." + mStoreNameList);
				if (mStoreNameList != null && mStoreNameList.size() > 0) {
					mStoreAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_spinner_item, mStoreNameList);
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
					builder.setTitle(R.string.get_store_label)
					.setIcon(R.drawable.ic_car_default)
					.setCancelable(true)
					.setAdapter(mStoreAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSelectedStoreName = mStoreNameList.get(which);
							mSelectedStore = mStoreList.get(which);	
							mStoreTextView.setText(mSelectedStoreName);
						}
					})
					.show();
				}
			}
        });        
        
        mDateTextView.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(mActivity,
                        mTakeDateSetListener,
                        mTakeYear, mTakeMonth - 1, mTakeDay);
				dpd.show();
			}
		});
		
        mTimeTextView.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(mActivity,
                        mTakeTimeSetListener, mTakeHour, mTakeMinute, false);
				tpd.show();
			}
		});        
        
		mNextButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionEnterStoreDetail();
			}
		});        
        
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
//        mReturnYear = c.get(Calendar.YEAR);
//        mReturnMonth = c.get(Calendar.MONTH) + 1;
//        mReturnDay = c.get(Calendar.DAY_OF_MONTH);
//        mReturnHour = c.get(Calendar.HOUR_OF_DAY);
//        mReturnMinute = c.get(Calendar.MINUTE);		
//        mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
//        mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
        mTakeYear = c.get(Calendar.YEAR);
        mTakeMonth = c.get(Calendar.MONTH) + 1;
        mTakeDay = c.get(Calendar.DAY_OF_MONTH);
        mTakeHour = c.get(Calendar.HOUR_OF_DAY);
        mTakeMinute = c.get(Calendar.MINUTE);	 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(c.getTime());
        mDateTextView.setText(date);
        sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(c.getTime());        
        mTimeTextView.setText(time);     

        doActionGetStore();
        mNextButton.setEnabled(true);
    }
    
    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to {@link Activity#onStart() Activity.onStart} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStart() {
        if (DEBUG) Log.v(TAG, "onStart");
        super.onStart();    	
    }
    
    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
    	if (DEBUG) Log.v(TAG, "onResume");
    	super.onResume();
        Intent intent = new Intent(Constants.TITLE_CHANGE_ACTION);
        intent.putExtra(Constants.TITLE_CHANGE_EXTRA, mActivity.getString(R.string.reservation_service_title));
        mActivity.sendBroadcast(intent);    	
    }
    
    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
    	if (DEBUG) Log.v(TAG, "onPause");
    	super.onPause();
    }
    
    /**
     * Called when the Fragment is no longer started.  This is generally
     * tied to {@link Activity#onStop() Activity.onStop} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onStop() {
    	if (DEBUG) Log.v(TAG, "onStop");
    	super.onStop();
		if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
			mGetStoreDialog.dismiss();
		}
    }
    
    /**
     * Called when the view previously created by {@link #onCreateView} has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after {@link #onStop()} and before {@link #onDestroy()}.  It is called
     * <em>regardless</em> of whether {@link #onCreateView} returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    @Override
    public void onDestroyView() {
    	if (DEBUG) Log.v(TAG, "onDestroyView");
    	super.onDestroyView();
    }
    
    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
    	if (DEBUG) Log.v(TAG, "onDestroy");
    	super.onDestroy();
    }
    
    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
    	if (DEBUG) Log.v(TAG, "onDetach");
    	super.onDetach();
    }
    
	// private method do action section
	private void doActionEnterStoreDetail() {
		if (mSelectedStore == null) {
			return;
		}
		final Calendar c = Calendar.getInstance();
		c.set(mTakeYear, mTakeMonth-1, mTakeDay, mTakeHour, mTakeMinute);
		long takeVehicleDate = c.getTimeInMillis();
		long currentTimeMillis = System.currentTimeMillis();
		if ((takeVehicleDate - currentTimeMillis) > MINUTES_MAX_30 || (takeVehicleDate - currentTimeMillis) < 0) {
			Toast.makeText(mActivity, R.string.threshold_30_minutes, Toast.LENGTH_SHORT).show();
			return;
		}
//		c.set(mTakeYear, mTakeMonth-1, mTakeDay, mTakeHour, mTakeMinute);
//		long returnVehicleDate = c.getTimeInMillis();
        String takeVehicleDateString = String.valueOf(takeVehicleDate);
        
//        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
//		
//		ft.add(StoreDetailFragment.newInstance(Constants.FROM_RESERVATION_VEHICLE,
//				mSelectedStore.mId,
//				takeVehicleDateString,
//				mSelectedStoreName), StoreDetailFragment.class.getSimpleName());
//		ft.commit();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.ARG_FROM_WHERE_EXTRA, Constants.FROM_RESERVATION_VEHICLE);
        bundle.putString(Constants.ARG_STORE_ID_EXTRA, mSelectedStore.mId);
        bundle.putString(Constants.ARG_TAKE_VEHICLE_DATE_EXTRA, takeVehicleDateString);
        bundle.putString(Constants.ARG_STORE_NAME_EXTRA, mSelectedStoreName);
//		Message msg = new Message();
//		msg.what = Constants.MSG_START_NEW_FRAGMENT;
		NewFragmentInfo info = new NewFragmentInfo();
		info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
		info.mName = StoreDetailFragment.class.getSimpleName();
		info.mArgs = bundle;
//		msg.obj = info;
//		mHandler.sendMessage(msg);
		mCallback.onNextClicked(info);
	}
	
    private void doActionGetStore() {
		if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
			mGetStoreDialog.dismiss();
		}    	
        mGetStoreDialog = new ProgressDialog(mActivity);
        mGetStoreDialog.setTitle(R.string.get_store_label);
        mGetStoreDialog.setMessage(getString(R.string.getting_store_prompt));
        mGetStoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetStoreThread != null && mGetStoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
//        mGetStoreDialog.setOwnerActivity(mActivity);
        mGetStoreDialog.show();

        // do action getStore
        GetStoreRunner getStoreRunner = new GetStoreRunner();
        mGetStoreThread = new Thread(getStoreRunner);
        mGetStoreThread.start(); 
    }     
    
	// private class section
	private class GetStoreRunner implements Runnable {
		public void run() {
			GetCitiesResult result = null;
			Message msg = new Message();
			try {
				result = WebServiceController.getCities();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (result == null || result.mCitiesCount == 0) {
				msg.what = QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "GetStoreRunner->getStore failure.");
				if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
					mGetStoreDialog.dismiss();
				}
				mHandler.sendMessage(msg);	
				return;
			} else {
				mCityList = result.mCityList;
			}
			
			ArrayList<Store> storeList = null;
			ArrayList<String> storeNameList = null;
			// get store action
			String startIndex = "0";
			String endIndex = "100";
			int length = mCityList.size();
			int index = 0;
			while (index < length) {
				try {
					storeList = WebServiceController.getStores(
							null, 
							null, 
							null, 
							mCityList.get(index), 
							startIndex, 
							endIndex);
				} catch (Exception e) {
					e.printStackTrace();
				}		
				storeNameList = new ArrayList<String>();
				int size = storeList.size();
				int i = 0;
				while (i < size) {
					storeNameList.add(storeList.get(i).mName);
					i++;
				}
				mCityStoreHashMap.put(mCityList.get(index), storeList);
				mCityStoreNameHashMap.put(mCityList.get(index), storeNameList);
				index++;
			}
			
			// Deal with get store result
			if (mCityStoreHashMap.size() > 0) {
				if (DEBUG) Log.d(TAG, "GetStoreRunner->getStore success");
        		msg.what = QUERY_SUCCESS;
			} else {
				msg.what = QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "GetStoreRunner->getStore failure.");
			}
			if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
				mGetStoreDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}     
	
    /**
     * Callback interface that owning activities must implement
     */
    public interface Callback {
        /**
         * Called when next is clicked.
         *
         * @param info
         *          The info of the new fragment.          
         */
        public void onNextClicked(NewFragmentInfo info);        
    }
    
    private static class EmptyCallback implements Callback {
        public static final Callback INSTANCE = new EmptyCallback();
        @Override
        public void onNextClicked(NewFragmentInfo info) {};
    }    
    
    public void setCallback(Callback callback) {
        mCallback = (callback == null) ? EmptyCallback.INSTANCE : callback;
    }    
}