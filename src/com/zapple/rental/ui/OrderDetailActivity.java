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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zapple.rental.R;
import com.zapple.rental.data.LoginResult;
import com.zapple.rental.data.Order;
import com.zapple.rental.transaction.WebServiceController;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity providers order detail feature.
 */
public class OrderDetailActivity extends Activity {
	private static final String TAG = "OrderDetailActivity";
	private static final boolean DEBUG = true;
	public static final String ORDER_ID_EXTRA = "order_id_extra";
	public static final String ORDER_NAME_EXTRA = "order_name_extra";
	public static final String ORDER_EXTRA = "order_extra";
	private static final int CANCEL_ORDER_SUCCESS = 0;
	private static final int CANCEL_ORDER_FAILURE = 1;
	
	private TextView mTitleTextView;
	
    private TextView mOrderStatusTextView;
    private TextView mVehicleModelTextView;
    private TextView mVehicleBrandTextView;
    private TextView mTakeVehicleStoreNameTextView;
    private TextView mTakeVehicleDateTextView;
    private TextView mReturnVehicleStoreNameTextView;
    private TextView mReturnVehicleDateTextView;
    private TextView mVehicleRentsTextView;
    private TextView mAddedServiceFeeTextView;
    private TextView mOtherFeeTextView;
    private TextView mTotalFeeTextView;
    
	private Button mCancelOrderButton;
	private Button mContinueOrderButton;
	
	private String mOrderId;
	private String mOrderName;
	private Order mOrder;
	private Context mContext;
	
