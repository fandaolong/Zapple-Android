/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.ArrayList;
import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.Store;
import com.zapple.evshare.data.Store.StoreTable;
import com.zapple.evshare.transaction.WebServiceController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StoreListActivity extends Activity {
	private static final String TAG = StoreListActivity.class.getSimpleName();
	private static final boolean DEBUG = true;
	private static final int LIST_QUERY_TOKEN = 9601;
	public static final String FROM_WHERE_EXTRA = "from_were_extra";
	public static final String SEARCH_TEXT_EXTRA = "search_text_extra";
	public static final int FROM_QUICK_ORDER = 1;
	public static final int FROM_RESERVATION_VEHICLE = 2;
	public static final int FROM_QUERY_STORE = 3;
	
	private static final int QUERY_SUCCESS = 0;
	private static final int QUERY_FAILURE = 1;	
	
	private TextView mTitleTextView;
	private Button mMapButton;	
	private ListView mListView;
	private StoreListAdapter mListAdapter;
	private BackgroundQueryHandler mBackgroundQueryHandler;
	private int mFromWhere;
	private String mSearchText;
	private Context mContext;
	private ArrayList<Store> mStoreList;
    private ProgressDialog mGetStoreDialog;
    private Thread mGetStoreThread = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case QUERY_SUCCESS: {
                	mListView.setAdapter(new StoreListArrayAdapter(mContext));
//                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//						@Override
//						public void onItemClick(AdapterView<?> arg0, View arg1,
//								int arg2, long arg3) {
//							if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//							StoreListItem listItem = (StoreListItem) arg1;
//							doActionEnterOrderDetail(listItem.getListItem().getId());							
//						}                		
//                	});
                	if (mStoreList != null && mStoreList.size() == 0) {
                        Toast.makeText(mContext, R.string.empty_prompt, 
                       		 Toast.LENGTH_SHORT).show();                  		
                	} else {
                        Toast.makeText(mContext, R.string.get_store_success_label, 
                       		 Toast.LENGTH_SHORT).show();  
                	}
                    break;
                }
                case QUERY_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.get_store_failure_label);
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
		setContentView(R.layout.store_list_layout);
		
		mContext = this;
		// find view section
		mTitleTextView = (TextView) findViewById(R.id.title_text_view);
		mMapButton = (Button) findViewById(R.id.map_button);
		
		mListView = (ListView) findViewById(R.id.list_view);
		mListView.setEmptyView((TextView) findViewById(R.id.empty));

		mMapButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				doActionEnterQuickOrder();
			}
		});

		mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());
		Intent i = getIntent();
		if (i != null) {
			mFromWhere = i.getIntExtra(FROM_WHERE_EXTRA, 0);
			mSearchText = i.getStringExtra(SEARCH_TEXT_EXTRA);
		}		

		mListAdapter = new StoreListAdapter(this, null);
		mListAdapter.setFromWhere(mFromWhere);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
				StoreListItem listItem = (StoreListItem) arg1;
				doActionEnterOrderDetail(listItem.getListItem());					
			}
		});		
		mTitleTextView.setText(mSearchText + getString(R.string.store_list_label));
		
		if (FROM_QUERY_STORE == mFromWhere) {
			doActionGetStore();
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
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
    private void doActionGetStore() {
		if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
			mGetStoreDialog.dismiss();
		}    	
        mGetStoreDialog = new ProgressDialog(this);
        mGetStoreDialog.setTitle(R.string.get_store_label);
        mGetStoreDialog.setMessage(getString(R.string.getting_store_prompt));
        mGetStoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetStoreThread != null && mGetStoreThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mGetStoreDialog.setOwnerActivity(this);
        mGetStoreDialog.show();

        //TODO: do action getStore
        GetStoreRunner getStoreRunner = new GetStoreRunner();
        mGetStoreThread = new Thread(getStoreRunner);
        mGetStoreThread.start();
    }
    
    private void doActionEnterQuickOrder() {
//    	Intent i = new Intent(mContext, QuickOrderActivity.class);
//    	i.putExtra(QuickOrderActivity.TITLE_EXTRA, mSearchText + getString(R.string.store_list_label));
//    	i.putParcelableArrayListExtra(QuickOrderActivity.STORE_LIST_EXTRA, mStoreList);
//    	i.putExtra(QuickOrderActivity.ENABLE_LIST_BUTTON_EXTRA, true);
//    	startActivity(i);      	
    }
    
    private void doActionEnterOrderDetail(StoreItem item) {
//		Intent intent = new Intent(mContext, StoreDetailActivity.class);
//		intent.putExtra(StoreDetailActivity.FROM_WHERE_EXTRA, StoreDetailActivity.FROM_STORE_LIST);
//		intent.putExtra(StoreDetailActivity.STORE_ID_EXTRA, item.getRemoteId());
//		intent.putExtra(StoreDetailActivity.STORE_NAME_EXTRA, item.getName());
//		startActivity(intent);	    	
    }
        
	// private method section
	private void startListQuery() {
		if (DEBUG) Log.d(TAG, "startListQuery");
        String selection = null;

        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(LIST_QUERY_TOKEN);
        try {       	
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
                    LIST_QUERY_TOKEN, null, StoreTable.CONTENT_URI,
                    StoreListAdapter.STORE_PROJECTION, selection, null, null);
        } catch (SQLiteException e) {
        	if (DEBUG) Log.e(TAG, "startListQuery:" + e);
        }
	}
	
	// private class section    
	private class GetStoreRunner implements Runnable {

		public void run() {
			// get store action
			String startIndex = "0";
			String endIndex = "100";
			try {
				mStoreList = WebServiceController.getStores(null, null, null, mSearchText, startIndex, endIndex);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "GetStoreRunner->" + mStoreList);
			
			// Deal with upload result
			Message msg = new Message();
			if (mStoreList != null) {
				if (DEBUG) Log.d(TAG, "GetStoreRunner->getStore success");
        		msg.what = QUERY_SUCCESS;
			} else {
				msg.what = QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "GetStoreRunner->getStore failure.");
			}
			if (mGetStoreDialog != null && mGetStoreDialog.isShowing()) {
				mGetStoreDialog.dismiss();
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
    
	private class StoreListArrayAdapter extends BaseAdapter {
		private TextView textView1;
		private LayoutInflater inflater;

		public StoreListArrayAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}
		
		public void setList(ArrayList<Store> list)
		{
			mStoreList = list;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mStoreList.size();
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
				convertView = inflater.inflate(R.layout.store_list_item, null);
				// 获取元素
			}
			StoreListItem headerView = (StoreListItem) convertView;
			StoreItem item = new StoreItem(mStoreList.get(position));
	        headerView.bind(mContext, item);

			return convertView;
		}
	}    
}
