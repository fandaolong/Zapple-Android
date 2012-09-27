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
import com.zapple.rental.ZappleRentalApp;
import com.zapple.rental.data.AdvertiseUpgradeResult;
import com.zapple.rental.data.GetCitiesResult;
import com.zapple.rental.data.LoginResult;
import com.zapple.rental.data.PersonalInfo;
import com.zapple.rental.data.Store;
import com.zapple.rental.data.Order.OrderTable;
import com.zapple.rental.data.PilesSite.PilesSiteTable;
import com.zapple.rental.data.Score.ScoreTable;
import com.zapple.rental.data.Store.StoreTable;
import com.zapple.rental.data.SubmitOrder;
import com.zapple.rental.data.Vehicle.VehicleTable;
import com.zapple.rental.service.GpsService;
import com.zapple.rental.test.TestSoapTask;
import com.zapple.rental.transaction.WebServiceController;
import com.zapple.rental.util.Utility;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity providers homepage feature.
 */
public class HomePageActivity extends Activity {
    private static final String TAG = "HomePageActivity";
    private static final boolean DEBUG = true;   	
	private static final boolean TEST_SECTION_CODE = false;
	private static final boolean TEST_SOAP_SECTION_CODE = false;
	private static final boolean TEST_MAP_SECTION_CODE = false;
	
	private static final int LOGIN_REQUEST_CODE = 1;
	
	private static final int LOCATION_CHANGE = 1;
	private static final int QUERY_SUCCESS = 2;
	private static final int QUERY_FAILURE = 3;	
	
    private Button mLoginButton;
    private TextView mGpsInfoTextView;
    private ImageView mGetCurrentLocationImageView;
    private GridView mHomePageGridView;
    private List<TextView> mAppsList;
    private TextView mAdvertisementTextView;
    
