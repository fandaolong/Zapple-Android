/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.util.ArrayList;
import java.util.List;

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.QueryFavoriteResult;
import com.zapple.evshare.data.Store;
import com.zapple.evshare.data.Store.StoreTable;
import com.zapple.evshare.data.Vehicle;
import com.zapple.evshare.data.Vehicle.VehicleTable;
import com.zapple.evshare.transaction.WebServiceController;
import com.zapple.evshare.util.Constants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This activity providers favorite feature.
 */
public class FavoriteActivity extends Activity {
	private static final String TAG = "FavoriteActivity";
	private static final boolean DEBUG = true;
	private static final int STORE_LIST_QUERY_TOKEN = 9601;
	private static final int VEHICLE_LIST_QUERY_TOKEN = 9602;
    
	private static final int QUERY_SUCCESS = 0;
	private static final int QUERY_FAILURE = 1;			
	
	private ViewPager mContentViewPager;
	private ArrayList<View> mPageViewerListItem;
	private int mCurrentPager;
	private Button mStoreTabTextView;
	private Button mVehicleTabTextView;
	
	private ListView mStoreListView;
	private ListView mVehicleListView;
	private FavoriteStoreListAdapter mStoreListAdapter;
	private FavoriteVehicleListAdapter mVehicleListAdapter;
	private BackgroundQueryHandler mBackgroundQueryHandler;	
		
	private Context mContext;
	private QueryFavoriteResult mQueryFavoriteResult;
    private ProgressDialog mQueryDialog;
    private Thread mQueryThread = null;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case QUERY_SUCCESS: {
                	startFavoriteStoreListQuery();
                	Toast.makeText(mContext, R.string.query_favorite_success_label, 
                    		 Toast.LENGTH_SHORT).show();  
                    break;
                }
                case QUERY_FAILURE: {
                	String failureReason;
                	if (msg.obj == null) {
                		failureReason = getString(R.string.query_favorite_failure_label);
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
		setContentView(R.layout.favorite_layout);
		
		mContext = this;
		// find view section
		mContentViewPager = (ViewPager) findViewById(R.id.content_view_pager);
		mStoreTabTextView = (Button) findViewById(R.id.store_tab_text_view);
		mVehicleTabTextView = (Button) findViewById(R.id.vehicle_tab_text_view);
		
		LayoutInflater inflater = getLayoutInflater();		
		
		View pager1 = inflater.inflate(R.layout.list_view, null);
		mStoreListView = (ListView) pager1.findViewById(R.id.list_view);
		mStoreListView.setEmptyView(pager1.findViewById(R.id.empty));
		
		View pager2 = inflater.inflate(R.layout.list_view, null);
		mVehicleListView = (ListView) pager2.findViewById(R.id.list_view);
		mVehicleListView.setEmptyView(pager2.findViewById(R.id.empty));
		
		mPageViewerListItem = new ArrayList<View>();
		mPageViewerListItem.add(pager1);
		mPageViewerListItem.add(pager2);
		mContentViewPager.setAdapter(new MyAdapter());
		mContentViewPager.setOnPageChangeListener(new MyPageChangeListener());		
//		
//		mListView = (ListView) findViewById(R.id.list_view);
//		mListView.setEmptyView((TextView) findViewById(R.id.empty));
		mStoreListAdapter = new FavoriteStoreListAdapter(this, null);
		mStoreListView.setAdapter(mStoreListAdapter);
		mStoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
				StoreListItem listItem = (StoreListItem) arg1;
				StoreItem item = listItem.getListItem();
				doActionEnterStoreDetail(item.getRemoteId(), item.getName());						
			}
		});
		
		mVehicleListAdapter = new FavoriteVehicleListAdapter(this, null);
		mVehicleListAdapter.setFromWhere(Constants.FROM_FAVORITE);
		mVehicleListView.setAdapter(mVehicleListAdapter);
		mVehicleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
			}			
		});
