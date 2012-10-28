/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.GetCitiesResult;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.NewFragmentInfo;
import com.zapple.evshare.data.SubmitOrder;
import com.zapple.evshare.data.Store;
import com.zapple.evshare.data.Vehicle;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class StoreDetailFragment extends Fragment {
    private static final String TAG = StoreDetailFragment.class.getSimpleName();
    private static final boolean DEBUG = true;	
    /** Argument name(s) */
//    private static final String ARG_FROM_WHERE_EXTRA = "from_were_extra";
//    private static final String ARG_TAKE_VEHICLE_DATE_EXTRA = "take_vehicle_date_extra"; 
//    private static final String ARG_STORE_NAME_EXTRA = "store_name_extra";
//    private static final String ARG_STORE_ID_EXTRA = "store_id_extra";
	
	
	public static final int FROM_QUICK_ORDER = 1;
	public static final int FROM_RESERVATION_VEHICLE = 2;
	public static final int FROM_STORE_LIST = 3;
	public static final int FROM_FAVORITE = 4;
	
	private static final int MSG_QUERY_SUCCESS = 0;
	private static final int MSG_QUERY_FAILURE = 1;		
	private static final int MSG_ADD_FAVORITE_STORE_SUCCESS = 2;
	private static final int MSG_ADD_FAVORITE_STORE_FAILURE = 3;	
	public static final int MSG_ADD_FAVORITE_VEHICLE = 4;
	private static final int MSG_ADD_FAVORITE_VEHICLE_SUCCESS = 5;
	private static final int MSG_ADD_FAVORITE_VEHICLE_FAILURE = 6;
	public static final int MSG_RESERVATION = 7;
	
	private Callback mCallback = EmptyCallback.INSTANCE;
	private Button mFilterButton;	
	private Spinner mChoiceVehicleModelSpinner;
	private Spinner mChoiceVehicleBrandSpinner;
	private ListView mListView;
	private int mFromWhere;
	private String mStoreId;
	private String mStoreName;
	private String mTakeVehicleDate;
	private List<String> mModelList;
	private List<String> mBrandList;
	
	private List<Vehicle> mVehicleList;
	private Activity mActivity;
    private ProgressDialog mGetVehicleDialog;
    private Thread mGetVehicleThread = null;
    private ProgressDialog mFavoriteStoreDialog;
    private Thread mFavoriteStoreThread = null;  
    private ProgressDialog mFavoriteVehicleDialog;
    private Thread mFavoriteVehicleThread = null;    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_QUERY_SUCCESS: {
            		mChoiceVehicleModelSpinner.setAdapter(new ArrayAdapter<String>(
            				mActivity, 
            				R.layout.simple_spinner_item, 
            				mModelList));
            		mChoiceVehicleModelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							
						}
            		});
            		mChoiceVehicleBrandSpinner.setAdapter(new ArrayAdapter<String>(
            				mActivity, 
            				R.layout.simple_spinner_item, 
            				mBrandList));       
            		mChoiceVehicleBrandSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							
						}
            		});
                	mListView.setAdapter(new VehicleListArrayAdapter(mActivity));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							VehicleListItem listItem = (VehicleListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                     Toast.makeText(mActivity, R.string.get_store_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                    break;
                }
                case MSG_QUERY_FAILURE: {
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
                case MSG_ADD_FAVORITE_STORE_SUCCESS: {
                    Toast.makeText(mActivity, R.string.add_favorite_success_label, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_ADD_FAVORITE_STORE_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.add_favorite_failure_label);
                	} else {
                		failureReason = (String) msg.obj;
                	}
                    Toast.makeText(mActivity, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }    
                case MSG_ADD_FAVORITE_VEHICLE: {
                	doActionFavoriteVehicle((String)msg.obj);
//                    Toast.makeText(mActivity, R.string.add_favorite_success_label, 
//                    		Toast.LENGTH_SHORT).show();
                    break;
                }      
                case MSG_ADD_FAVORITE_VEHICLE_SUCCESS: {
                    Toast.makeText(mActivity, R.string.add_favorite_success_label, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_ADD_FAVORITE_VEHICLE_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.add_favorite_failure_label);
                	} else {
                		failureReason = (String) msg.obj;
                	}
                    Toast.makeText(mActivity, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }       
                case MSG_RESERVATION: {
                	Bundle bundle = new Bundle();
                	SubmitOrder submitOrder = (SubmitOrder)msg.obj;
                	submitOrder.mTakeVehicleStoreId = mStoreId;
                	submitOrder.mTakeStoreName = mStoreName;
                	submitOrder.mTakeVehicleDate = mTakeVehicleDate;
                	bundle.putParcelable(Constants.SUBMIT_ORDER_EXTRA, submitOrder);
            		NewFragmentInfo info = new NewFragmentInfo();
            		info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
            		info.mName = StoreDetailFragment.class.getSimpleName();
            		info.mArgs = bundle;
                	mCallback.onReservationClicked(info);
                    break;
                }                
            }
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
    public static StoreDetailFragment newInstance(int fromWhere,
            String storeId,
            String takeVehicleDate,
            String storeName) {
        final StoreDetailFragment instance = new StoreDetailFragment();
        final Bundle args = new Bundle();
        args.putInt(Constants.ARG_FROM_WHERE_EXTRA, fromWhere);
        args.putString(Constants.ARG_STORE_ID_EXTRA, storeId);
        args.putString(Constants.ARG_TAKE_VEHICLE_DATE_EXTRA, takeVehicleDate);
        args.putString(Constants.ARG_STORE_NAME_EXTRA, storeName);
        instance.setArguments(args);
        return instance;
    }            
       
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
    public static StoreDetailFragment newInstance(NewFragmentInfo info) {
        final StoreDetailFragment instance = new StoreDetailFragment();
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
        View v = inflater.inflate(R.layout.store_detail_layout, container, false);
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
    	
		mChoiceVehicleModelSpinner = (Spinner) v.findViewById(R.id.choice_vehicle_model_spinner);
		mChoiceVehicleBrandSpinner = (Spinner) v.findViewById(R.id.choice_vehicle_brand_spinner);
		mFilterButton = (Button) v.findViewById(R.id.filter_button);
		
		mListView = (ListView) v.findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) v.findViewById(R.id.empty));

		mFilterButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionFilter();
			}
		});

