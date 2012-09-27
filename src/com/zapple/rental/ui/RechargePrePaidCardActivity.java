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

import java.util.ArrayList;
import java.util.List;

import com.zapple.rental.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This activity providers recharge pre-paid card feature.
 */
public class RechargePrePaidCardActivity extends Activity {
	private static final String TAG = "RechargePrePaidCardActivity";
	private static final boolean DEBUG = true;
	
	private ListView mListView;
	private List<String> mListTitleInfo;
	private List<String> mListHintInfo;
	
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
		setContentView(R.layout.message_center_layout);
		
		// find view section
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));

		mListTitleInfo = new ArrayList<String>();
		mListTitleInfo.add(getString(R.string.alipay_recharge_label));
		mListTitleInfo.add(getString(R.string.debit_card_recharge_label));
		mListTitleInfo.add(getString(R.string.credit_card_recharge_label));
		
		mListHintInfo = new ArrayList<String>();
		mListHintInfo.add(getString(R.string.alipay_recharge_hint_label));
		mListHintInfo.add(getString(R.string.debit_card_recharge_hint_label));
		mListHintInfo.add(getString(R.string.credit_card_recharge_hint_label));
		
		mListView.setAdapter(new ListAdapter(this));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
				if (arg2 == 0) {
					doActionEnterAlipayRecharge();
				} else if (arg2 == 1) {
					doActionEnterDebitCardRecharge();
				} else if (arg2 == 2) {
					doActionEnterCreditCardRecharge();
				}
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
	private void doActionEnterAlipayRecharge() {
		Intent intent = new Intent(RechargePrePaidCardActivity.this, AlipayRechargeActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterAlipayRecharge->", e);
		}		
	}	
	
	private void doActionEnterDebitCardRecharge() {
		Intent intent = new Intent(RechargePrePaidCardActivity.this, DebitCardRechargeActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterDebitCardRecharge->", e);
		}		
	}
	
	private void doActionEnterCreditCardRecharge() {
		Intent intent = new Intent(RechargePrePaidCardActivity.this, CreditCardRechargeActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterCreditCardRecharge->", e);
		}		
	}	
	
	// private method section

	// private class section
    private class ListAdapter extends BaseAdapter {
    	private Context mContext;
    	
        public ListAdapter(Context context) {
            mContext = context;
        }

        public int getCount() {
            return mListTitleInfo.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout tv;
            if (convertView == null) {
                tv = (RelativeLayout) LayoutInflater.from(mContext).inflate(
                        R.layout.pre_paid_list_item, parent, false);
            } else {
                tv = (RelativeLayout) convertView;
            }
            String title = mListTitleInfo.get(position);
            TextView titleTextView = (TextView) tv.findViewById(R.id.title_text_view);
            titleTextView.setText(title);
            
            String hint = mListHintInfo.get(position);
            TextView hintTextView = (TextView) tv.findViewById(R.id.hint_text_view);
            hintTextView.setText(hint);
            return tv;
        }        
    }
}