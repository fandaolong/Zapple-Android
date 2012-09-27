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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.zapple.rental.R;
import com.zapple.rental.ZappleRentalApp;
import com.zapple.rental.data.GetCitiesResult;
import com.zapple.rental.data.Store;
import com.zapple.rental.transaction.WebServiceController;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * This activity providers reservation vehicle feature.
 */
public class ReservationVehicleActivity extends Activity {
	private static final String TAG = "ReservationVehicleActivity";
	public static final String EXTRA_CITIES_LIST = "extra_cities_list";
    private final static long MINUTES_MAX_30 = 30*60*1000;
	private static final boolean DEBUG = true;
	private static final int QUERY_SUCCESS = 2;
	private static final int QUERY_FAILURE = 3;	
	
	private Context mContext;
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
    
	private EditText mTakeVehicleCityEditText;
	private Spinner mTakeVehicleCitySpinner;
	private EditText mTakeVehicleStoreEditText;
	private Spinner mTakeVehicleStoreSpinner;	
	private Button mTakeVehicleDateButton;
	private Button mTakeVehicleTimeButton;
//	
//	private EditText mReturnVehicleCityEditText;
//	private Spinner mReturnVehicleCitySpinner;
//	private EditText mReturnVehicleStoreEditText;
//	private Spinner mReturnVehicleStoreSpinner;	
//	private Button mReturnVehicleDateButton;
//	private Button mReturnVehicleTimeButton;	
	private Button mNextButton;
		
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
    
	private ProgressDialog mUploadDialog;
	private Thread mLoginThread = null;
	private TextWatcher mTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			setFieldStatus();
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
	};
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
	            case QUERY_SUCCESS: {
	            	loadData();
	                Toast.makeText(mContext, R.string.get_store_success_label, 
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
	               Toast.makeText(mContext, failureReason, 
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
	
//    private DatePickerDialog.OnDateSetListener mReturnDateSetListener =
//            new DatePickerDialog.OnDateSetListener() {
//
//                public void onDateSet(DatePicker view, int year, int monthOfYear,
//                        int dayOfMonth) {
//                    mReturnYear = year;
//                    mReturnMonth = monthOfYear + 1;
//                    mReturnDay = dayOfMonth;
//                    mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
//                }
//            };

//    private TimePickerDialog.OnTimeSetListener mReturnTimeSetListener =
//            new TimePickerDialog.OnTimeSetListener() {
//
//                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                    mReturnHour = hourOfDay;
//                    mReturnMinute = minute;
//                    mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
//                }
//            };            
            
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
		setContentView(R.layout.activity_reservation_vehicle);
		
		mContext = this;
		// find view section
		mTakeVehicleCityEditText = (EditText) findViewById(R.id.take_vehicle_city_edit_text);
		mTakeVehicleCitySpinner = (Spinner) findViewById(R.id.take_vehicle_city_spinner);
		mTakeVehicleStoreEditText = (EditText) findViewById(R.id.take_vehicle_store_edit_text);
		mTakeVehicleStoreSpinner = (Spinner) findViewById(R.id.take_vehicle_store_spinner);
		mTakeVehicleDateButton = (Button) findViewById(R.id.take_vehicle_date_button);
		mTakeVehicleTimeButton = (Button) findViewById(R.id.take_vehicle_time_button);
		
//		mReturnVehicleCityEditText = (EditText) findViewById(R.id.return_vehicle_city_edit_text);
//		mReturnVehicleCitySpinner = (Spinner) findViewById(R.id.return_vehicle_city_spinner);
//		mReturnVehicleStoreEditText = (EditText) findViewById(R.id.return_vehicle_store_edit_text);
//		mReturnVehicleStoreSpinner = (Spinner) findViewById(R.id.return_vehicle_store_spinner);
//		mReturnVehicleDateButton = (Button) findViewById(R.id.return_vehicle_date_button);
//		mReturnVehicleTimeButton = (Button) findViewById(R.id.return_vehicle_time_button);

		mNextButton = (Button) findViewById(R.id.next_button);	
		
		mTakeVehicleCityEditText.setVisibility(View.GONE);
		mTakeVehicleStoreEditText.setVisibility(View.GONE);
//		mTakeVehicleCityEditText.addTextChangedListener(mTextWatcher);
//		mTakeVehicleStoreEditText.addTextChangedListener(mTextWatcher);
//		mReturnVehicleCityEditText.addTextChangedListener(mTextWatcher);
//		mReturnVehicleStoreEditText.addTextChangedListener(mTextWatcher);		
		
		mTakeVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(ReservationVehicleActivity.this,
                        mTakeDateSetListener,
                        mTakeYear, mTakeMonth, mTakeDay);
				dpd.show();
			}
		});
		
		mTakeVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(ReservationVehicleActivity.this,
                        mTakeTimeSetListener, mTakeHour, mTakeMinute, false);
				tpd.show();
			}
		});	
		