	private ProgressDialog mCancelOrderDialog;
	private Thread mCancelOrderThread = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case CANCEL_ORDER_SUCCESS:{
					Toast.makeText(mContext, R.string.cancel_order_success_prompt, Toast.LENGTH_SHORT).show();		
					finish();
					break;
				}
				case CANCEL_ORDER_FAILURE:{
					Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();					
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
		setContentView(R.layout.order_detail_layout);
		
		mContext = this;
		// find view section
		mTitleTextView = (TextView) findViewById(R.id.title_text_view);
		
	    mOrderStatusTextView = (TextView) findViewById(R.id.order_status_value_text_view);
	    mVehicleModelTextView = (TextView) findViewById(R.id.model_text_view);
	    mVehicleBrandTextView = (TextView) findViewById(R.id.brand_text_view);
	    mTakeVehicleStoreNameTextView = (TextView) findViewById(R.id.take_store_name_text_view);
	    mTakeVehicleDateTextView = (TextView) findViewById(R.id.take_vehicle_time_text_view);
	    mReturnVehicleStoreNameTextView = (TextView) findViewById(R.id.return_store_name_text_view);
	    mReturnVehicleDateTextView = (TextView) findViewById(R.id.return_vehicle_time_text_view);
	    mVehicleRentsTextView = (TextView) findViewById(R.id.vehicle_fee_text_view);
	    mAddedServiceFeeTextView = (TextView) findViewById(R.id.added_value_fee_text_view);
	    mOtherFeeTextView = (TextView) findViewById(R.id.other_fee_text_view);
	    mTotalFeeTextView = (TextView) findViewById(R.id.order_total_amount_text_view);		
		
		mCancelOrderButton = (Button) findViewById(R.id.cancel_order_button);
		mContinueOrderButton = (Button) findViewById(R.id.continue_order_button);

		mCancelOrderButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionCancelOrder();
			}
		});	
		
		mContinueOrderButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionContinueOrder();
			}
		});		
		
		Intent intent = getIntent();
		if (intent != null) {
			mOrderId = intent.getStringExtra(ORDER_ID_EXTRA);
			mOrderName = intent.getStringExtra(ORDER_NAME_EXTRA);
			mOrder = intent.getParcelableExtra(ORDER_EXTRA);
		}
		mTitleTextView.setText(mOrderName);
		
		if (mOrder != null) {
		    mOrderStatusTextView.setText(mOrder.mOrderStatus);
		    mVehicleModelTextView.setText(mOrder.mVehicleModel);
		    mVehicleBrandTextView.setText(mOrder.mVehicleBrand);
		    mTakeVehicleStoreNameTextView.setText(mOrder.mTakeVehicleStoreName);
//		    Date data = new Date();
		    final Calendar c = Calendar.getInstance();
		    c.setTimeInMillis(mOrder.mTakeVehicleDate);
		    long current = System.currentTimeMillis();
		    if (DEBUG) Log.d(TAG, "currentTimeMillis." + current);
		    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		    String takeTime = sdf.format(c.getTime());
		    c.setTimeInMillis(mOrder.mReturnVehicleDate);
		    String returnTime = sdf.format(c.getTime());
		    mTakeVehicleDateTextView.setText(takeTime);
		    mReturnVehicleStoreNameTextView.setText(mOrder.mReturnVehicleStoreName);
		    mReturnVehicleDateTextView.setText(returnTime);
		    mVehicleRentsTextView.setText(mOrder.mVehicleRents);
		    mAddedServiceFeeTextView.setText(mOrder.mAddedServiceFee);
		    mOtherFeeTextView.setText(mOrder.mOtherFee);
		    // remove the YUAN unit.
		    float totalFee = Float.parseFloat(mOrder.mVehicleRents.substring(0, mOrder.mVehicleRents.length() -1)) + Float.parseFloat(mOrder.mAddedServiceFee.substring(0, mOrder.mAddedServiceFee.length() -1)) + Float.parseFloat(mOrder.mOtherFee.substring(0, mOrder.mOtherFee.length() -1));
		    mTotalFeeTextView.setText(String.valueOf(totalFee));		
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
	private void doActionCancelOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderDetailActivity.this);
        builder.setTitle(R.string.confirm_cancel_dialog_title)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelOrder();
				}            	
            })
            .show();		
	}
	
	private void cancelOrder() {
		if (mCancelOrderDialog != null && mCancelOrderDialog.isShowing()) {
			mCancelOrderDialog.dismiss();
		}		
		mCancelOrderDialog = new ProgressDialog(this);
//		mCancelOrderDialog.setTitle(R.string.submit_label);
//		mCancelOrderDialog.setMessage(getString(R.string.submitting_prompt));
		mCancelOrderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mCancelOrderThread != null && mCancelOrderThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mCancelOrderDialog.setOwnerActivity(this);
		mCancelOrderDialog.show();
		
		
		CancelOrderRunner cancelOrderRunner = new CancelOrderRunner();
		mCancelOrderThread = new Thread(cancelOrderRunner);
		mCancelOrderThread.start();		
	}	
	
	private void doActionContinueOrder() {
		Intent intent = new Intent(OrderDetailActivity.this, ContinueOrderActivity.class);
		intent.putExtra(OrderDetailActivity.ORDER_ID_EXTRA, mOrderId);
//		intent.putExtra(OrderDetailActivity.ORDER_NAME_EXTRA, orderName);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterOrderDetail->", e);
		}		
	}
			
	// private method section
	
	// private class section
	private class CancelOrderRunner implements Runnable {

		public void run() {
			// cancel order action
			String result = null;
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
			String password = sp.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
			try {
				result = WebServiceController.cancelOrder(userName, password, mOrderId);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "CancelOrderRunner->failure");
			}				
			
			// deal with cancel order result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				msg.what = CANCEL_ORDER_SUCCESS;								
				if (DEBUG) Log.d(TAG, "CancelOrderRunner->success.");
			} else {
				if (DEBUG) Log.d(TAG, "CancelOrderRunner->failure");
        		msg.what = CANCEL_ORDER_FAILURE;
        		msg.obj = result;				
			}

			if (mCancelOrderDialog != null && mCancelOrderDialog.isShowing()) {
				mCancelOrderDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
}