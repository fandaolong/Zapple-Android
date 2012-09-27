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

package com.zapple.rental.ui;

import java.util.ArrayList;
import java.util.List;

import com.zapple.rental.R;
import com.zapple.rental.data.LoginResult;
import com.zapple.rental.data.Vehicle;
import com.zapple.rental.data.Vehicle.VehicleTable;
import com.zapple.rental.transaction.WebServiceController;
import com.zapple.rental.util.Constants;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StoreDetailActivity extends Activity {
	private static final String TAG = "StoreDetailActivity";
	private static final boolean DEBUG = true;
	private static final int LIST_QUERY_TOKEN = 9601;
	public static final String STORE_ID_EXTRA = "store_id_extra";
	public static final String STORE_NAME_EXTRA = "store_name_extra";
	public static final String FROM_WHERE_EXTRA = "from_were_extra";
	public static final String TAKE_VEHICLE_DATE_EXTRA = "take_vehicle_date_extra";
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
	
	private TextView mTitleTextView;
	private Button mFavoriteButton;	
	private Spinner mChoiceVehicleModelSpinner;
	private Spinner mChoiceVehicleBrandSpinner;
	private ListView mListView;
	private VehicleListAdapter mListAdapter;
	private BackgroundQueryHandler mBackgroundQueryHandler;
	private int mFromWhere;
	private String mStoreId;
	private String mStoreName;
	private String mTakeVehicleDate;
	private List<String> mModelList;
	private List<String> mBrandList;
	
	private List<Vehicle> mVehicleList;
	private Context mContext;
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
            				StoreDetailActivity.this, 
            				android.R.layout.simple_spinner_item, 
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
            				StoreDetailActivity.this, 
            				android.R.layout.simple_spinner_item, 
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
                	mListView.setAdapter(new VehicleListArrayAdapter(mContext));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							VehicleListItem listItem = (VehicleListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                     Toast.makeText(mContext, R.string.get_store_success_label, 
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
                    Toast.makeText(mContext, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_ADD_FAVORITE_STORE_SUCCESS: {
                    Toast.makeText(mContext, R.string.add_favorite_success_label, 
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
                    Toast.makeText(mContext, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }    
                case MSG_ADD_FAVORITE_VEHICLE: {
                	doActionFavoriteVehicle((String)msg.obj);
                    Toast.makeText(mContext, R.string.add_favorite_success_label, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }      
                case MSG_ADD_FAVORITE_VEHICLE_SUCCESS: {
                    Toast.makeText(mContext, R.string.add_favorite_success_label, 
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
                    Toast.makeText(mContext, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }                 
            }
        }
    };	
    /**
     * Called when the activity is starting.  This is where most initialization
     * should go: calling {@link #setContentView(int)} to inflate the
     * activity's UI, using {@link #findViewById} to programmatically interact
     * with widgets in the UI, calling
     * {@link #managedQuery(android.net.Uri , String[], String, String[], String)} to retrieve
     * cursors for data being displayed, etc.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.store_detail_layout);
		
		mContext = this;
		// find view section
		mTitleTextView = (TextView) findViewById(R.id.title_text_view);
		mFavoriteButton = (Button) findViewById(R.id.favorite_button);
		mChoiceVehicleModelSpinner = (Spinner) findViewById(R.id.choice_vehicle_model_spinner);
		mChoiceVehicleBrandSpinner = (Spinner) findViewById(R.id.choice_vehicle_brand_spinner);
		
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));

		mFavoriteButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionFavoriteStore();
			}
		});

		mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());
		Intent i = getIntent();
		if (i != null) {
			mFromWhere = i.getIntExtra(FROM_WHERE_EXTRA, 0);
			mStoreId = i.getStringExtra(STORE_ID_EXTRA);
			mStoreName = i.getStringExtra(STORE_NAME_EXTRA);
			mTakeVehicleDate = i.getStringExtra(TAKE_VEHICLE_DATE_EXTRA);
		}		
		mListAdapter = new VehicleListAdapter(this, null);
		mListAdapter.setFromWhere(mFromWhere);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				VehicleListItem listItem = (VehicleListItem) arg1;
//				doActionEnterOrderDetail(listItem.getListItem().getId());					
			}
		});		
		mTitleTextView.setText(mStoreName);

		if (FROM_FAVORITE == mFromWhere || FROM_STORE_LIST == mFromWhere || FROM_QUICK_ORDER == mFromWhere || FROM_RESERVATION_VEHICLE == mFromWhere) {
			doActionGetVehicle();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }	
	
    /**
     * Called after {@link #onStop} when the current activity is being
     * re-displayed to the user (the user has navigated back to it).  It will
     * be followed by {@link #onStart} and then {@link #onResume}.
     */	
	@Override
	public void onRestart() {
		if (DEBUG) Log.d(TAG, "onRestart");
		super.onRestart();		
	}

    /**
     * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when  
     * the activity had been stopped, but is now again being displayed to the 
	 * user.  It will be followed by {@link #onResume}.
	 */	
	@Override
	public void onStart() {
		if (DEBUG) Log.d(TAG, "onStart");
		super.onStart();
		if (FROM_FAVORITE == mFromWhere || FROM_STORE_LIST == mFromWhere || FROM_QUICK_ORDER == mFromWhere || FROM_RESERVATION_VEHICLE == mFromWhere) {
			
		} else {
			startListQuery();
		}		
	}

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * <var>savedInstanceState</var>.  Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     */	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (DEBUG) Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);		
	}
	
    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     */	
	@Override
	public void onResume() {
		if (DEBUG) Log.d(TAG, "onResume");
		super.onResume();
	}

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     */	
	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause");
		super.onPause();		
	}

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link Bundle} populated by this method
     * will be passed to both).
     * 
     * <p>If called, this method will occur before {@link #onStop}.  There are
     * no guarantees about whether it will occur before or after {@link #onPause}.
     */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (DEBUG) Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);			
	}
	
    /**
     * Called when you are no longer visible to the user.  You will next
     * receive either {@link #onRestart}, {@link #onDestroy}, or nothing,
     * depending on later user activity.
     */	
	@Override
	public void onStop() {
		if (DEBUG) Log.d(TAG, "onStop");
		super.onStop();		
        // Close the cursor in the ListAdapter if the activity stopped.
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        mListAdapter.changeCursor(null);		
	}
	
    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     */	
	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG, "onDestroy");
		super.onDestroy();	
		
		// TODO: deal thread, ..., etc.
	}
	
	// private method do action section
    private void doActionGetVehicle() {
		if (mGetVehicleDialog != null && mGetVehicleDialog.isShowing()) {
			mGetVehicleDialog.dismiss();
		}    	
        mGetVehicleDialog = new ProgressDialog(this);
        mGetVehicleDialog.setTitle(R.string.get_store_label);
        mGetVehicleDialog.setMessage(getString(R.string.getting_store_prompt));
        mGetVehicleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetVehicleThread != null && mGetVehicleThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mGetVehicleDialog.setOwnerActivity(this);
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
        mFavoriteStoreDialog = new ProgressDialog(this);
        mFavoriteStoreDialog.setTitle(R.string.add_favorite_label);
        mFavoriteStoreDialog.setMessage(getString(R.string.add_favorite_prompt));
        mFavoriteStoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mFavoriteStoreThread != null && mFavoriteStoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mFavoriteStoreDialog.setOwnerActivity(this);
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
        mFavoriteVehicleDialog = new ProgressDialog(this);
        mFavoriteVehicleDialog.setTitle(R.string.add_favorite_label);
        mFavoriteVehicleDialog.setMessage(getString(R.string.add_favorite_prompt));
        mFavoriteVehicleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mFavoriteVehicleThread != null && mFavoriteVehicleThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mFavoriteVehicleDialog.setOwnerActivity(this);
        mFavoriteVehicleDialog.show();

        //TODO: do action getVehicle
        FavoriteVehicleRunner favoriteVehicleRunner = new FavoriteVehicleRunner(vehicleId);
        mFavoriteVehicleThread = new Thread(favoriteVehicleRunner);
        mFavoriteVehicleThread.start();
    }    
    
	// private method section
	private void startListQuery() {
		if (DEBUG) Log.d(TAG, "startListQuery");
        String selection = null;

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(LIST_QUERY_TOKEN);
        try {       	
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
                    LIST_QUERY_TOKEN, null, VehicleTable.CONTENT_URI,
                    VehicleListAdapter.VEHICLE_PROJECTION, selection, null, null);
        } catch (SQLiteException e) {
        	if (DEBUG) Log.e(TAG, "startListQuery:" + e);
        }
	}
	
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
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
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
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
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
				if (DEBUG) Log.d(TAG, "FavoriteVehicleRunner->FavoriteVehicle failure.");
			}
			if (mFavoriteVehicleDialog != null && mFavoriteVehicleDialog.isShowing()) {
				mFavoriteVehicleDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
	
    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch(token) {
                case LIST_QUERY_TOKEN:
                    mListAdapter.changeCursor(cursor);
                    break;
            }
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
	        headerView.bind(mContext, item);
	        headerView.setFromWhere(mFromWhere);
	        headerView.setHandler(mHandler);
	        
			return convertView;
		}
	}     
}