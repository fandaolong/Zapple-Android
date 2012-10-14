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

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.transaction.WebServiceController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This activity providers Advertisement feature.
 */
public class ComplaintSuggestionActivity extends Activity {
	private static final String TAG = "ComplaintSuggestionActivity";
	private static final boolean DEBUG = true;
	private static final int SUBMIT_SUCCESS = 0;
	private static final int SUBMIT_FAILURE = 1;
    
	private EditText mContentEditText;
	private EditText mMobileEditText;
	private EditText mEmailAddressEditText;
	private Button mSubmitButton;

	private Context mContext;
	private ProgressDialog mSubmitDialog;
	private Thread mSubmitThread = null;
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
				case SUBMIT_SUCCESS:{
					Toast.makeText(mContext, R.string.submit_success_prompt, Toast.LENGTH_SHORT).show();					
					break;
				}
				case SUBMIT_FAILURE:{
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
		setContentView(R.layout.complain_suggestion_layout);
		
		mContext = this;
		// find view section
		mContentEditText = (EditText) findViewById(R.id.content_edit_text);
		mMobileEditText = (EditText) findViewById(R.id.mobile_edit_text);
		mEmailAddressEditText = (EditText) findViewById(R.id.email_address_edit_text);
		mSubmitButton = (Button) findViewById(R.id.submit_button);
				
		mContentEditText.addTextChangedListener(mTextWatcher);
		mMobileEditText.addTextChangedListener(mTextWatcher);
		mSubmitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doActionSubmit();
			}			
		});
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
	private void doActionSubmit() {
		if (mSubmitDialog != null && mSubmitDialog.isShowing()) {
			mSubmitDialog.dismiss();
		}		
		mSubmitDialog = new ProgressDialog(this);
		mSubmitDialog.setTitle(R.string.submit_label);
		mSubmitDialog.setMessage(getString(R.string.submitting_prompt));
		mSubmitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mSubmitThread != null && mSubmitThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mSubmitDialog.setOwnerActivity(this);
		mSubmitDialog.show();
		
		
		SubmitRunner submitRunner = new SubmitRunner();
		mSubmitThread = new Thread(submitRunner);
		mSubmitThread.start();		
	}	
	
	// private method section
    private void setFieldStatus() {
        if (!TextUtils.isEmpty(mContentEditText.getText().toString().trim())
                && !TextUtils.isEmpty(mMobileEditText.getText().toString().trim())) {
        	mSubmitButton.setEnabled(true);
        } else {
        	mSubmitButton.setEnabled(false);
        }
    }	
	
	// private class section    
	private class SubmitRunner implements Runnable {

		public void run() {
			// submit action
			String result = null;
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
			String content = mContentEditText.getText().toString().trim();
			String phoneNumber = mMobileEditText.getText().toString().trim();
			String emailAddress = mEmailAddressEditText.getText().toString().trim();
			try {
				result = WebServiceController.submitComplaintAndSuggestion(userName, content, phoneNumber, emailAddress);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
			
			// deal with submit result
			Message msg = new Message();
			if (result != null) {
				if (DEBUG) Log.d(TAG, "SubmitRunner->success");
        		msg.what = SUBMIT_FAILURE;
        		msg.obj = result;
			} else {
				msg.what = SUBMIT_SUCCESS;								
				if (DEBUG) Log.d(TAG, "SubmitRunner->failure.");
			}

			if (mSubmitDialog != null && mSubmitDialog.isShowing()) {
				mSubmitDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
}
