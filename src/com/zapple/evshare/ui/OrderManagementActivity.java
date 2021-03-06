/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.Order;
import com.zapple.evshare.data.Order.OrderTable;
import com.zapple.evshare.data.QueryOrderResult;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
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
 * This activity providers order management feature.
 */
public class OrderManagementActivity extends Activity {
	private static final String TAG = "OrderManagementActivity";
	private static final boolean DEBUG = true;
	private static final int LIST_QUERY_TOKEN = 9601;
    
	private static final int QUERY_SUCCESS = 0;
	private static final int QUERY_FAILURE = 1;		
	private static final int CANCEL_ORDER_SUCCESS = 2;
	private static final int CANCEL_ORDER_FAILURE = 3;	
	
	private ListView mListView;
	private OrderListAdapter mListAdapter;
	private BackgroundQueryHandler mBackgroundQueryHandler;	
		
	private Context mContext;
	private List<Order> mOrderList;
    private ProgressDialog mQueryOrderDialog;
    private Thread mQueryOrderThread = null;
	private ProgressDialog mCancelOrderDialog;
	private Thread mCancelOrderThread = null;    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
			case CANCEL_ORDER_SUCCESS:{
				Toast.makeText(mContext, R.string.cancel_order_success_prompt, Toast.LENGTH_SHORT).show();		
				finish();
				break;
			}
			case CANCEL_ORDER_FAILURE:{
				Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();					
				break;
			}            
                case QUERY_SUCCESS: {
                	mListView.setAdapter(new OrderListArrayAdapter(mContext));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							OrderListItem listItem = (OrderListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                     Toast.makeText(mContext, R.string.query_order_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                    break;
                }
                case QUERY_FAILURE: {
                	String failureReason;
                	
                	if (msg.obj != null) {
                		failureReason = (String) msg.obj;
                	} else {
                		failureReason = getString(R.string.query_order_failure_label);
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
		setContentView(R.layout.order_management_layout);
		
		mContext = this;
		// find view section
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));
		mListAdapter = new OrderListAdapter(this, null);
		mListView.setAdapter(mListAdapter);
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
				if (Constants.ORDER_STATUS_WAITING_TAKE == statusCode) {
					doActionCancelOrder(item.getRemoteId());
				} else if (Constants.ORDER_STATUS_UNPAID == statusCode) {
					
				}
				
			}
		});				
		mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());	
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
//		startListQuery();
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
        // Close the cursor in the ListAdapter if the activity stopped.
        Cursor cursor = mListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        mListAdapter.changeCursor(null);		
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
        mQueryOrderDialog.setTitle(R.string.query_order_label);
        mQueryOrderDialog.setMessage(getString(R.string.quering_order_prompt));
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
    
	private void doActionCancelOrder(final String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.confirm_cancel_dialog_title)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelOrder(orderId);
					dialog.dismiss();
				}            	
            })
            .show();		
	}
		
	// private method section
	private void cancelOrder(String orderId) {
		if (mCancelOrderDialog != null && mCancelOrderDialog.isShowing()) {
			mCancelOrderDialog.dismiss();
		}		
		mCancelOrderDialog = new ProgressDialog(this);
//		mCancelOrderDialog.setTitle(R.string.submit_label);
//		mCancelOrderDialog.setMessage(getString(R.string.submitting_prompt));
		mCancelOrderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {			
			public void onDismiss(DialogInterface dialog) {
				if(mCancelOrderThread != null && mCancelOrderThread.isInterrupted()) {
					// indicate thread should cancel
				}
			}
		});
		mCancelOrderDialog.setOwnerActivity(this);
		mCancelOrderDialog.show();
		
		
		CancelOrderRunner cancelOrderRunner = new CancelOrderRunner(orderId);
		mCancelOrderThread = new Thread(cancelOrderRunner);
		mCancelOrderThread.start();		
	}
	
	private void startListQuery() {
		if (DEBUG) Log.d(TAG, "startListQuery");
        String selection = null;

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(LIST_QUERY_TOKEN);
        try {       	
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
                    LIST_QUERY_TOKEN, null, OrderTable.CONTENT_URI,
                    OrderListAdapter.ORDER_PROJECTION, selection, null, null);
        } catch (SQLiteException e) {
        	if (DEBUG) Log.e(TAG, "startListQuery:" + e);
        }
	}
	
	// private class section
	private class QueryOrderRunner implements Runnable {

		public void run() {
			// query order action
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
	
	private class CancelOrderRunner implements Runnable {
		private String mOrderId;
		
		CancelOrderRunner(String orderId) {
			mOrderId = orderId;
		}

		public void run() {
			// cancel order action
			String result = null;
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			String userName = sp.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
			String password = sp.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
			try {
				result = WebServiceController.cancelOrder(userName, password, mOrderId);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "CancelOrderRunner->failure");
			}				
			
			// deal with cancel order result
			Message msg = new Message();
			if (TextUtils.isEmpty(result)) {
				msg.what = CANCEL_ORDER_SUCCESS;								
				if (DEBUG) Log.d(TAG, "CancelOrderRunner->success.");
			} else {
				if (DEBUG) Log.d(TAG, "CancelOrderRunner->failure");
        		msg.what = CANCEL_ORDER_FAILURE;
        		msg.obj = result;				
			}

			if (mCancelOrderDialog != null && mCancelOrderDialog.isShowing()) {
				mCancelOrderDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}	
	
    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch(token) {
                case LIST_QUERY_TOKEN:
                    mListAdapter.changeCursor(cursor);
                    break;
            }
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
				convertView = inflater.inflate(R.layout.order_list_item, null);
				// 获取元素
			}
			OrderListItem headerView = (OrderListItem) convertView;
			OrderItem item = new OrderItem(mOrderList.get(position));
	        headerView.bind(mContext, item);

			return convertView;
		}
	}    
}