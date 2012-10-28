/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.zapple.evshare.R;
import com.zapple.evshare.service.account.Authenticator;
import com.zapple.evshare.util.Constants;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This activity providers Account setup feature.
 */
public class AccountSetupActivity extends Activity implements OnClickListener {
	private static final String TAG = AccountSetupActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
	private static final int LOGIN_REQUEST_CODE = 1000;
	private static final int REGISTER_REQUEST_CODE = 1001;
    // NORMAL is the standard entry from the Zapple app; ACCOUNT_MANAGER is used when entering via
    // Settings -> Accounts
    public static final int FLOW_MODE_NORMAL = 0;
    public static final int FLOW_MODE_ACCOUNT_MANAGER = 1;	
	
	private Button mLoginButton;
	private Button mRegisterButton;
	private AccountAuthenticatorResponse mAuthenticatorResponse;
	private int mFlowMode;
	private Context mContext;
	
    /**
     * This generates setup data that can be used to start a self-contained account creation flow
     * for pop/imap accounts.
     */
    public static Intent actionSetupAccountIntent(Context context) {
        Intent i = new Intent(context, AccountSetupActivity.class);
        i.putExtra(Constants.FLOW_MODE_EXTRA, FLOW_MODE_ACCOUNT_MANAGER);
        return i;
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
		if (DEBUG) Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		mContext = this;
		setContentView(R.layout.account_setup_layout);
		
		// find view section
		mLoginButton = (Button) findViewById(R.id.login_button);
		mRegisterButton = (Button) findViewById(R.id.register_button);
		
		mLoginButton.setOnClickListener(this);
		mRegisterButton.setOnClickListener(this);
		
		setTitle(R.string.account_setup_title);
		
        // Set aside incoming AccountAuthenticatorResponse, if there was any
		mAuthenticatorResponse =
            getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);	
		
		mFlowMode = getIntent().getIntExtra(Constants.FLOW_MODE_EXTRA, FLOW_MODE_NORMAL);
		
		if (FLOW_MODE_NORMAL == mFlowMode) {
			Account[] accountArray = AccountManager.get(mContext).getAccountsByType(Authenticator.ACCOUNT_TYPE);
			if (accountArray != null && accountArray.length > 0) {
				Account account = accountArray[0];
				if (account != null) {
					String accountName = account.name;
					String password = AccountManager.get(mContext).getPassword(account);
					doActionEnterAccountLoginAuto(accountName, password);
					finish();
				}
			}
		} else {
			
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

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.login_button: {
			doActionEnterAccountLogin();
			break;
		}
		case R.id.register_button: {
			doActionEnterRegister();
			break;
		}
		}
	}
	
    @Override
    public void finish() {
        // If the account manager initiated the creation, and success was not reported,
        // then we assume that we're giving up (for any reason) - report failure.
        if (mAuthenticatorResponse != null) {
        	mAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
        }
        super.finish();
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
     * 
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     * 
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (DEBUG) {
    		Log.d(TAG, "onActivityResult." + requestCode + ", resultCode." +resultCode );
    	}
    	if (resultCode != Activity.RESULT_OK) {
    		return;
    	}
    	switch (requestCode) {
    	case LOGIN_REQUEST_CODE: {
    		mAuthenticatorResponse = null;
    		finish();
    		break;
    	}
    	case REGISTER_REQUEST_CODE: {
    		mAuthenticatorResponse = null;
    		finish();
    		break;
    	}
    	}
    }
    
	// private method do action section
    private void doActionEnterAccountLoginAuto(String accountName, String accountPassword) {
		Intent intent = new Intent(this, AccountLoginActivity.class);
		intent.putExtra(Constants.ACCOUNT_NAME_EXTRA, accountName);
		intent.putExtra(Constants.ACCOUNT_PASSWORD_EXTRA, accountPassword);
		startActivity(intent);    	
    }
    
	private void doActionEnterAccountLogin() {
		Intent intent = new Intent(this, AccountLoginActivity.class);
		if (mAuthenticatorResponse != null) {
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, mAuthenticatorResponse);			
		}		
		startActivityForResult(intent, LOGIN_REQUEST_CODE);
	}
	
	private void doActionEnterRegister() {
		Intent intent = new Intent(this, AccountRegisterActivity.class);
		if (mAuthenticatorResponse != null) {
			intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, mAuthenticatorResponse);			
		}		
		startActivityForResult(intent, REGISTER_REQUEST_CODE);
	}	
	
	// private method section
	
	// private class section    
}
