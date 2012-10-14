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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
 * This activity providers Power Info feature.
 */
public class PowerInfoActivity extends Activity {
	private static final String TAG = "PowerInfoActivity";
	private static final boolean DEBUG = true;
	private static final int UPLOAD_SUCCESS = 0;
    
	private TextView mTitleTextView;
	private TextView mPowerInfo1TextView;
	private TextView mPowerInfo2TextView;
	private ImageView mDelete1ImageView;
	private ImageView mDelete2ImageView;

	private ProgressDialog mUploadDialog;
	private Thread mUploadThread = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case UPLOAD_SUCCESS:{
					Toast.makeText(PowerInfoActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();					
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
		setContentView(R.layout.power_info_layout);
		
		// find view section
		mTitleTextView = (TextView) findViewById(R.id.title_text_view);
		mPowerInfo1TextView = (TextView) findViewById(R.id.power_info1_text_view);
		mPowerInfo2TextView = (TextView) findViewById(R.id.power_info2_text_view);
		mDelete1ImageView = (ImageView) findViewById(R.id.delete1_image_view);
		mDelete2ImageView = (ImageView) findViewById(R.id.delete2_image_view);
				
		mTitleTextView.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionUpload();
			}
		});
		mPowerInfo1TextView.setText(getString(R.string.power_info_label) + "1");
		mPowerInfo2TextView.setText(getString(R.string.power_info_label) + "2");
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
	private void doActionUpload() {
		mUploadDialog = new ProgressDialog(this);
//		mUploadDialog.setTitle(R.string.login_label);
//		mUploadDialog.setMessage(getString(R.string.logining_prompt));
		mUploadDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mUploadThread != null && mUploadThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mUploadDialog.show();
		
		
		UploadRunner uploadRunner = new UploadRunner();
		mUploadThread = new Thread(uploadRunner);
		mUploadThread.start();		
	}	
	
	// private method section
	
	// private class section    
	private class UploadRunner implements Runnable {

		public void run() {
			// TODO: upload action
			if (DEBUG) {
				Log.d(TAG, "UploadRunner->run->isUploadSuccess:");
			}			
			
			// TODO deal with login result
			Message msg = new Message();
			msg.what = UPLOAD_SUCCESS;
			msg.obj = "TODO";
			mHandler.sendMessage(msg);
			if (mUploadDialog != null && mUploadDialog.isShowing()) {
				mUploadDialog.dismiss();
			}
		}		
	}
}
