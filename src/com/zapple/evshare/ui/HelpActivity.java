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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.TextView;

/**
 * This activity providers About feature.
 */
public class HelpActivity extends Activity implements OnClickListener {
	private static final String TAG = HelpActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
    
	private Button mPersonalInfoButton;
	private Button mMemberRegulationButton;
	private Button mCustomerServiceButton;
	private Button mComplaintSuggestionButton;
	private Button mMessageCenterButton;
	private Button mPowerInfoButton;
	private Button mNearbyPilesButton;
	private Button mRentalProcedureButton;
	
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
		setContentView(R.layout.help_layout);
		
		// find view section
		mPersonalInfoButton = (Button) findViewById(R.id.personal_info_button);
		mMemberRegulationButton = (Button) findViewById(R.id.member_regulation_button);
		mCustomerServiceButton = (Button) findViewById(R.id.customer_service_button);
		mComplaintSuggestionButton = (Button) findViewById(R.id.complaint_suggestion_button);
		mMessageCenterButton = (Button) findViewById(R.id.message_center_button);
		mPowerInfoButton = (Button) findViewById(R.id.power_info_button);
		mNearbyPilesButton = (Button) findViewById(R.id.nearby_piles_button);
		mRentalProcedureButton = (Button) findViewById(R.id.rental_procedure_button);
		
		mPersonalInfoButton.setOnClickListener(this);
		mMemberRegulationButton.setOnClickListener(this);
		mCustomerServiceButton.setOnClickListener(this);
		mComplaintSuggestionButton.setOnClickListener(this);
		mMessageCenterButton.setOnClickListener(this);
		mPowerInfoButton.setOnClickListener(this);
		mNearbyPilesButton.setOnClickListener(this);
		mRentalProcedureButton.setOnClickListener(this);
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
		switch(arg0.getId()) {
			case R.id.personal_info_button: {
				doActionEnterPersonalInfo();
				break;
			}
			case R.id.member_regulation_button: {
				doActionEnterMemberRegulation();
				break;
			}
			case R.id.customer_service_button: {
				doActionEnterCustomerService();
				break;
			}
			case R.id.complaint_suggestion_button: {
				doActionEnterCompaintSuggestion();
				break;
			}
			case R.id.message_center_button: {
				doActionEnterMessageCenter();
				break;
			}
			case R.id.power_info_button: {
				doActionEnterPowerInfo();
				break;
			}
			case R.id.nearby_piles_button: {
				doActionEnterNearbyPiles();
				break;
			}
			case R.id.rental_procedure_button: {
				doActionRentalProcedure();
				break;
			}		
		}		
	}
	
	// private method do action section
	private void doActionEnterPersonalInfo() {
		Intent intent = new Intent(this, PersonalInfoActivity.class);
		startActivity(intent);
	}
	
	private void doActionEnterMemberRegulation() {
		Intent intent = new Intent(this, MemberRegulationActivity.class);
		startActivity(intent);		
	}
	
	private void doActionEnterCustomerService() {
		Intent intent = new Intent(this, CustomerServiceActivity.class);
		startActivity(intent);		
	}
	
	private void doActionEnterCompaintSuggestion() {
		Intent intent = new Intent(this, ComplaintSuggestionActivity.class);
		startActivity(intent);		
	}
	
	private void doActionEnterMessageCenter() {
		Intent intent = new Intent(this, MessageCenterActivity.class);
		startActivity(intent);		
	}
	
	private void doActionEnterPowerInfo() {
		Intent intent = new Intent(this, PowerInfoActivity.class);
		startActivity(intent);		
	}
	
	private void doActionEnterNearbyPiles() {
		Intent intent = new Intent(this, PilesSiteMapActivity.class);
		startActivity(intent);		
	}
	
	private void doActionRentalProcedure() {
		Intent intent = new Intent(this, RentalProcedureActivity.class);
		startActivity(intent);		
	}	
	
	// private method section
	
	// private class section    
}
