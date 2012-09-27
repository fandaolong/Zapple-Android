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

import com.zapple.rental.R;
import com.zapple.rental.data.LoginResult;
import com.zapple.rental.transaction.WebServiceController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ContinueOrderActivity extends Activity {
    private static final String TAG = "ContinueOrderActivity";
    private static final boolean DEBUG = true;
    
	private static final int CONTINUE_ORDER_SUCCESS = 0;
	private static final int CONTINUE_ORDER_FAILURE = 1;    
    
	private EditText mReturnVehicleDateEditText;
	private EditText mReturnVehicleTImeEditText;
	private Button mSubmitButton;
	
	private long mOrderId;
	private Context mContext;
	
	private ProgressDialog mContinueOrderDialog;
	private Thread mContinueOrderThread = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case CONTINUE_ORDER_SUCCESS:{
					Toast.makeText(mContext, R.string.continue_order_success_prompt, Toast.LENGTH_SHORT).show();		
					finish();
					break;
				}
				case CONTINUE_ORDER_FAILURE:{
					Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();					
					break;
				}				
			}
		}
	};	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.continue_order_layout);
        
        mContext = this;
        mReturnVehicleDateEditText = (EditText) findViewById(R.id.return_vehicle_date_edit_text);
        mReturnVehicleTImeEditText = (EditText) findViewById(R.id.return_vehicle_time_edit_text);
        mSubmitButton = (Button) findViewById(R.id.submit_button);
        
        mSubmitButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				doActionSubmit();
			}
		});
        
		Intent intent = getIntent();
		if (intent != null) {
			mOrderId = intent.getLongExtra(OrderDetailActivity.ORDER_ID_EXTRA, 0);
		}        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_quick_order_time, menu);
        return true;
    }   
    
    // private method do action section
	private void doActionSubmit() {
		if (mContinueOrderDialog != null && mContinueOrderDialog.isShowing()) {
			mContinueOrderDialog.dismiss();
		}		
		mContinueOrderDialog = new ProgressDialog(this);
//		mContinueOrderDialog.setTitle(R.string.submit_label);
		mContinueOrderDialog.setMessage(getString(R.string.continuing_prompt));
		mContinueOrderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mContinueOrderThread != null && mContinueOrderThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mContinueOrderDialog.setOwnerActivity(this);
		mContinueOrderDialog.show();		
		
		ContinueOrderRunner continueOrderRunner = new ContinueOrderRunner();
		mContinueOrderThread = new Thread(continueOrderRunner);
		mContinueOrderThread.start();		
	}

	// private method section
	
	// private class section
	private class ContinueOrderRunner implements Runnable {

		public void run() {
			// continue order action
			String result = null;
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
			String password = sp.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
			try {
				result = WebServiceController.continueOrder(userName, password, String.valueOf(mOrderId));
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "ContinueOrderRunner->success");
			}				
			
			// deal with continue order result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				msg.what = CONTINUE_ORDER_SUCCESS;								
				if (DEBUG) Log.d(TAG, "ContinueOrderRunner->failure.");				
			} else {
				if (DEBUG) Log.d(TAG, "ContinueOrderRunner->success");
        		msg.what = CONTINUE_ORDER_FAILURE;
        		msg.obj = result;
			}

			if (mContinueOrderDialog != null && mContinueOrderDialog.isShowing()) {
				mContinueOrderDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
}