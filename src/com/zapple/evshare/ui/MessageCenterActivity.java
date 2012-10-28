/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.ArrayList;
import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.Station;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
 * This activity providers message center feature.
 */
public class MessageCenterActivity extends Activity {
	private static final String TAG = "MessageCenterActivity";
	private static final boolean DEBUG = true;
	private static final int QUERY_SUCCESS = 1;
	private static final int QUERY_FAILURE = 2;		
	
	private Context mContext;
	private ArrayList<Station> mStationList;
    private ProgressDialog mGetStationDialog;
    private Thread mGetStationThread = null;	
	private ListView mListView;
	private List<String> mListInfo;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case QUERY_SUCCESS: {
					break;
				}
				case QUERY_FAILURE: {
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
		setContentView(R.layout.message_center_layout);
		
		mContext = this;
		// find view section
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));

		mListInfo = new ArrayList<String>();
//		mListInfo.add(getString(R.string.power_info_label));
		mListInfo.add(getString(R.string.query_piles_site_label));
		
		mListView.setAdapter(new ListAdapter(this));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
				if (getString(R.string.power_info_label).equalsIgnoreCase((String)arg1.getTag())) {
					doActionEnterPowerInfo();
				} else if (getString(R.string.query_piles_site_label).equalsIgnoreCase((String)arg1.getTag())) {
//    				Location location = GpsService.getLocation(mContext);
//    				if (location != null) {
//    					doActionGetStore();    					
//    				} else {
//    					doActionNoticeLocationPrompt();
//    				}					
					doActionEnterPilesSite();
				} else {
					
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
    private void doActionNoticeLocationPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.prompt_label)
        	.setMessage(R.string.notice_location_prompt)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO: location ?
					if (dialog != null) {
						dialog.dismiss();
					}					
				}            	
            })
            .show();    	
    } 
    

    
	private void doActionEnterPowerInfo() {
		Intent intent = new Intent(MessageCenterActivity.this, PowerInfoActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterPowerInfo->", e);
		}		
	}	
	
	private void doActionEnterPilesSite() {
		Intent intent = new Intent(MessageCenterActivity.this, PilesSiteMapActivity.class);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "doActionEnterPilesSite->", e);
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
            return mListInfo.size();
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
                        R.layout.member_management_list_item, parent, false);
            } else {
                tv = (RelativeLayout) convertView;
            }
            String title = mListInfo.get(position);
            TextView titleTextView = (TextView) tv.findViewById(R.id.title_text_view);
            titleTextView.setText(title);
            tv.setTag(title);
            return tv;
        }        
    }
}