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

import java.util.ArrayList;
import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.PersonalInfo;
import com.zapple.evshare.data.Score;
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
import android.widget.Toast;

/**
 * This activity providers member management feature.
 */
public class MemberManagementActivity extends Activity {
	private static final String TAG = MemberManagementActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
	private static final int LIST_QUERY_TOKEN = 9601;
    
	private static final int QUERY_SUCCESS = 0;
	private static final int QUERY_FAILURE = 1;
	
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private ListView mListView;
	private View mFooterView;
	private TextView mFooterTextView;
	
	private List<Score> mScoreList;
    private ProgressDialog mQueryScoreDialog;
    private Thread mQueryScoreThread = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case QUERY_SUCCESS: {
                	mListView.setAdapter(new ScoreListArrayAdapter(mContext));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							ScoreListItem listItem = (ScoreListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                     Toast.makeText(mContext, R.string.query_score_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                    break;
                }
                case QUERY_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.query_score_failure_label);
                	} else {
                		failureReason = (String) msg.obj;
                	}
                    Toast.makeText(mContext, failureReason, 
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
		setContentView(R.layout.member_management_layout);
		
		mContext = this;
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		// find view section
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
			}
		});		
		TextView memberName = (TextView) findViewById(R.id.member_name_text_view);
		TextView memberRank = (TextView) findViewById(R.id.member_rank_text_view);
		TextView memberScore = (TextView) findViewById(R.id.member_score_text_view);
		memberName.setText(mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_NAME_KEY, ""));
		memberRank.setText(mSharedPreferences.getString(PersonalInfo.PERSONAL_INFO_USER_RANK_KEY, ""));
		memberScore.setText(String.valueOf(mSharedPreferences.getLong(LoginResult.LOGIN_RESULT_TOTAL_SCORES_KEY, 0)));
		
		mFooterView = getLayoutInflater().inflate(R.layout.load_more_list_footer_view, null);
		mFooterTextView = (TextView) mFooterView.findViewById(R.id.footer_text);
		mListView.addFooterView(mFooterView, null, true);		
		doActionQueryScore();
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
    private void doActionQueryScore() {
		if (mQueryScoreDialog != null && mQueryScoreDialog.isShowing()) {
			mQueryScoreDialog.dismiss();
		}    	
        mQueryScoreDialog = new ProgressDialog(this);
        mQueryScoreDialog.setTitle(R.string.query_score_label);
        mQueryScoreDialog.setMessage(getString(R.string.quering_score_prompt));
        mQueryScoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mQueryScoreThread != null && mQueryScoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mQueryScoreDialog.setOwnerActivity(this);
        mQueryScoreDialog.show();

        //TODO: do action queryScore
        QueryScoreRunner queryScoreRunner = new QueryScoreRunner();
        mQueryScoreThread = new Thread(queryScoreRunner);
        mQueryScoreThread.start();
    }	
	// private method section
    private void setFooterViewVisible(boolean isVisible) {
    	if (isVisible) {
    		mFooterTextView.setText(R.string.load_more_prompt);
    	} else {
    		mFooterTextView.setText(R.string.loaded_all_prompt);
    	}
//    	mFooterView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
    
	// private class section
	private class QueryScoreRunner implements Runnable {

		public void run() {
			// query score action
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	        String account = sharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, "");
	        String password = sharedPreferences.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, "");			
			String startIndex = "0";
			String endIndex = "100";
			try {
				mScoreList = WebServiceController.queryScores(account, password, startIndex, endIndex);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			// TODO: load more ?
        	if (mScoreList != null && mScoreList.size() == 100) {
        		setFooterViewVisible(true);
        	} else {
        		setFooterViewVisible(false);
        	}			
			if (DEBUG) Log.d(TAG, "QueryScoreRunner->" + mScoreList);
			
			// Deal with upload result
			Message msg = new Message();
			if (mScoreList != null) {
				if (DEBUG) Log.d(TAG, "QueryScoreRunner->queryScore success");
        		msg.what = QUERY_SUCCESS;
			} else {
				msg.what = QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "QueryScoreRunner->queryScore failure.");
			}
			if (mQueryScoreDialog != null && mQueryScoreDialog.isShowing()) {
				mQueryScoreDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
	
	private class ScoreListArrayAdapter extends BaseAdapter {
		private TextView textView1;
		private LayoutInflater inflater;

		public ScoreListArrayAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public void setList(List<Score> list)
		{
			mScoreList = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mScoreList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				// 将自定义布局 －> View
				convertView = inflater.inflate(R.layout.score_list_item, null);
				// 获取元素
			}
			ScoreListItem headerView = (ScoreListItem) convertView;
			ScoreItem item = new ScoreItem(mScoreList.get(position));
	        headerView.bind(mContext, item);

			return convertView;
		}
	} 	
}