//		if (i != null) {
//			mFromWhere = i.getIntExtra(FROM_WHERE_EXTRA, 0);
//			mStoreId = i.getStringExtra(STORE_ID_EXTRA);
//			mStoreName = i.getStringExtra(STORE_NAME_EXTRA);
//			mTakeVehicleDate = i.getStringExtra(TAKE_VEHICLE_DATE_EXTRA);
//		}	
		
		Bundle bundle = getArguments();
		SubmitOrder submitOrder = bundle.getParcelable(Constants.SUBMIT_ORDER_EXTRA);
		if (submitOrder != null) {
			mFromWhere = Constants.FROM_RESERVATION_VEHICLE;
			mStoreId = submitOrder.mTakeVehicleStoreId;
			mStoreName = submitOrder.mTakeStoreName;
			mTakeVehicleDate = submitOrder.mTakeVehicleDate;				
		} else {
			mFromWhere = bundle.getInt(Constants.ARG_FROM_WHERE_EXTRA);
			mStoreId = bundle.getString(Constants.ARG_STORE_ID_EXTRA);
			mStoreName = bundle.getString(Constants.ARG_STORE_NAME_EXTRA);
			mTakeVehicleDate = bundle.getString(Constants.ARG_TAKE_VEHICLE_DATE_EXTRA);				
		}

		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				VehicleListItem listItem = (VehicleListItem) arg1;
