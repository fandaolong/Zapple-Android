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

import java.util.Calendar;

import com.zapple.evshare.R;
import com.zapple.evshare.data.NewFragmentInfo;
import com.zapple.evshare.data.SubmitOrder;
import com.zapple.evshare.util.Constants;

import android.support.v4.app.Fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class OrderChooseTimeFragment extends Fragment implements OnClickListener {
    private static final String TAG = OrderChooseTimeFragment.class.getSimpleName();
	private static final boolean DEBUG = true;
	
    private final static long MINUTES_MAX_30 = 30*60*1000;
    
	private Button mTakeVehicleDateButton;
	private Button mTakeVehicleTimeButton;
	private Button mReturnVehicleDateButton;
	private Button mReturnVehicleTimeButton;
	private Button mNextButton;
	
	private SubmitOrder mSubmitOrder;
    // date and time
    private int mTakeYear;
    private int mTakeMonth;
    private int mTakeDay;
    private int mTakeHour;
    private int mTakeMinute;
    private int mReturnYear;
    private int mReturnMonth;
    private int mReturnDay;
    private int mReturnHour;
    private int mReturnMinute;		
	
	private Activity mActivity;
	private Button mPreviousButton;

	private Callback mCallback = EmptyCallback.INSTANCE;

    private DatePickerDialog.OnDateSetListener mTakeDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mTakeYear = year;
                    mTakeMonth = monthOfYear + 1;
                    mTakeDay = dayOfMonth;
                    mTakeVehicleDateButton.setText(mTakeYear + "-" + mTakeMonth + "-" + mTakeDay);
                }
            };

    private TimePickerDialog.OnTimeSetListener mTakeTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mTakeHour = hourOfDay;
                    mTakeMinute = minute;
                    mTakeVehicleTimeButton.setText(mTakeHour + ":" + mTakeMinute);
                }
            };	
	
    private DatePickerDialog.OnDateSetListener mReturnDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mReturnYear = year;
                    mReturnMonth = monthOfYear + 1;
                    mReturnDay = dayOfMonth;
                    mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
                }
            };

    private TimePickerDialog.OnTimeSetListener mReturnTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mReturnHour = hourOfDay;
                    mReturnMinute = minute;
                    mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
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
    public static OrderChooseTimeFragment newInstance(NewFragmentInfo info) {
        final OrderChooseTimeFragment instance = new OrderChooseTimeFragment();
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
        Intent intent = new Intent(Constants.TITLE_CHANGE_ACTION);
        intent.putExtra(Constants.TITLE_CHANGE_EXTRA, mActivity.getString(R.string.reservation_date_label));
        mActivity.sendBroadcast(intent);
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
        View v = inflater.inflate(R.layout.order_choose_time_layout, container, false);
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
        // find view section
		mTakeVehicleDateButton = (Button) v.findViewById(R.id.take_vehicle_date_button);
		mTakeVehicleTimeButton = (Button) v.findViewById(R.id.take_vehicle_time_button);		
		mReturnVehicleDateButton = (Button) v.findViewById(R.id.return_vehicle_date_button);
		mReturnVehicleTimeButton = (Button) v.findViewById(R.id.return_vehicle_time_button);

        mNextButton = (Button) v.findViewById(R.id.next_button);
        mPreviousButton = (Button) v.findViewById(R.id.previous_button);
        
		mTakeVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(
						mActivity,
                        mTakeDateSetListener,
                        mTakeYear, mTakeMonth, mTakeDay);
				dpd.show();
			}
		});
		
		mTakeVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(
						mActivity,
                        mTakeTimeSetListener, mTakeHour, mTakeMinute, false);
				tpd.show();
			}
		});	
		
		mReturnVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(
						mActivity,
                        mReturnDateSetListener,
                        mReturnYear, mReturnMonth, mReturnDay);
				dpd.show();			
			}
		});
		
		mReturnVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(
						mActivity,
						mReturnTimeSetListener, mReturnHour, mReturnMinute, false);
				tpd.show();				
			}
		});	        
        
        mNextButton.setOnClickListener(this);
        
        mPreviousButton.setOnClickListener(this);        
        
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        mReturnYear = c.get(Calendar.YEAR);
        mReturnMonth = c.get(Calendar.MONTH) + 1;
        mReturnDay = c.get(Calendar.DAY_OF_MONTH) + 1;
        mReturnHour = c.get(Calendar.HOUR_OF_DAY);
        mReturnMinute = c.get(Calendar.MINUTE);		
        mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
        mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
        mTakeYear = c.get(Calendar.YEAR);
        mTakeMonth = c.get(Calendar.MONTH) + 1;
        mTakeDay = c.get(Calendar.DAY_OF_MONTH);
        mTakeHour = c.get(Calendar.HOUR_OF_DAY);
        mTakeMinute = c.get(Calendar.MINUTE);	        
        mTakeVehicleDateButton.setText(mTakeYear + "-" + mTakeMonth + "-" + mTakeDay);
        mTakeVehicleTimeButton.setText(mTakeHour + ":" + mTakeMinute);   
		
		Bundle bundle = getArguments();
		mSubmitOrder = bundle.getParcelable(Constants.SUBMIT_ORDER_EXTRA);
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
    
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.next_button: {
				doActionNext();
				break;
			}
			case R.id.previous_button: {
				doActionPrevious();
				break;
			}
		}
	}	    
    
	// private method do action section
	private void doActionNext() {
		if (mSubmitOrder != null) {
			final Calendar c = Calendar.getInstance();
			c.set(mTakeYear, mTakeMonth - 1, mTakeDay, mTakeHour, mTakeMinute);
			long takeVehicleDate = c.getTimeInMillis();
			long currentTimeMillis = System.currentTimeMillis();
			if ((takeVehicleDate - currentTimeMillis) > MINUTES_MAX_30
					|| (takeVehicleDate - currentTimeMillis) < 0) {
				Toast.makeText(mActivity, R.string.threshold_30_minutes,
						Toast.LENGTH_SHORT).show();
				return;
			}
			c.set(mReturnYear, mReturnMonth - 1, mReturnDay, mReturnHour,
					mReturnMinute);
			long returnVehicleDate = c.getTimeInMillis();
			mSubmitOrder.mTakeVehicleDate = String.valueOf(takeVehicleDate);
			mSubmitOrder.mReturnVehicleDate = String.valueOf(returnVehicleDate);
		}
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.SUBMIT_ORDER_EXTRA, mSubmitOrder);
		NewFragmentInfo info = new NewFragmentInfo();
		info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
		info.mName = OrderChooseTimeFragment.class.getSimpleName();
		info.mArgs = bundle;
		mCallback.onChooseTimeNextClicked(info);		
	}
	
	private void doActionPrevious() {
    	Bundle bundle = new Bundle();
    	bundle.putParcelable(Constants.SUBMIT_ORDER_EXTRA, mSubmitOrder);
		NewFragmentInfo info = new NewFragmentInfo();
		info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
		info.mName = OrderChooseTimeFragment.class.getSimpleName();
		info.mArgs = bundle;
		mCallback.onChooseTimePreviousClicked(info);
	}
	// private method section

	
	// private class section
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
        public void onChooseTimePreviousClicked(NewFragmentInfo info);        
        /**
         * Called when next is clicked.
         *
         * @param info
         *          The info of the new fragment.          
         */
        public void onChooseTimeNextClicked(NewFragmentInfo info);             
    }
    
    private static class EmptyCallback implements Callback {
        public static final Callback INSTANCE = new EmptyCallback();
        @Override
        public void onChooseTimePreviousClicked(NewFragmentInfo info) {};
        @Override
        public void onChooseTimeNextClicked(NewFragmentInfo info) {};        
    }    
    
    public void setCallback(Callback callback) {
        mCallback = (callback == null) ? EmptyCallback.INSTANCE : callback;
    }
}