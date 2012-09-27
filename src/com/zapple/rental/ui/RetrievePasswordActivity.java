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
import com.zapple.rental.transaction.WebServiceController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * This activity providers retrieve password feature.
 */
public class RetrievePasswordActivity extends Activity {
	private static final String TAG = "RetrievePasswordActivity";
	private static final boolean DEBUG = true;
	private static final int RETRIEVE_PASSWORD_SUCCESS = 0;
	private static final int RETRIEVE_PASSWORD_FAILURE = 1;
    
	private TextView mTitleTextView;
	private EditText mRetrieveByMobileEditText;
	private Button mRetrieveByMobileButton;
	private EditText mRetrieveByEmailEditText;
	private Button mRetrieveByEmailButton; 
	private EditText mRetrieveByPhoneEditText;
	private Button mRetrieveByPhoneButton;	

	private ProgressDialog mPasswordRetrievalDialog;
	private Thread mPasswordRetrievalThread = null;
    private TextWatcher mTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
//            setFieldStatus();
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
				case RETRIEVE_PASSWORD_SUCCESS:{
					Toast.makeText(RetrievePasswordActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();					
					break;
				}
				case RETRIEVE_PASSWORD_FAILURE:{
					Toast.makeText(RetrievePasswordActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();					
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
		setContentView(R.layout.activity_retrieve_password);
		
		// find view section
		mTitleTextView = (TextView) findViewById(R.id.title_text_view);
		mRetrieveByMobileEditText = (EditText) findViewById(R.id.retrieve_by_mobile_edit_text);
		mRetrieveByMobileButton = (Button) findViewById(R.id.retrieve_by_mobile_button);
		mRetrieveByEmailEditText = (EditText) findViewById(R.id.retrieve_by_email_edit_text);
		mRetrieveByEmailButton = (Button) findViewById(R.id.retrieve_by_email_button);
		mRetrieveByPhoneEditText = (EditText) findViewById(R.id.retrieve_by_phone_edit_text);
		mRetrieveByPhoneButton = (Button) findViewById(R.id.retrieve_by_phone_button);
		
		mRetrieveByMobileEditText.addTextChangedListener(mTextWatcher);
		mRetrieveByEmailEditText.addTextChangedListener(mTextWatcher);
		mRetrieveByPhoneEditText.addTextChangedListener(mTextWatcher);
		
		mRetrieveByMobileButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionRetrieveByMobile();
			}
		});
		mRetrieveByEmailButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionRetrieveByEmail();
			}
		});
		mRetrieveByPhoneButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionRetrieveByPhone();
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
	private void doActionRetrieveByMobile() {
		doActionPasswordRetrieval();
	}
	
	private void doActionRetrieveByEmail() {
		doActionPasswordRetrieval();
	}

	private void doActionRetrieveByPhone() {
		
	}
	
	private void doActionPasswordRetrieval() {
		if (mPasswordRetrievalDialog != null && mPasswordRetrievalDialog.isShowing()) {
			mPasswordRetrievalDialog.dismiss();
			if (DEBUG) {
				Log.d(TAG, "markTodo.dismiss.");
			}
		}		
		mPasswordRetrievalDialog = new ProgressDialog(this);
//		mPasswordRetrievalDialog.setTitle(R.string.login_label);
//		mPasswordRetrievalDialog.setMessage(getString(R.string.logining_prompt));
		mPasswordRetrievalDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mPasswordRetrievalThread != null && mPasswordRetrievalThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mPasswordRetrievalDialog.show();
		
		
		PasswordRetrievalRunner uploadRunner = new PasswordRetrievalRunner();
		mPasswordRetrievalThread = new Thread(uploadRunner);
		mPasswordRetrievalThread.start();		
	}	
	
	// private method section
	
	// private class section    
	private class PasswordRetrievalRunner implements Runnable {

		public void run() {
			// upload action
			String result = null;
			String phoneNumber = mRetrieveByMobileEditText.getText().toString().trim();
			String emailAddress = mRetrieveByEmailEditText.getText().toString().trim();
			
			try {
				result = WebServiceController.passwordRetrieval(phoneNumber, emailAddress);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "PasswordRetrievalRunner->e." + e);
			}
			
			
			if (DEBUG) {
				Log.d(TAG, "PasswordRetrievalRunner->run->result:" + result);
			}			
			
			// Deal with password retrieval result
			Message msg = new Message();
			msg.what = RETRIEVE_PASSWORD_SUCCESS;
			msg.obj = result;
			mHandler.sendMessage(msg);
			if (mPasswordRetrievalDialog != null && mPasswordRetrievalDialog.isShowing()) {
				mPasswordRetrievalDialog.dismiss();
			}
		}		
	}
}
