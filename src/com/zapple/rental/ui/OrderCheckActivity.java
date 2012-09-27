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

import java.util.Date;
import java.util.List;

import com.zapple.rental.R;
import com.zapple.rental.data.LoginResult;
import com.zapple.rental.data.Score;
import com.zapple.rental.data.SubmitOrder;
import com.zapple.rental.transaction.WebServiceController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This activity providers add choose service, such as: gps, seat for child, feature.
 */
public class OrderCheckActivity extends Activity {
	private static final String TAG = "OrderCheckActivity";
	private static final boolean DEBUG = true;
	
	public static final String SUBMIT_ORDER_EXTRA = "submit_order_extra";
	
	private static final int SUBMIT_ORDER_SUCCESS = 0;
	private static final int SUBMIT_ORDER_FAILURE = 1;	
	
	private TextView mMemberNameTextView;
	private TextView mRemainingTextView;
	private TextView mModelTextView;
	private TextView mBrandTextView;
	private TextView mTakeStoreNameTextView;
	private TextView mTakeVehicleTimeTextView;
	private TextView mReturnStoreNameTextView;
	private TextView mReturnVehicleTimeTextView;
	private TextView mVehicleFeeTextView;
	private TextView mAddedValueFeeTextView;
	private TextView mOtherFeeTextView;
	private TextView mTotalAmountTextView;
	private Button mPreviousButton;
	private Button mSubmitOrderButton;
	
	private SubmitOrder mSubmitOrder;
	private Context mContext;
    private ProgressDialog mSubmitOrderDialog;
    private Thread mSubmitOrderThread = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case SUBMIT_ORDER_SUCCESS: {
//                	mListView.setAdapter(new ScoreListArrayAdapter(mContext));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							ScoreListItem listItem = (ScoreListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                     Toast.makeText(mContext, R.string.submit_order_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                     finish();
                    break;
                }
                case SUBMIT_ORDER_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.submit_order_failure_label);
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
		setContentView(R.layout.order_check_layout);
		
		mContext = this;
		// find view section
		mMemberNameTextView = (TextView) findViewById(R.id.member_name_text_view);
		mRemainingTextView = (TextView) findViewById(R.id.remaining_text_view);
		mModelTextView = (TextView) findViewById(R.id.model_text_view);
		mBrandTextView = (TextView) findViewById(R.id.brand_text_view);
		mTakeStoreNameTextView = (TextView) findViewById(R.id.take_store_name_text_view);
		mTakeVehicleTimeTextView = (TextView) findViewById(R.id.take_vehicle_time_text_view);
		mReturnStoreNameTextView = (TextView) findViewById(R.id.return_store_name_text_view);
		mReturnVehicleTimeTextView = (TextView) findViewById(R.id.return_vehicle_time_text_view);
		mVehicleFeeTextView = (TextView) findViewById(R.id.vechicle_fee_text_view);
		mAddedValueFeeTextView = (TextView) findViewById(R.id.added_value_fee_text_view);
		mOtherFeeTextView = (TextView) findViewById(R.id.other_fee_text_view);
		mTotalAmountTextView = (TextView) findViewById(R.id.total_amount_text_view);		
		mPreviousButton = (Button) findViewById(R.id.previous_button);
		mSubmitOrderButton = (Button) findViewById(R.id.submit_button);

		mPreviousButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				finish();
			}
		});	
		
		mSubmitOrderButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionSubmitOrder();
			}
		});	
		
		Intent intent = getIntent();
		if (intent != null) {
			mSubmitOrder = intent.getParcelableExtra(SUBMIT_ORDER_EXTRA);
		}		
		
		if (mSubmitOrder != null) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	        String account = sharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, "");
	        mSubmitOrder.mUserName = account;
			mMemberNameTextView.setText(account);
//			mRemainingTextView.setText(mSubmitOrder.);
			mModelTextView.setText(mSubmitOrder.mModel);
			mBrandTextView.setText(mSubmitOrder.mBrand);
			mTakeStoreNameTextView.setText(mSubmitOrder.mTakeStoreName);
			mTakeVehicleTimeTextView.setText((new Date(Long.parseLong(mSubmitOrder.mTakeVehicleDate))).toLocaleString());
			mReturnStoreNameTextView.setText(mSubmitOrder.mReturnStoreName);
			mReturnVehicleTimeTextView.setText((new Date(Long.parseLong(mSubmitOrder.mReturnVehicleDate))).toLocaleString());
			mVehicleFeeTextView.setText(mSubmitOrder.mVehicleFee);
			mAddedValueFeeTextView.setText(mSubmitOrder.mAddedFee);
			mOtherFeeTextView.setText(mSubmitOrder.mOtherFee);
			long amount = 0;
			try {
				amount = Long.parseLong(mSubmitOrder.mVehicleFee) + Long.parseLong(mSubmitOrder.mAddedFee) + Long.parseLong(mSubmitOrder.mAddedFee);	
			} catch (Exception e) {
				
			}			
			mTotalAmountTextView.setText(String.valueOf(amount));			
		}
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
    private void doActionSubmitOrder() {
		if (mSubmitOrderDialog != null && mSubmitOrderDialog.isShowing()) {
			mSubmitOrderDialog.dismiss();
		}    	
        mSubmitOrderDialog = new ProgressDialog(this);
        mSubmitOrderDialog.setTitle(R.string.submit_order_label);
        mSubmitOrderDialog.setMessage(getString(R.string.submitting_order_prompt));
        mSubmitOrderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mSubmitOrderThread != null && mSubmitOrderThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mSubmitOrderDialog.setOwnerActivity(this);
        mSubmitOrderDialog.show();

        //TODO: do action submit order
        SubmitOrderRunner submitOrderRunner = new SubmitOrderRunner();
        mSubmitOrderThread = new Thread(submitOrderRunner);
        mSubmitOrderThread.start();
    }
		
	// private method section

	
	// private class section
	private class SubmitOrderRunner implements Runnable {

		public void run() {
			// submit order action
			String submitOrderResult = null;
			if (mSubmitOrder != null) {
				try {
					submitOrderResult = WebServiceController.submitOrder(mSubmitOrder);
				} catch (Exception e) {
					e.printStackTrace();
					submitOrderResult = getString(R.string.submit_order_failure_label);
				}					
			}
			
			if (DEBUG) Log.d(TAG, "SubmitOrderRunner->" + submitOrderResult);
			
			// Deal with upload result
			Message msg = new Message();
			if (TextUtils.isEmpty(submitOrderResult)) {
				if (DEBUG) Log.d(TAG, "SubmitOrderRunner->submitOrder success");
        		msg.what = SUBMIT_ORDER_SUCCESS;
			} else {
				msg.what = SUBMIT_ORDER_FAILURE;
				if (DEBUG) Log.d(TAG, "SubmitOrderRunner->submitOrder failure.");
			}
			if (mSubmitOrderDialog != null && mSubmitOrderDialog.isShowing()) {
				mSubmitOrderDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
}