//		mListView.setAdapter(mListAdapter);
//		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				if (DEBUG) Log.d(TAG, "setOnItemClickListener.arg2." + arg2);
//				FavoriteStoreListItem listItem = (FavoriteStoreListItem) arg1;
//				FavoriteStoreItem item = listItem.getListItem();
//				doActionEnterFavoriteStoreDetail(item.getId(), item.getName());					
//			}
//		});				
		mStoreTabTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mStoreTabTextView." + v);
				mStoreTabTextView.setBackgroundResource(R.drawable.btn_grey_pressed);
				mVehicleTabTextView.setBackgroundResource(R.drawable.btn_grey_default);				
				mContentViewPager.setCurrentItem(0, true);
				mCurrentPager = 0;
			}			
		});
		mVehicleTabTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mVehicleTabTextView." + v);
				mStoreTabTextView.setBackgroundResource(R.drawable.btn_grey_default);
				mVehicleTabTextView.setBackgroundResource(R.drawable.btn_grey_pressed);				
				mContentViewPager.setCurrentItem(1, true);
				mCurrentPager = 1;
			}			
		});		
		mBackgroundQueryHandler = new BackgroundQueryHandler(getContentResolver());	
		doActionQuery();
		mCurrentPager = 0;
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
		if (0 == mCurrentPager) {
			startFavoriteStoreListQuery();
		} else if (1 == mCurrentPager) {
			startFavoriteVehicleListQuery();
		}
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
        Cursor cursor = mVehicleListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        mVehicleListAdapter.changeCursor(null);		
        
        cursor = mStoreListAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        mStoreListAdapter.changeCursor(null);		        
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
    private void doActionQuery() {
		if (mQueryDialog != null && mQueryDialog.isShowing()) {
			mQueryDialog.dismiss();
		}    	
        mQueryDialog = new ProgressDialog(this);
        mQueryDialog.setTitle(R.string.query_favorite_label);
        mQueryDialog.setMessage(getString(R.string.quering_favorite_prompt));
        mQueryDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mQueryThread != null && mQueryThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mQueryDialog.setOwnerActivity(this);
        mQueryDialog.show();

        //TODO: do action queryOrder
        QueryRunner queryRunner = new QueryRunner();
        mQueryThread = new Thread(queryRunner);
        mQueryThread.start();
    }	
	
	private void doActionEnterStoreDetail(String storeId, String storeName) {
//		Intent intent = new Intent(FavoriteActivity.this, StoreDetailActivity.class);
//		intent.putExtra(Constants.FROM_WHERE_EXTRA, Constants.FROM_FAVORITE);
//		intent.putExtra(Constants.STORE_ID_EXTRA, storeId);
//		intent.putExtra(Constants.STORE_NAME_EXTRA, storeName);		
//		try {
//			startActivity(intent);
//		} catch (ActivityNotFoundException e) {
//			Log.e(TAG, "doActionEnterFavoriteStoreDetail->", e);
//		}		
	}
		
	// private method section
	private void startFavoriteStoreListQuery() {
		if (DEBUG) Log.d(TAG, "startFavoriteStoreListQuery");
        String selection = null;
		mStoreTabTextView.setBackgroundResource(R.drawable.btn_grey_pressed);
		mVehicleTabTextView.setBackgroundResource(R.drawable.btn_grey_default);	
        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(STORE_LIST_QUERY_TOKEN);
        try {       	
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
            		STORE_LIST_QUERY_TOKEN, null, StoreTable.CONTENT_URI,
                    FavoriteStoreListAdapter.STORE_PROJECTION, selection, null, null);
        } catch (SQLiteException e) {
        	if (DEBUG) Log.e(TAG, "startFavoriteStoreListQuery:" + e);
        }
	}
	
	private void startFavoriteVehicleListQuery() {
		if (DEBUG) Log.d(TAG, "startFavoriteVehicleListQuery");
        String selection = null;
		mStoreTabTextView.setBackgroundResource(R.drawable.btn_grey_default);
		mVehicleTabTextView.setBackgroundResource(R.drawable.btn_grey_pressed);	
        // Cancel any pending queries
        mBackgroundQueryHandler.cancelOperation(VEHICLE_LIST_QUERY_TOKEN);
        try {       	
            // Kick off the new query
            mBackgroundQueryHandler.startQuery(
            		VEHICLE_LIST_QUERY_TOKEN, null, VehicleTable.CONTENT_URI,
                    FavoriteVehicleListAdapter.VEHICLE_PROJECTION, selection, null, null);
        } catch (SQLiteException e) {
        	if (DEBUG) Log.e(TAG, "startFavoriteVehicleListQuery:" + e);
        }
	}	
	
	// private class section
	private class QueryRunner implements Runnable {

		public void run() {
			// query order action
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	        String userName = sharedPreferences.getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, "");
	        String password = sharedPreferences.getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, "");
			String startIndex = "0";
			String endIndex = "100";
			try {
				mQueryFavoriteResult = WebServiceController.queryFavorite(userName, password, startIndex, endIndex, startIndex, endIndex);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			if (DEBUG) Log.d(TAG, "QueryRunner->" + mQueryFavoriteResult);
			
			// Deal with upload result
			Message msg = new Message();
			if (mQueryFavoriteResult != null) {
				if (DEBUG) Log.d(TAG, "QueryRunner->queryOrder success");
        		msg.what = QUERY_SUCCESS;
        		// delete all local data
        		String storeWhere = StoreTable.IS_FAVORITE + " = 1";
        		String vehicleWhere = StoreTable.IS_FAVORITE + " = 1";
        		mContext.getContentResolver().delete(StoreTable.CONTENT_URI, storeWhere, null);
        		mContext.getContentResolver().delete(VehicleTable.CONTENT_URI, vehicleWhere, null);
        		
        		// insert new data
        		List<Store> storeList = mQueryFavoriteResult.mStoreList;
        		if (storeList != null && storeList.size() > 0) {
        			int index = 0;
        			int length = storeList.size();
        			ContentValues[] valuesArray = new ContentValues[length];
        			while (index < length) {
        				Store store = storeList.get(index);
        				ContentValues values = new ContentValues();
        				values.put(StoreTable.REMOTE_ID, store.mId);
        				values.put(StoreTable.NAME, store.mName);
        				values.put(StoreTable.ADDRESS, store.mAddress);
        				values.put(StoreTable.LONGITUDE, store.mLongitude);
        				values.put(StoreTable.LATITUDE, store.mLatitude);
        				values.put(StoreTable.ALTITUDE, store.mAltitude);
        				values.put(StoreTable.IS_FAVORITE, 1);
        				valuesArray[index] = values;
        				index++;
        			}
        			mContext.getContentResolver().bulkInsert(StoreTable.CONTENT_URI, valuesArray);        			
        		}
        		List<Vehicle> vehicleList = mQueryFavoriteResult.mVehicleList;
        		if (vehicleList != null && vehicleList.size() > 0) {
        			int index = 0;
        			int length = vehicleList.size();
        			ContentValues[] valuesArray = new ContentValues[length];
        			while (index < length) {
        				Vehicle vehicle = vehicleList.get(index);
        				ContentValues values = new ContentValues();
        				values.put(VehicleTable.REMOTE_ID, vehicle.mId);
        				values.put(VehicleTable.BRAND, vehicle.mBrand);
        				values.put(VehicleTable.MODEL, vehicle.mModel);
        				values.put(VehicleTable.PRICE, vehicle.mPrice);
        				values.put(VehicleTable.DUMP_ENERGY, vehicle.mDumpEnergy);
        				values.put(VehicleTable.PARKING_GARAGE, vehicle.mParkingGarage);
        				values.put(VehicleTable.PHOTO_URI, vehicle.mPhoto);
        				values.put(VehicleTable.IS_FAVORITE, 1);
        				valuesArray[index] = values;
        				index++;
        			}
        			mContext.getContentResolver().bulkInsert(VehicleTable.CONTENT_URI, valuesArray);        			
        		}        		
			} else {
				msg.what = QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "QueryRunner->queryOrder failure.");
			}
			if (mQueryDialog != null && mQueryDialog.isShowing()) {
				mQueryDialog.dismiss();
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
                case STORE_LIST_QUERY_TOKEN:
                	mStoreListAdapter.changeCursor(cursor);
                    break;
                case VEHICLE_LIST_QUERY_TOKEN:
                	mVehicleListAdapter.changeCursor(cursor);
                    break;                    
            }
        }
    }	
    
	private class MyPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int position) {
			if (DEBUG) Log.d(TAG, "onPageSelected:" + position);
			if (0 == position) {
				startFavoriteStoreListQuery();
			} else if (1 == position) {
				startFavoriteVehicleListQuery();
			}
			mCurrentPager = position;
		}
	}    
	
	class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mPageViewerListItem.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mPageViewerListItem.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mPageViewerListItem.get(arg1));
			return mPageViewerListItem.get(arg1);
		}

		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		public Parcelable saveState() {
			return null;
		}

		public void startUpdate(View arg0) {
		}

		public void finishUpdate(View arg0) {
		}
	}	
}