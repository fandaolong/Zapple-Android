/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.Order;
import com.zapple.evshare.data.QueryOrderResult;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * This activity providers add POS feature.
 */
public class AccountManagementActivity extends Activity {
	private static final String TAG = AccountManagementActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
	private static final int QUERY_SUCCESS = 0;
	private static final int QUERY_FAILURE = 1;		
	
	// TODO: query card id from server instead of "0000 0000 0000 0000 32"
	private String mCardId = "0000 0000 0000 0000 32";
	private String mAccountBalance;
	private ListView mListView;
	private View mFooterView;
	private TextView mFooterTextView;
	
	private Context mContext;
	private List<Order> mOrderList;
    private ProgressDialog mQueryOrderDialog;	
    private Thread mQueryOrderThread = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
            case QUERY_SUCCESS: {
            	mListView.setAdapter(new OrderListArrayAdapter(mContext));
//            	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//					@Override
//					public void onItemClick(AdapterView<?> arg0, View arg1,
//							int arg2, long arg3) {
//						if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//						OrderListItem listItem = (OrderListItem) arg1;
//						doActionEnterOrderDetail(listItem.getListItem().getId());							
//					}                		
//            	});
                 Toast.makeText(mContext, R.string.query_account_success_label, 
                		 Toast.LENGTH_SHORT).show();  
                break;
            }
            case QUERY_FAILURE: {
            	String failureReason;
            	if (msg.obj != null) {
            		failureReason = (String) msg.obj;
            	} else {
            		failureReason = getString(R.string.query_account_failure_label);
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
		setContentView(R.layout.account_management_layout);
		
		mContext = this;
		// find view section
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
				OrderListItem listItem = (OrderListItem) arg1;
				OrderItem item = listItem.getListItem();
		       	int statusCode = -1;
	        	try {
	        		statusCode = Integer.parseInt(item.getStatus());
	        	} catch (Exception e) {
	        		if (DEBUG) Log.e(TAG, "setOnItemClickListener.e." + e);
	        	}
	        	if (DEBUG) Log.d(TAG, "setOnItemClickListener.statusCode." + statusCode);
				
			}
		});
		TextView cardId = (TextView) findViewById(R.id.card_id_text_view);
		cardId.setText(mCardId);
		// TODO: query account balance from server instead of "1600"
		mAccountBalance = "￥" + "1600" + getResources().getString(R.string.account_management_yuan_label);
		TextView accountBalanceTextView = (TextView) findViewById(R.id.account_balance_text_view);
		accountBalanceTextView.setText(mAccountBalance);
		
		mFooterView = getLayoutInflater().inflate(R.layout.load_more_list_footer_view, null);
		mFooterTextView = (TextView) mFooterView.findViewById(R.id.footer_text);
		mListView.addFooterView(mFooterView, null, true);
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
		doActionQueryOrder();
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
    private void doActionQueryOrder() {
		if (mQueryOrderDialog != null && mQueryOrderDialog.isShowing()) {
			mQueryOrderDialog.dismiss();
		}    	
        mQueryOrderDialog = new ProgressDialog(this);
        mQueryOrderDialog.setTitle(R.string.query_account_label);
        mQueryOrderDialog.setMessage(getString(R.string.quering_account_prompt));
        mQueryOrderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mQueryOrderThread != null && mQueryOrderThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mQueryOrderDialog.setOwnerActivity(this);
        mQueryOrderDialog.show();

        //TODO: do action queryOrder
        QueryOrderRunner queryOrderRunner = new QueryOrderRunner();
        mQueryOrderThread = new Thread(queryOrderRunner);
        mQueryOrderThread.start();
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
	private class QueryOrderRunner implements Runnable {

		public void run() {
			// query account action
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	        String account = sharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, "");
	        String password = sharedPreferences.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, "");
			String startIndex = "0";
			String endIndex = "100";
			QueryOrderResult result = null;
			try {
				result = WebServiceController.queryOrders(account, password, startIndex, endIndex);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			// TODO: load more ?
        	if (result != null && result.mOrderList != null && result.mOrderList.size() == 100) {
        		setFooterViewVisible(true);
        	} else {
        		setFooterViewVisible(false);
        	}
			if (DEBUG) Log.d(TAG, "QueryOrderRunner->" + mOrderList);
			
			// Deal with upload result
			Message msg = new Message();
			if (result != null && result.mOrderList != null && TextUtils.isEmpty(result.mResult)) {
				if (DEBUG) Log.d(TAG, "QueryOrderRunner->queryOrder success");
        		msg.what = QUERY_SUCCESS;
        		mOrderList = result.mOrderList;
			} else {
				msg.what = QUERY_FAILURE;
				msg.obj = result != null ? result.mResult : "";
				if (DEBUG) Log.d(TAG, "QueryOrderRunner->queryOrder failure.");
			}
			if (mQueryOrderDialog != null && mQueryOrderDialog.isShowing()) {
				mQueryOrderDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}
	
	private class OrderListArrayAdapter extends BaseAdapter {
		private TextView textView1;
		private LayoutInflater inflater;

		public OrderListArrayAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public void setList(List<Order> list)
		{
			mOrderList = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mOrderList.size();
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
				convertView = inflater.inflate(R.layout.account_management_list_item, null);
				// 获取元素
			}
			AccountManagementListItem headerView = (AccountManagementListItem) convertView;
	        headerView.bind(mContext, mOrderList.get(position));

			return convertView;
		}
	}	
}
