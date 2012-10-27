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

import com.zapple.evshare.EvShareApp;
import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.PersonalInfo;
import com.zapple.evshare.service.account.Authenticator;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AccountLoginActivity extends Activity {
	private static final String TAG = AccountLoginActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
	private static final int LOGIN_SUCCESS = 0;
	private static final int LOGIN_FAILURE = 1;
	private static final int LOGIN_UPDATE = 2;
	private static final int LOGIN_NETWORK_FAILURE = 3;
	private static final int LOGIN_USER_PSW_ERROR = 4;
    
	private EditText mAccountEditText;
	private EditText mPasswordEditText;
	private CheckBox mPushCheckBox;
	private Button mLoginButton;
	private Button mBackButton;
	private SharedPreferences mSharedPreferences;
	
	private AccountAuthenticatorResponse mAuthenticatorResponse;

    private ProgressDialog mLoginDialog;
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
                case LOGIN_SUCCESS: {
                     Toast.makeText(AccountLoginActivity.this, R.string.login_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                     EvShareApp.getApplication().setLogined(true);
                     if (mAuthenticatorResponse != null) {
                    	 
                         final Account account = new Account(mAccountEditText.getText().toString().trim(),
                        		 Authenticator.ACCOUNT_TYPE);
                         AccountManager.get(AccountLoginActivity.this).addAccountExplicitly(
                                     account, mPasswordEditText.getText().toString().trim(), null);                    	 
                    	 mAuthenticatorResponse.onResult(null); 
                    	 mAuthenticatorResponse = null;

                     } else {
                    	 doActionEnterMain();
                     }
                     setResult(RESULT_OK);
                     finish();
                    break;
                }
                case LOGIN_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.login_failure_label);
                	} else {
                		failureReason = (String) msg.obj;
                	}
                    Toast.makeText(AccountLoginActivity.this, failureReason, 
                    		Toast.LENGTH_SHORT).show();
                    EvShareApp.getApplication().setLogined(false);
                    break;
                }
                case LOGIN_UPDATE: {
                    Toast.makeText(AccountLoginActivity.this, (String) msg.obj, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }
                case LOGIN_NETWORK_FAILURE: {
                    Toast.makeText(AccountLoginActivity.this, (String) msg.obj, 
                    		Toast.LENGTH_SHORT).show();
                    break;
                }
                case LOGIN_USER_PSW_ERROR: {
                    Toast.makeText(AccountLoginActivity.this, (String) msg.obj, 
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
		setContentView(R.layout.account_login_layout);
		
		// find view section
		mAccountEditText = (EditText) findViewById(R.id.account_edit_text);
		mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);
		mPushCheckBox = (CheckBox) findViewById(R.id.push_check_box);
		mLoginButton = (Button) findViewById(R.id.login_button);
		mBackButton = (Button) findViewById(R.id.back_button);
				
		mAccountEditText.addTextChangedListener(mTextWatcher);
		mPasswordEditText.addTextChangedListener(mTextWatcher);

		mLoginButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionLogin();
			}
		});
		mBackButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				finish();
			}
		});
		
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(AccountLoginActivity.this);
        String account = mSharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
        String password = mSharedPreferences.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
        
//        if (DEBUG) WLog.d(TAG, "onCreate.account." + account + ", password." + password);
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
        	mAccountEditText.setText(account);
        	mPasswordEditText.setText(password);
        }		
        // just for test
