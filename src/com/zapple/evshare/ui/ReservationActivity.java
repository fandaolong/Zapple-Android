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
import com.zapple.evshare.data.NewFragmentInfo;
import com.zapple.evshare.ui.OrderChooseTimeFragment.Callback;
import com.zapple.evshare.util.Constants;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.Window;

/**
 * This activity providers Reservation feature.
 */
public class ReservationActivity extends FragmentActivity implements 
	ReservationServiceFragment.Callback, StoreDetailFragment.Callback,
	OrderChooseServiceFragment.Callback, OrderChooseTimeFragment.Callback,
	OrderCheckFragment.Callback {
	private static final String TAG = ReservationActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
    private ReservationServiceFragment mReservationFragment;
    private StoreDetailFragment mStoreDetailFragment;
    private OrderChooseServiceFragment mOrderChooseServiceFragment;
    private OrderChooseTimeFragment mOrderChooseTimeFragment;
    private OrderCheckFragment mOrderCheckFragment;
    
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
		setContentView(R.layout.activity_reservation);
		
		// find view section
		NewFragmentInfo info = new NewFragmentInfo();
		info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
		info.mName = ReservationServiceFragment.class.getSimpleName();
		info.mArgs = null;
		mReservationFragment = ReservationServiceFragment.newInstance(info);
		mReservationFragment.setCallback(this);
		
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.content, mReservationFragment);
		t.commit();
	}
	
	@Override
	public void onBackPressed() {
		
//	    mReservationFragment = null;
//	    mStoreDetailFragment = null;
//	    mOrderChooseServiceFragment = null;
//	    mOrderChooseTimeFragment = null;
//	    mOrderCheckFragment = null;
		if (mStoreDetailFragment != null) {
			NewFragmentInfo info = new NewFragmentInfo();
			info.mTabIndex = Constants.TAB_INDEX_RESERVATION;
			info.mName = ReservationServiceFragment.class.getSimpleName();
			info.mArgs = null;
			onStoreDetailBackClicked(info);
			return;
		}
		super.onBackPressed();
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
	public void onNextClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mStoreDetailFragment = StoreDetailFragment.newInstance(info);
		mStoreDetailFragment.setCallback(this);
		t.replace(R.id.content, mStoreDetailFragment);
		t.commit();
	    mReservationFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderChooseTimeFragment = null;
	    mOrderCheckFragment = null;
	}

	@Override
	public void onReservationClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mOrderChooseServiceFragment = OrderChooseServiceFragment.newInstance(info);
		mOrderChooseServiceFragment.setCallback(this);
		t.replace(R.id.content, mOrderChooseServiceFragment);
		t.commit();
	    mReservationFragment = null;
	    mStoreDetailFragment = null;
	    mOrderChooseTimeFragment = null;
	    mOrderCheckFragment = null;
	}
	
	@Override
	public void onStoreDetailBackClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mReservationFragment = ReservationServiceFragment.newInstance(info);
		mReservationFragment.setCallback(this);
		t.replace(R.id.content, mReservationFragment);
		t.commit();		
	    mStoreDetailFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderChooseTimeFragment = null;
	    mOrderCheckFragment = null;
	}	

	@Override
	public void onChooseServicePreviousClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mStoreDetailFragment = StoreDetailFragment.newInstance(info);
		mStoreDetailFragment.setCallback(this);
		t.replace(R.id.content, mStoreDetailFragment);
		t.commit();
	    mReservationFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderChooseTimeFragment = null;
	    mOrderCheckFragment = null;
	}

	@Override
	public void onChooseServiceNextClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mOrderChooseTimeFragment = OrderChooseTimeFragment.newInstance(info);
		mOrderChooseTimeFragment.setCallback(this);
		t.replace(R.id.content, mOrderChooseTimeFragment);
		t.commit();		
	    mReservationFragment = null;
	    mStoreDetailFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderCheckFragment = null;
	}

	@Override
	public void onChooseTimePreviousClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mOrderChooseServiceFragment = OrderChooseServiceFragment.newInstance(info);
		mOrderChooseServiceFragment.setCallback(this);
		t.replace(R.id.content, mOrderChooseServiceFragment);
		t.commit();
	    mReservationFragment = null;
	    mStoreDetailFragment = null;
	    mOrderChooseTimeFragment = null;
	    mOrderCheckFragment = null;
	}

	@Override
	public void onChooseTimeNextClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mOrderCheckFragment = OrderCheckFragment.newInstance(info);
		mOrderCheckFragment.setCallback(this);
		t.replace(R.id.content, mOrderCheckFragment);
		t.commit();
	    mReservationFragment = null;
	    mStoreDetailFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderChooseTimeFragment = null;
	}

	@Override
	public void onOrderCheckPreviousClicked(NewFragmentInfo info) {
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		mOrderChooseTimeFragment = OrderChooseTimeFragment.newInstance(info);
		mOrderChooseTimeFragment.setCallback(this);
		t.replace(R.id.content, mOrderChooseTimeFragment);
		t.commit();		
	    mReservationFragment = null;
	    mStoreDetailFragment = null;
	    mOrderChooseServiceFragment = null;
	    mOrderCheckFragment = null;
	}

	@Override
	public void onOrderCheckSubmitClicked(NewFragmentInfo info) {
		// TODO Auto-generated method stub
		
	}

	// private method do action section
	
	// private method section
	
	// private class section    
}