	private Context mContext;
	private ArrayList<Store> mStoreList;
    private ProgressDialog mGetStoreDialog;
    private Thread mGetStoreThread = null;
    private SharedPreferences mSharedPreferences;
    private UpgradeAdvertiseTask mUpgradeAdvertiseTask;
    private GetCitiesTask mGetCitiesTask;
    private GpsConnection mConnection;
    private ArrayList<String> mCityList;
    private UiHandler mHandler = new UiHandler();
    
	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LOCATION_CHANGE: {
					Log.d(TAG, "LOCATION_CHANGE");
					doActionGetCurrentLocation();
					break;
				}
                case QUERY_SUCCESS: {
                     Toast.makeText(mContext, R.string.get_store_success_label, 
                    		 Toast.LENGTH_SHORT).show();
                     doActionEnterQuickOrder();
                    break;
                }			
                case QUERY_FAILURE: {
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
			}
		}
	}    
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
    	if (DEBUG) Log.v(TAG, "onCreate");
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
//    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        mContext = this;
        
        loadApps();
        
        mLoginButton = (Button) findViewById(R.id.login_button);
        mGpsInfoTextView = (TextView) findViewById(R.id.gps_info_text_view);
        mGetCurrentLocationImageView = (ImageView) findViewById(R.id.current_location_image_view);
        mHomePageGridView = (GridView) findViewById(R.id.home_page_grid_view);
        mAdvertisementTextView = (TextView) findViewById(R.id.advertisement_text_view);
        mHomePageGridView.setAdapter(new AppsAdapter());
        
        mLoginButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (ZappleRentalApp.getApplication().isLogined()) {										
					doActionEnterLogout();
				} else {		
					doActionEnterLogin();
				}								
			}
		});
        mGetCurrentLocationImageView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				doActionGetCurrentLocation();
			}
		});
        mAdvertisementTextView.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionEnterAdvertisement();
			}
		});
        
        // clear last personal info 
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HomePageActivity.this);
        Editor e = mSharedPreferences.edit();
        e.remove(LoginResult.LOGIN_RESULT_TOTAL_SCORES_KEY);
        e.remove(LoginResult.LOGIN_RESULT_EXPIRY_DATE_KEY);
        e.remove(LoginResult.LOGIN_RESULT_ACCOUNT_DATA_KEY);
		
        e.remove(PersonalInfo.PERSONAL_INFO_NAME_KEY);
        e.remove(PersonalInfo.PERSONAL_INFO_USER_RANK_KEY);
        e.remove(PersonalInfo.PERSONAL_INFO_ID_TYPE_KEY);        
        e.remove(PersonalInfo.PERSONAL_INFO_ID_KEY);
        e.remove(PersonalInfo.PERSONAL_INFO_PHONE_NUMBER_KEY);
        e.remove(PersonalInfo.PERSONAL_INFO_EMAIL_ADDRESS_KEY);           
        		
        e.remove(LoginResult.LOGIN_RESULT_ACCOUNT_DATA_KEY);
		e.commit();        
        
		mUpgradeAdvertiseTask = new UpgradeAdvertiseTask();
		mUpgradeAdvertiseTask.execute();
		
		mConnection = new GpsConnection();
		Intent intent = new Intent(getApplicationContext(),
				GpsService.class);
		startService(intent);
		bindService(new Intent(this, GpsService.class), mConnection, Context.BIND_AUTO_CREATE);		
		
		// check login status 
		if (ZappleRentalApp.getApplication().isLogined()) {
			mLoginButton.setText(R.string.logout_button_text);
		} else {
			mLoginButton.setText(R.string.login_button_text);
		}
		
		//just for test
		if (TEST_SOAP_SECTION_CODE) {
			TestSoapTask testSoapTask = new TestSoapTask(mContext);
			testSoapTask.execute();
		}
		
		// just for test
		if (TEST_SECTION_CODE) {
			ContentValues values = new ContentValues();
			values.put(VehicleTable.REMOTE_ID, "z993");
			values.put(VehicleTable.BRAND, "BMW");
			values.put(VehicleTable.MODEL, "SUV");
			values.put(VehicleTable.PRICE, "$300/day");
			values.put(VehicleTable.DUMP_ENERGY, "80%");
			values.put(VehicleTable.PARKING_GARAGE, "1-2-3");
			values.put(VehicleTable.PHOTO_URI, "");
			ContentValues[] valuesArray = {
				values, values, values
			};
			this.getContentResolver().bulkInsert(VehicleTable.CONTENT_URI, valuesArray);
			
			values.clear();
			values.put(StoreTable.REMOTE_ID, "zp003");
			values.put(StoreTable.NAME, "Store 1");
			values.put(StoreTable.ADDRESS, "1st door in beijing");
			values.put(StoreTable.LONGITUDE, "-122.084095");
			values.put(StoreTable.LATITUDE, "37.422006");
			values.put(StoreTable.ALTITUDE, "20");
			ContentValues[] valuesArray1 = {
				values, values, values
			};
			this.getContentResolver().bulkInsert(StoreTable.CONTENT_URI, valuesArray1);
			
			values.clear();
			values.put(ScoreTable.REMOTE_ID, 1);
			values.put(ScoreTable.DESCRIPTION, "Score 1");
			values.put(ScoreTable.SCORE_VALUE, "100");
			values.put(ScoreTable.DATE, "2012.08.01");
			values.put(ScoreTable.EXPIRY_DATE, "2015.09.01");

			ContentValues[] valuesArray2 = {
				values, values, values
			};
			this.getContentResolver().bulkInsert(ScoreTable.CONTENT_URI, valuesArray2);
			
			values.clear();
			values.put(OrderTable.REMOTE_ID, "111");
			values.put(OrderTable.NAME, "Order 1");
			values.put(OrderTable.STATUS, "100");

			ContentValues[] valuesArray3 = {
				values, values
			};
			this.getContentResolver().bulkInsert(OrderTable.CONTENT_URI, valuesArray3);
			
			values.clear();
			values.put(PilesSiteTable.REMOTE_ID, 1);
			values.put(PilesSiteTable.NAME, "PilesSite 1");
			values.put(PilesSiteTable.ADDRESS, "1st door in beijing");
			values.put(PilesSiteTable.LONGITUDE, "-122.084095");
			values.put(PilesSiteTable.LATITUDE, "37.422006");
			values.put(PilesSiteTable.ALTITUDE, "20");
			ContentValues[] valuesArray4 = {
				values, values, values
			};
			this.getContentResolver().bulkInsert(PilesSiteTable.CONTENT_URI, valuesArray4);			
		}        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_page, menu);
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
    protected void onStart() {
        if (DEBUG) Log.v(TAG, "onStart");        
        super.onStart();
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
    protected void onResume() {
        if (DEBUG) Log.v(TAG, "onResume");        
        super.onResume();
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.  The counterpart to
     * {@link #onResume}.
     */    
    @Override
    protected void onPause() {
        if (DEBUG) Log.v(TAG, "onPause");
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
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
    
    /**
     * Called when you are no longer visible to the user.  You will next
     * receive either {@link #onRestart}, {@link #onDestroy}, or nothing,
     * depending on later user activity.
     */	    
    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop");
        super.onStop();
    }

    /**
     * Perform any final cleanup before an activity is destroyed.  This can
     * happen either because the activity is finishing (someone called
     * {@link #finish} on it, or because the system is temporarily destroying
     * this instance of the activity to save space.  You can distinguish
     * between these two scenarios with the {@link #isFinishing} method.
     */	    
    @Override
    protected void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy");
        super.onDestroy();
        
        // TODO: deal thread, ..., etc.
        Utility.cancelTaskInterrupt(mUpgradeAdvertiseTask);
        mUpgradeAdvertiseTask = null;
        Utility.cancelTaskInterrupt(mGetCitiesTask);
        mGetCitiesTask = null;        

        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.v(TAG, "onBackPressed");
        super.onBackPressed();
    }    
    
    /**
     * Called when the search key is pressd.
     *
     * Use the below command to emulate the key press on devices without the search key.
     * adb shell input keyevent 84
     */
    @Override
    public boolean onSearchRequested() {
        if (DEBUG) Log.v(TAG, "onSearchRequested");
        return true; // Event handled.
    }    
    
    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * 
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     */	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG) {
			Log.e(TAG, "onActivityResult.requestCode." + requestCode +
					", resultCode." + resultCode + ", data=" + data);			
		}
		if (resultCode != RESULT_OK){
			return;
		}
		switch (requestCode) {
			case LOGIN_REQUEST_CODE: {
				if (ZappleRentalApp.getApplication().isLogined()) {
					mLoginButton.setText(R.string.logout_button_text);
				} else {
					mLoginButton.setText(R.string.login_button_text);
				}
			}
		}
	}
    
	// private method do action section  
    private void doActionEnterLogin() {
    	Intent i = new Intent(HomePageActivity.this, LoginActivity.class);
    	startActivityForResult(i, LOGIN_REQUEST_CODE);     	
    }
    
    private void doActionEnterLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.logout_button_text)
        	.setMessage(R.string.logout_prompt)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mLoginButton.setText(R.string.login_button_text);
					ZappleRentalApp.getApplication().setLogined(false);
					// TODO: logout and notify the server
					if (dialog != null) {
						dialog.dismiss();
					}
				}            	
            })
            .show();    	
    }
    
    private void doActionNoticeLoginPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.prompt_label)
        	.setMessage(R.string.notice_login_prompt)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					doActionEnterLogin();
					if (dialog != null) {
						dialog.dismiss();
					}					
				}            	
            })
            .show();    	
    }
    
    private void doActionNoticeLocationPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.prompt_label)
        	.setMessage(R.string.notice_location_prompt)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO: location ?
					if (dialog != null) {
						dialog.dismiss();
					}					
				}            	
            })
            .show();    	
    }    
    
    private void doActionGetStore() {
		if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
			mGetStoreDialog.dismiss();
		}    	
        mGetStoreDialog = new ProgressDialog(this);
        mGetStoreDialog.setTitle(R.string.get_store_label);
        mGetStoreDialog.setMessage(getString(R.string.getting_store_prompt));
        mGetStoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetStoreThread != null && mGetStoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mGetStoreDialog.setOwnerActivity(this);
        mGetStoreDialog.show();

        // do action getStore
        GetStoreRunner getStoreRunner = new GetStoreRunner();
        mGetStoreThread = new Thread(getStoreRunner);
        mGetStoreThread.start();
    }  
    
    private void doActionGetCurrentLocation() {
		if (mConnection != null) {
			Location location = mConnection.getGpsLocation();
			if (location != null) {
				mGpsInfoTextView.setText(
						getString(R.string.gps_info_prompt) + 
						location.getLatitude() + 
						"," + 
						location.getLongitude());				
			}
//			String note = location == null ? "Location change" : location.toString();
//			Toast.makeText(getApplicationContext(), note, Toast.LENGTH_SHORT).show();						
		}    	
    }
    
    private void doActionEnterAdvertisement() {
    	Intent i = new Intent(HomePageActivity.this, AdvertisementActivity.class);
    	startActivity(i);  
    }
    
    private void doActionEnterQuickOrder() {
    	Intent i = new Intent(HomePageActivity.this, QuickOrderActivity.class);
    	i.putExtra(QuickOrderActivity.TITLE_EXTRA, getString(R.string.quick_order_cell_item_name));
    	i.putParcelableArrayListExtra(QuickOrderActivity.STORE_LIST_EXTRA, mStoreList);
    	startActivity(i);      	
    }    
    
    private void doActionEnterReservationVehicle() {
    	Intent i = new Intent(HomePageActivity.this, ReservationVehicleActivity.class);
    	i.putStringArrayListExtra(ReservationVehicleActivity.EXTRA_CITIES_LIST, mCityList);
    	startActivity(i);      	
    }
    
    private void doActionEnterQueryStore() {
    	Intent i = new Intent(HomePageActivity.this, QueryStoreActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterMemberManagement() {
    	Intent i = new Intent(HomePageActivity.this, MemberManagementActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterOrderManagement() {
    	Intent i = new Intent(HomePageActivity.this, OrderManagementActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterAccountManagement() {
    	Intent i = new Intent(HomePageActivity.this, AccountManagementActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterCustomerService() {
    	Intent i = new Intent(HomePageActivity.this, CustomerServiceActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterMessageCenter() {
    	Intent i = new Intent(HomePageActivity.this, MessageCenterActivity.class);
    	startActivity(i);      	
    }
    
    private void doActionEnterMore() {
    	Intent i = new Intent(HomePageActivity.this, MoreActivity.class);
    	startActivity(i);      	
	}    
    
    // private method section
    private Location getGpsLocation() {
    	Location location = null;
		if (mConnection != null) {
			location = mConnection.getGpsLocation();
		}
		return location;
    }    
    
    private void loadApps() {
    	mAppsList = new ArrayList<TextView>();
    	final LayoutInflater mFactory = LayoutInflater.from(this);
    	TextView tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.quick_order_selector), null, null);
    	tv.setText(R.string.quick_order_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (TEST_MAP_SECTION_CODE) {
        			Store s1 = new Store();
        			s1.mId = "123";
        			s1.mLongitude = 116.357428;
        			s1.mLatitude = 39.90923;
        			s1.mName = "store1";
        			s1.mAddress = "address1";
        			
        			Store s2 = new Store();
        			s2.mId = "456";
        			s2.mLongitude = 116.397428;
        			s2.mLatitude = 39.90923;
        			s2.mName = "store2";
        			s2.mAddress = "address2";
        			
        			Store s3 = new Store();
        			s3.mId = "456";
        			s3.mLongitude = 116.437428;
        			s3.mLatitude = 39.90923;
        			s3.mName = "store3";
        			s3.mAddress = "address3";
        			
        			mStoreList = new ArrayList<Store>();
        			mStoreList.add(s1);
        			mStoreList.add(s2);
        			mStoreList.add(s3);
        			doActionEnterQuickOrder();    				
    			}

    			if (ZappleRentalApp.getApplication().isLogined()) {
    				Location location = getGpsLocation();
    				if (location != null) {
    					doActionGetStore();    					
    				} else {
    					doActionNoticeLocationPrompt();
    				}    									
    			} else {
    				doActionNoticeLoginPrompt();
    			}				
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.reservation_vehicle_selector), null, null);
    	tv.setText(R.string.reservation_vehicle_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterReservationVehicle();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.query_store_selector), null, null);
    	tv.setText(R.string.query_store_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterQueryStore();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.member_management_selector), null, null);
    	tv.setText(R.string.member_management_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterMemberManagement();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.order_management_selector), null, null);
    	tv.setText(R.string.order_management_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterOrderManagement();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.account_management_selector), null, null);
    	tv.setText(R.string.account_management_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterAccountManagement();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);

    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.customer_service_selector), null, null);
    	tv.setText(R.string.customer_service_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterCustomerService();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);
    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.message_center_selector), null, null);
    	tv.setText(R.string.message_center_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterMessageCenter();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});
    	mAppsList.add(tv);
    	tv = (TextView) mFactory.inflate(R.layout.grid_cell_item, null, false);
    	tv.setCompoundDrawablesWithIntrinsicBounds(null, this.getResources().getDrawable(R.drawable.more_selector), null, null);
    	tv.setText(R.string.more_cell_item_name);
    	tv.setOnClickListener(new View.OnClickListener() {			
    		public void onClick(View v) {
    			if (ZappleRentalApp.getApplication().isLogined()) {
    				doActionEnterMore();					
    			} else {
    				doActionNoticeLoginPrompt();
    			}								
    		}
    	});      
    	mAppsList.add(tv); 
	}    
  
	// private class section  
	private class GetStoreRunner implements Runnable {
		public void run() {
			// get store action
			String startIndex = "0";
			String endIndex = "100";
			Location location = getGpsLocation();
			if (location != null) {
				try {
					mStoreList = WebServiceController.getStores(
							String.valueOf(location.getLongitude()), 
							String.valueOf(location.getLatitude()), 
							String.valueOf(location.getAltitude()), 
							null, 
							startIndex, 
							endIndex);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}		
			
			if (DEBUG) Log.d(TAG, "GetStoreRunner->" + mStoreList);
			
			// Deal with get store result
			Message msg = new Message();
			if (mStoreList != null) {
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
    
	private class AppsAdapter extends BaseAdapter {
		public AppsAdapter() {
		}
	
		public View getView(int position, View convertView, ViewGroup parent) {
			return mAppsList.get(position);
		}
	
	
		public final int getCount() {
			return mAppsList.size();
		}
	
		public final Object getItem(int position) {
			return mAppsList.get(position);
		}
	
		public final long getItemId(int position) {
			return position;
		}
	}    
	
	private class UpgradeAdvertiseTask extends AsyncTask<Void, Void, AdvertiseUpgradeResult> {

		@Override
		protected AdvertiseUpgradeResult doInBackground(Void... params) {
			AdvertiseUpgradeResult result = null;
			String currentAdvertiseVersion = mSharedPreferences.getString(AdvertiseUpgradeResult.ADVERTISE_UPGRADE_RESULT_VERSION, "");;
			try {
				result = WebServiceController.advertiseUpgrade(currentAdvertiseVersion);
			} catch (Exception e) {
				e.printStackTrace();
				if (DEBUG) Log.d(TAG, "UpgradeAdvertiseTask." + e);
			}
			return result;
		}
		
        @Override
        protected void onPostExecute(AdvertiseUpgradeResult result) {
        	if (result == null 
        			|| TextUtils.isEmpty(result.mAdvertiseVersion) 
        			|| TextUtils.isEmpty(result.mAdvertiseData)) {
        		return;
        	} else {
        		Editor e = mSharedPreferences.edit();
        		e.putString(AdvertiseUpgradeResult.ADVERTISE_UPGRADE_RESULT_VERSION, result.mAdvertiseVersion);
        		e.putString(AdvertiseUpgradeResult.ADVERTISE_UPGRADE_RESULT_DATA, result.mAdvertiseData);
        	}
        	
        	mAdvertisementTextView.setText(result.mAdvertiseData);
        }
	}
	
	private class GetCitiesTask extends AsyncTask<Void, Void, GetCitiesResult> {

		@Override
		protected GetCitiesResult doInBackground(Void... params) {
			GetCitiesResult result = null;
			try {
				result = WebServiceController.getCities();
			} catch (Exception e) {
				e.printStackTrace();
				if (DEBUG) Log.d(TAG, "UpgradeAdvertiseTask." + e);
			}
			return result;
		}
		
        @Override
        protected void onPostExecute(GetCitiesResult result) {
        	if (result == null 
        			|| result.mCitiesCount == 0) {
        		return;
        	} else {
        		mCityList = result.mCityList;
        	}
        }
	}	
	
	private class GpsConnection implements ServiceConnection {
        private GpsService mService;

        public Location getGpsLocation() {
        	Log.d(TAG, "getGpsInfo.mService." + mService);
//        	mHandler;
        	if (mService != null) {
        		return mService.getLocation(HomePageActivity.this);
        	}        	
        	return null;
        }
        
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "Connected from GpsService");
			mService = ((GpsService.MyBinder) service).getService();
			mService.setUiHandler(mHandler);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(TAG, "Disconnected from GpsService");
		}
	}	
}