//		mReturnVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
//			public void onClick(View v) {
//				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
//				DatePickerDialog dpd = new DatePickerDialog(ReservationVehicleActivity.this,
//                        mReturnDateSetListener,
//                        mReturnYear, mReturnMonth, mReturnDay);
//				dpd.show();			
//			}
//		});
//		
//		mReturnVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
//			public void onClick(View v) {
//				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
//				TimePickerDialog tpd = new TimePickerDialog(ReservationVehicleActivity.this,
//						mReturnTimeSetListener, mReturnHour, mReturnMinute, false);
//				tpd.show();				
//			}
//		});		
		
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
        mTakeVehicleDateButton.setText(mTakeYear + "-" + mTakeMonth + "-" + mTakeDay);
        mTakeVehicleTimeButton.setText(mTakeHour + ":" + mTakeMinute);
        
		Intent intent = getIntent();
		if (intent != null) {
			mCityList = intent.getStringArrayListExtra(EXTRA_CITIES_LIST);
		}		

		doActionGetStore();
		mNextButton.setEnabled(true);
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
	private void doActionEnterStoreDetail() {
		Intent intent = new Intent(ReservationVehicleActivity.this, StoreDetailActivity.class);
		intent.putExtra(StoreDetailActivity.FROM_WHERE_EXTRA, StoreDetailActivity.FROM_RESERVATION_VEHICLE);
		if (mSelectedStore != null) {
			intent.putExtra(StoreDetailActivity.STORE_ID_EXTRA, mSelectedStore.mId);
		}		
		final Calendar c = Calendar.getInstance();
		c.set(mTakeYear, mTakeMonth-1, mTakeDay, mTakeHour, mTakeMinute);
		long takeVehicleDate = c.getTimeInMillis();
		long currentTimeMillis = System.currentTimeMillis();
		if ((takeVehicleDate - currentTimeMillis) > MINUTES_MAX_30 || (takeVehicleDate - currentTimeMillis) < 0) {
			Toast.makeText(mContext, R.string.threshold_30_minutes, Toast.LENGTH_SHORT).show();
			return;
		}
//		c.set(mTakeYear, mTakeMonth-1, mTakeDay, mTakeHour, mTakeMinute);
//		long returnVehicleDate = c.getTimeInMillis();
        String takeVehicleDateString = String.valueOf(takeVehicleDate);
        intent.putExtra(StoreDetailActivity.TAKE_VEHICLE_DATE_EXTRA, takeVehicleDateString);
		intent.putExtra(StoreDetailActivity.STORE_NAME_EXTRA, mSelectedStoreName);
		startActivity(intent);			
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
			
	// private method section
	private void setFieldStatus() {		
		if (!TextUtils.isEmpty(mTakeVehicleCityEditText.getText().toString().trim())
				&& !TextUtils.isEmpty(mTakeVehicleStoreEditText.getText().toString().trim())) {
			mNextButton.setEnabled(true);
		} else {
			mNextButton.setEnabled(false);
		}
	}	
	
	private void loadData() {
		if (mCityList != null) {
	        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, mCityList);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        mTakeVehicleCitySpinner.setAdapter(adapter);
	        mTakeVehicleCitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mSelectedCity = mCityList.get(arg2);
					mStoreList = mCityStoreHashMap.get(mSelectedCity);
					mStoreNameList = mCityStoreNameHashMap.get(mSelectedCity);				
					if (mStoreNameList == null || mStoreNameList.size() == 0) {
						mTakeVehicleStoreSpinner.setAdapter(null);
						mSelectedStoreName = null;
						mSelectedStore = null;								
					} else {
						ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, mStoreNameList);					
						mTakeVehicleStoreSpinner.setAdapter(adapter);
						mSelectedStoreName = mStoreNameList.get(0);
						mSelectedStore = mStoreList.get(0);						
					}

//					Toast.makeText(mContext, "Spinner1: position=" + arg2 + " id=" + arg3, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					Toast.makeText(mContext, "onNothingSelected", Toast.LENGTH_SHORT).show();
				}
			});	
	        mTakeVehicleStoreSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					mSelectedStoreName = mStoreNameList.get(arg2);
					mSelectedStore = mStoreList.get(arg2);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					
				}
	        });
		}		
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
}