//    	mAccountEditText.setText("fanruibing@gmail.com");
//    	mPasswordEditText.setText("!@#$asdf9");
//    	mAccountEditText.setText("897361589@qq.com");
//    	mPasswordEditText.setText("a123456");
    	mAccountEditText.setText("ineedtwo@126.com");
    	mPasswordEditText.setText("wangle123");    	
    	
        // Set aside incoming AccountAuthenticatorResponse, if there was any
		mAuthenticatorResponse =
            getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);	   
		
		account = getIntent().getStringExtra(Constants.ACCOUNT_NAME_EXTRA);
		password = getIntent().getStringExtra(Constants.ACCOUNT_PASSWORD_EXTRA);
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
        	mAccountEditText.setText(account);
        	mPasswordEditText.setText(password);
        	doActionLogin();
        }		
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_login, menu);
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
	
    @Override
    public void finish() {
        super.finish();
    }	
	
	// private method do action section
    private void doActionLogin() {
		if (mLoginDialog != null && mLoginDialog.isShowing()) {
			mLoginDialog.dismiss();
		}    	
        mLoginDialog = new ProgressDialog(this);
        mLoginDialog.setTitle(R.string.login_label);
        mLoginDialog.setMessage(getString(R.string.logining_prompt));
        mLoginDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mLoginThread != null && mLoginThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mLoginDialog.setOwnerActivity(this);
        mLoginDialog.show();

        //TODO: do action login
        LoginRunner loginRunner = new LoginRunner();
        mLoginThread = new Thread(loginRunner);
        mLoginThread.start();
    }	
	
    private void doActionEnterMain() {
        Intent intent = new Intent(this, MainTabActivity.class);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "doActionEnterMain->", e);
        }
    }
    
    private void doActionEnterRetrievePassword() {
        Intent intent = new Intent(AccountLoginActivity.this, RetrievePasswordActivity.class);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "doActionEnterRetrievePassword->", e);
        }
    }    
    
	// private method section
    private void setFieldStatus() {
        if (!TextUtils.isEmpty(mAccountEditText.getText().toString().trim())
                && !TextUtils.isEmpty(mPasswordEditText.getText().toString().trim())) {
            mLoginButton.setEnabled(true);
        } else {
            mLoginButton.setEnabled(false);
        }
    }
    
	// private class section    
	private class LoginRunner implements Runnable {

		public void run() {
			// Login action
			LoginResult loginResult = null;
			try {
				loginResult = WebServiceController.login(mAccountEditText.getText().toString().trim(), mPasswordEditText.getText().toString().trim());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "LoginRunner->" + loginResult);
			
			// Deal with upload result
			Message msg = new Message();
			if (loginResult != null && TextUtils.isEmpty(loginResult.mLoginResult)) {
				if (DEBUG) Log.d(TAG, "LoginRunner->login success");
				Editor e = mSharedPreferences.edit();
				
				if (mPushCheckBox.isChecked()) {       
					// remember account data
            		e.putString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, mAccountEditText.getText().toString().trim());
            		e.putString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, mPasswordEditText.getText().toString().trim());
            	} else {
            		// clear account data: account name and password
            		e.putString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, "");
            		e.putString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, "");            		
            	}				
          		
        		e.putLong(LoginResult.LOGIN_RESULT_TOTAL_SCORES_KEY, loginResult.mTotalScores);
        		e.putLong(LoginResult.LOGIN_RESULT_EXPIRY_DATE_KEY, loginResult.mExpiryDate);
        		e.putString(LoginResult.LOGIN_RESULT_ACCOUNT_DATA_KEY, loginResult.mAccountData);
        		
        		if (loginResult.mPersonalInfo != null) {
        			e.putString(PersonalInfo.PERSONAL_INFO_NAME_KEY, loginResult.mPersonalInfo.mName);
        			e.putString(PersonalInfo.PERSONAL_INFO_USER_RANK_KEY, loginResult.mPersonalInfo.mUserRank);
        			e.putString(PersonalInfo.PERSONAL_INFO_ID_TYPE_KEY, loginResult.mPersonalInfo.mIdType);
        			e.putString(PersonalInfo.PERSONAL_INFO_ID_KEY, loginResult.mPersonalInfo.mId);
        			e.putString(PersonalInfo.PERSONAL_INFO_PHONE_NUMBER_KEY, loginResult.mPersonalInfo.mPhoneNumber);
        			e.putString(PersonalInfo.PERSONAL_INFO_EMAIL_ADDRESS_KEY, loginResult.mPersonalInfo.mEmailAddress);
        		}
        		
        		e.putString(LoginResult.LOGIN_RESULT_ACCOUNT_DATA_KEY, loginResult.mAccountData);
        		e.commit();
        		msg.what = LOGIN_SUCCESS;
			} else {
				msg.what = LOGIN_FAILURE;
				if (loginResult != null) {
					msg.obj = loginResult.mLoginResult;
					if (DEBUG) Log.d(TAG, "LoginRunner->login failure." + loginResult.mLoginResult);
				}
			}
			if (mLoginDialog != null && mLoginDialog.isShowing()) {
				mLoginDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
}