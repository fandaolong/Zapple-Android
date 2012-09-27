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
import com.zapple.rental.data.PersonalInfo;
import com.zapple.rental.transaction.WebServiceController;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

/**
 * This activity providers Personal info feature.
 */
public class PersonalInfoActivity extends Activity {
	private static final String TAG = "PersonalInfoActivity";
	private static final boolean DEBUG = true;
	private static final int MODIFY_SUCCESS = 0;
	private static final int MODIFY_FAILURE = 1;
    
	private TextView mNameTextView;
	private TextView mMemberRankTextView;
	private TextView mIdTypeTextView;
	private TextView mIdTextView;
	private TextView mMobileTextView;
	private TextView mEmailAddressTextView;
	private EditText mMobileEditText;
	private EditText mEmailAddressEditText;
	private EditText mVerifyPasswordEditText;
	private Button mModifyButton;
	
	private boolean mIsEditable;
	private SharedPreferences mSharedPreferences;
	private PersonalInfo mPersonalInfo = new PersonalInfo();
	private ProgressDialog mModifyDialog;
	private Thread mModifyThread = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case MODIFY_SUCCESS:{
//					mMobileEditText.setVisibility(View.GONE);
//					mEmailAddressEditText.setVisibility(View.GONE);
//					mVerifyPasswordEditText.setVisibility(View.GONE);
					mMobileEditText.setEnabled(false);
					mEmailAddressEditText.setEnabled(false);
					mIsEditable = false;
					mModifyButton.setText(R.string.modify_label);	
					Toast.makeText(PersonalInfoActivity.this, R.string.modify_personal_info_success_prompt, Toast.LENGTH_SHORT).show();					
					break;
				}
				case MODIFY_FAILURE:{
//					mMobileEditText.setVisibility(View.GONE);
//					mEmailAddressEditText.setVisibility(View.GONE);
//					mVerifyPasswordEditText.setVisibility(View.GONE);
					mIsEditable = false;
					mModifyButton.setText(R.string.modify_label);	
					Toast.makeText(PersonalInfoActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();					
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
		setContentView(R.layout.personal_info_layout);
		
		// find view section
		mNameTextView = (TextView) findViewById(R.id.name_text_view);
		mMemberRankTextView = (TextView) findViewById(R.id.member_rank_text_view);
		mIdTypeTextView = (TextView) findViewById(R.id.id_type_text_view);
		mIdTextView = (TextView) findViewById(R.id.id_text_view);
		mMobileTextView = (TextView) findViewById(R.id.mobile_text_view);
		mEmailAddressTextView = (TextView) findViewById(R.id.email_address_text_view);
		
		mMobileEditText = (EditText) findViewById(R.id.mobile_edit_text);
		mEmailAddressEditText = (EditText) findViewById(R.id.email_address_edit_text);
		mVerifyPasswordEditText = (EditText) findViewById(R.id.verify_password_edit_text);
		mModifyButton = (Button) findViewById(R.id.modify_button);
		
		mModifyButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mIsEditable) {
					doActionModifyPersonalInfo();
					mIsEditable = false;					
				} else {
					mIsEditable = true;
					mMobileEditText.setEnabled(true);
//					mMobileTextView.setText(getString(R.string.mobile_label));
					mMobileEditText.setText(mPersonalInfo.mPhoneNumber);
//					mMobileEditText.setVisibility(View.VISIBLE);
					
					mEmailAddressEditText.setEnabled(true);
//					mEmailAddressTextView.setText(getString(R.string.email_address_label));
					mEmailAddressEditText.setText(mPersonalInfo.mEmailAddress);
//					mEmailAddressEditText.setVisibility(View.VISIBLE);					
//					mVerifyPasswordEditText.setVisibility(View.VISIBLE);
					mModifyButton.setText(R.string.save_label);									
				}
			}
		});
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PersonalInfoActivity.this);
		mPersonalInfo.mName = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_NAME_KEY, "");
		mPersonalInfo.mUserRank = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_USER_RANK_KEY, "");
		mPersonalInfo.mIdType = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_ID_TYPE_KEY, "");
		mPersonalInfo.mId = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_ID_KEY, "");
		mPersonalInfo.mPhoneNumber = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_PHONE_NUMBER_KEY, "");
		mPersonalInfo.mEmailAddress = mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_EMAIL_ADDRESS_KEY, "");
		mNameTextView.setText(mPersonalInfo.mName);
		mMemberRankTextView.setText(mPersonalInfo.mUserRank);
		mIdTypeTextView.setText(mPersonalInfo.mIdType);
		mIdTextView.setText(mPersonalInfo.mId);
		mMobileEditText.setText(mPersonalInfo.mPhoneNumber);
		mMobileEditText.setEnabled(false);
		mEmailAddressEditText.setText(mPersonalInfo.mEmailAddress);
		mEmailAddressEditText.setEnabled(false);
		mIsEditable = false;
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
	private void doActionModifyPersonalInfo() {
		if (mModifyDialog != null && mModifyDialog.isShowing()) {
			mModifyDialog.dismiss();
		}		
		mModifyDialog = new ProgressDialog(this);
//		mModifyDialog.setTitle(R.string.login_label);
//		mModifyDialog.setMessage(getString(R.string.logining_prompt));
		mModifyDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mModifyThread != null && mModifyThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mModifyDialog.show();
		
		
		ModifyPersonalInfoRunner modifyPersonalInfoRunner = new ModifyPersonalInfoRunner();
		mModifyThread = new Thread(modifyPersonalInfoRunner);
		mModifyThread.start();		
	}	
	
	// private method section
	
	// private class section    
	private class ModifyPersonalInfoRunner implements Runnable {

		public void run() {
			// Modify personal info action
			String result = null;
			String userName = mSharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
			String password = mSharedPreferences.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
			// TODO: just for test
			mPersonalInfo.mIdType = "Éí·ÝÖ¤";
			mPersonalInfo.mId = "440901198403194316";
			
			mPersonalInfo.mPhoneNumber = mMobileEditText.getText().toString().trim();
			mPersonalInfo.mEmailAddress = mEmailAddressEditText.getText().toString().trim();
			try {
				result = WebServiceController.modifyPersonalInfo(userName, password, mPersonalInfo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (DEBUG) {
				Log.d(TAG, "ModifyPersonalInfoRunner->run->result:" + result);
			}			
			
			// Deal with modify personal info result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				msg.what = MODIFY_SUCCESS;
			} else {
				msg.what = MODIFY_FAILURE;
				msg.obj = result;
			}						
			mHandler.sendMessage(msg);
			if (mModifyDialog != null && mModifyDialog.isShowing()) {
				mModifyDialog.dismiss();
			}
		}		
	}
}