//				doActionEnterOrderDetail(listItem.getListItem().getId());					
			}
		});		

		if (FROM_FAVORITE == mFromWhere || FROM_STORE_LIST == mFromWhere || FROM_QUICK_ORDER == mFromWhere || FROM_RESERVATION_VEHICLE == mFromWhere) {
			doActionGetVehicle();
		}
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
        intent.putExtra(Constants.TITLE_CHANGE_EXTRA, mActivity.getString(R.string.store_detail_title));
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
		if (mGetVehicleDialog != null && mGetVehicleDialog.isShowing()) {
			mGetVehicleDialog.dismiss();
		}    	
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
    private void doActionFilter() {
    	
    }
    
    private void doActionGetVehicle() {
		if (mGetVehicleDialog != null && mGetVehicleDialog.isShowing()) {
			mGetVehicleDialog.dismiss();
		}    	
        mGetVehicleDialog = new ProgressDialog(mActivity);
        mGetVehicleDialog.setTitle(R.string.get_store_label);
        mGetVehicleDialog.setMessage(getString(R.string.getting_store_prompt));
        mGetVehicleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetVehicleThread != null && mGetVehicleThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mGetVehicleDialog.setOwnerActivity(mActivity);
        mGetVehicleDialog.show();

        //TODO: do action getVehicle
        GetVehicleRunner getVehicleRunner = new GetVehicleRunner();
        mGetVehicleThread = new Thread(getVehicleRunner);
        mGetVehicleThread.start();
    }
    
    private void doActionFavoriteStore() {
		if (mFavoriteStoreDialog != null && mFavoriteStoreDialog.isShowing()) {
			mFavoriteStoreDialog.dismiss();
		}    	
        mFavoriteStoreDialog = new ProgressDialog(mActivity);
        mFavoriteStoreDialog.setTitle(R.string.add_favorite_label);
        mFavoriteStoreDialog.setMessage(getString(R.string.add_favorite_prompt));
        mFavoriteStoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mFavoriteStoreThread != null && mFavoriteStoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mFavoriteStoreDialog.setOwnerActivity(mActivity);
        mFavoriteStoreDialog.show();

        //TODO: do action getVehicle
        FavoriteStoreRunner favoriteStoreRunner = new FavoriteStoreRunner();
        mFavoriteStoreThread = new Thread(favoriteStoreRunner);
        mFavoriteStoreThread.start();
    }    
        
    private void doActionFavoriteVehicle(String vehicleId) {
		if (mFavoriteVehicleDialog != null && mFavoriteVehicleDialog.isShowing()) {
			mFavoriteVehicleDialog.dismiss();
		}    	
        mFavoriteVehicleDialog = new ProgressDialog(mActivity);
        mFavoriteVehicleDialog.setTitle(R.string.add_favorite_label);
        mFavoriteVehicleDialog.setMessage(getString(R.string.add_favorite_prompt));
        mFavoriteVehicleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mFavoriteVehicleThread != null && mFavoriteVehicleThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mFavoriteVehicleDialog.setOwnerActivity(mActivity);
        mFavoriteVehicleDialog.show();

        //TODO: do action getVehicle
        FavoriteVehicleRunner favoriteVehicleRunner = new FavoriteVehicleRunner(vehicleId);
        mFavoriteVehicleThread = new Thread(favoriteVehicleRunner);
        mFavoriteVehicleThread.start();
    }    
    
	// private method section

    // private class section  
	private class GetVehicleRunner implements Runnable {
		
		public void run() {
			// get Vehicle action
			String startIndex = "0";
			String endIndex = "100";
			try {
				mVehicleList = WebServiceController.getVehicles(mStoreId, startIndex, endIndex);
			} catch (Exception e) {
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "GetVehicleRunner->" + mVehicleList);
			
			// Deal with upload result
			Message msg = new Message();
			if (mVehicleList != null) {
				if (DEBUG) Log.d(TAG, "GetVehicleRunner->getVehicle success");
				int length = mVehicleList.size();
				int index = 0;
				mBrandList = new ArrayList<String>();
				mModelList = new ArrayList<String>();
				while(index < length) {
					Vehicle vehicle = mVehicleList.get(index);
					mBrandList.add(vehicle.mBrand);
					mModelList.add(vehicle.mModel);
					index++;
				}
        		msg.what = MSG_QUERY_SUCCESS;
			} else {
				msg.what = MSG_QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "GetVehicleRunner->getVehicle failure.");
			}
			if (mGetVehicleDialog != null && mGetVehicleDialog.isShowing()) {
				mGetVehicleDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
	
	private class FavoriteStoreRunner implements Runnable {

		public void run() {
			// get Vehicle action
			String result = null;
			try {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
				String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
				result = WebServiceController.addFavorite(userName, Constants.FAVORITE_TYPE_STORE, mStoreId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "FavoriteStoreRunner->" + result);
			
			// Deal with upload result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				if (DEBUG) Log.d(TAG, "FavoriteStoreRunner->FavoriteStore success");
        		msg.what = MSG_ADD_FAVORITE_STORE_SUCCESS;
			} else {
				msg.what = MSG_ADD_FAVORITE_STORE_FAILURE;
				if (DEBUG) Log.d(TAG, "FavoriteStoreRunner->FavoriteStore failure.");
			}
			if (mFavoriteStoreDialog != null && mFavoriteStoreDialog.isShowing()) {
				mFavoriteStoreDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
	
	private class FavoriteVehicleRunner implements Runnable {
		private String mVehicleId;
		
		FavoriteVehicleRunner(String vehicleId) {
			mVehicleId = vehicleId;
		}
		
		public void run() {
			// get Vehicle action
			String result = null;
			try {
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
				String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
				result = WebServiceController.addFavorite(userName, Constants.FAVORITE_TYPE_VEHICLE, mVehicleId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "FavoriteVehicleRunner->" + result);
			
			// Deal with upload result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				if (DEBUG) Log.d(TAG, "FavoriteVehicleRunner->FavoriteVehicle success");
        		msg.what = MSG_ADD_FAVORITE_VEHICLE_SUCCESS;
			} else {
				msg.what = MSG_ADD_FAVORITE_VEHICLE_FAILURE;
				msg.obj = result;
				if (DEBUG) Log.d(TAG, "FavoriteVehicleRunner->FavoriteVehicle failure.");
			}
			if (mFavoriteVehicleDialog != null && mFavoriteVehicleDialog.isShowing()) {
				mFavoriteVehicleDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
    
	private class VehicleListArrayAdapter extends BaseAdapter {
		private TextView textView1;
		private LayoutInflater inflater;

		public VehicleListArrayAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public void setList(List<Vehicle> list) {
			mVehicleList = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mVehicleList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				// 将自定义布局 －> View
				convertView = inflater.inflate(R.layout.vehicle_list_item, null);
				// 获取元素
			}
			VehicleListItem headerView = (VehicleListItem) convertView;
			VehicleItem item = new VehicleItem(mVehicleList.get(position));
			item.setStoreId(mStoreId);
			item.setTakeVehicleDate(mTakeVehicleDate);
	        headerView.bind(mActivity, item);
	        headerView.setFromWhere(mFromWhere);
	        headerView.setHandler(mHandler);
	        
			return convertView;
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
        public void onReservationClicked(NewFragmentInfo info);       
        
        public void onStoreDetailBackClicked(NewFragmentInfo info);
    }
    
    private static class EmptyCallback implements Callback {
        public static final Callback INSTANCE = new EmptyCallback();
        @Override
        public void onReservationClicked(NewFragmentInfo info) {}
		@Override
		public void onStoreDetailBackClicked(NewFragmentInfo info) {};
    }    
    
    public void setCallback(Callback callback) {
        mCallback = (callback == null) ? EmptyCallback.INSTANCE : callback;
    }	
}