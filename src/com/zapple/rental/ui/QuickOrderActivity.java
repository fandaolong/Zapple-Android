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

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.zapple.rental.R;
import com.zapple.rental.ZappleRentalApp;
import com.zapple.rental.data.Store;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class QuickOrderActivity extends MapActivity {
	private static final String TAG = "QuickOrderActivity";
	private static final boolean DEBUG = true;
	public static final String TITLE_EXTRA = "title_extra";
	public static final String STORE_LIST_EXTRA = "store_list_extra";
	public static final String ENABLE_LIST_BUTTON_EXTRA = "show_list_button_extra";
	
	private TextView mTitleTextView;
	private Button mListButton;	
    // pop view for mark clicked
	static View mPopView = null;
	private static TextView mPopTextView = null;
	static MapView mMapView = null;
	private int mZoomLevel = 0;
	private OverItemT mOverItem = null;	
	
	private String mTitleString;
	private ArrayList<Store> mStoreList;
	private boolean mEnableListButton;
	
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
		setContentView(R.layout.activity_quick_order);
				
		Intent i = getIntent();
		if (i != null) {
			mTitleString = i.getStringExtra(TITLE_EXTRA);
			mStoreList = i.getParcelableArrayListExtra(STORE_LIST_EXTRA);
			mEnableListButton = i.getBooleanExtra(ENABLE_LIST_BUTTON_EXTRA, false);
		}
		ZappleRentalApp app = (ZappleRentalApp)this.getApplication();
		app.getBMapManager().start();
        // 如果使用地图SDK，请初始化地图Activity
        super.initMapActivity(app.getBMapManager());
        
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        mListButton = (Button) findViewById(R.id.list_button);
        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapView.setBuiltInZoomControls(true);
        //设置在缩放动画过程中也显示overlay,默认为不绘制
        mMapView.setDrawOverlayWhenZooming(true);
        GeoPoint point =new GeoPoint((int)(39.90923*1e6), (int)(116.397428*1e6));
        mMapView.getController().setCenter(point);
  
        // 添加ItemizedOverlay
		Drawable marker = getResources().getDrawable(R.drawable.map2);  //得到需要标在地图上的资源
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
				.getIntrinsicHeight());   //为maker定义位置和边界
		
		mOverItem = new OverItemT(marker, this, mStoreList);
		mMapView.getOverlays().add(mOverItem); //添加ItemizedOverlay实例到mMapView			
		
		// 创建点击mark时的弹出泡泡
		mPopView = super.getLayoutInflater().inflate(R.layout.pop_view, null);
		mPopTextView = (TextView) mPopView.findViewById(R.id.pop_text_view);
		mMapView.addView(mPopView,
                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                		null, MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);
		mZoomLevel = mMapView.getZoomLevel();
//		mMapView.regMapViewListener(app.mBMapMan, new MKMapViewListener(){
//			public void onMapMoveFinish() {
//				if(mZoomLevel != mMapView.getZoomLevel())
//				{// 比例尺不一样
//					mZoomLevel = mMapView.getZoomLevel();
//					if(mOverItem.mGeoList.size() > 0)
//						mOverItem.mGeoList.remove(0);
//					//mOverItem.updateOverlay();
////					Drawable marker = getResources().getDrawable(R.drawable.iconmarka);  //得到需要标在地图上的资源
////					marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
////							.getIntrinsicHeight());   //为maker定义位置和边界
////					mMapView.getOverlays().add(new OverItemT(marker, ItemizedOverlayDemo.this, 3));
//				}
//			}
//		});		

		mTitleTextView.setText(mTitleString);
		if (mEnableListButton) {
			mListButton.setVisibility(View.VISIBLE);
			mListButton.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					doActionEnterList();
				}
			});
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_quick_order, menu);
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
		ZappleRentalApp app = (ZappleRentalApp)this.getApplication();
		app.getBMapManager().start();		
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
		ZappleRentalApp app = (ZappleRentalApp)this.getApplication();
		if(app.getBMapManager() != null)
			app.getBMapManager().stop();		
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
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	// private method do action section
	private void doActionEnterList() {
		finish();
	}
	
	class OverItemT extends ItemizedOverlay<OverlayItem> {
		public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		private List<Store> mStoreList;
		private Drawable mMarker;
		private Context mContext;

		private double mLat1 = 39.90923; // point1纬度
		private double mLon1 = 116.357428; // point1经度

		private double mLat2 = 39.90923;
		private double mLon2 = 116.397428;

		private double mLat3 = 39.90923;
		private double mLon3 = 116.437428;

		public OverItemT(Drawable marker, Context context, ArrayList<Store> storeList) {
			super(boundCenterBottom(marker));

			mMarker = marker;
			mContext = context;
			mStoreList = storeList;
			
			if (DEBUG) Log.d(TAG, "OverItemT.storeList." + storeList);
			if (storeList != null) {
				int size = storeList.size();
				int index = 0;
				while (index < size) {
					Store store = storeList.get(index);
					GeoPoint geoPoint = new GeoPoint(
							(int) (store.mLatitude * 1E6), 
							(int) (store.mLongitude * 1E6));
					// OverlayItem: item GEO, title, text
					mGeoList.add(new OverlayItem(geoPoint, store.mName, store.mAddress));
					index++;
					if (DEBUG) Log.d(TAG, "OverItemT.storeList.index." + index + store.mLongitude + store.mLatitude);
				}
			}
			
//			Store store = storeList.get(0);
//			GeoPoint geoPoint = new GeoPoint(
//					(int) (store.mLongitude * 1E6), 
//					(int) (store.mLatitude * 1E6));
//			// OverlayItem: item GEO, title, text
//			mGeoList.add(new OverlayItem(geoPoint, "P1", "point1"));
			
//			// 用给定的经纬度构造GeoPoint，单位是微度 (度 * 1E6)
//			GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
//			GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
//			GeoPoint p3 = new GeoPoint((int) (mLat3 * 1E6), (int) (mLon3 * 1E6));
//			
//			// 构造OverlayItem的三个参数依次为：item的位置，标题文本，文字片段
//			mGeoList.add(new OverlayItem(p1, "P1", "point1"));
//			mGeoList.add(new OverlayItem(p2, "P2", "point2"));
//			mGeoList.add(new OverlayItem(p3, "P3", "point3"));
			populate();  //createItem(int)方法构造item。一旦有了数据，在调用其它方法前，首先调用这个方法
		}

		public void updateOverlay() {
			populate();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
			Projection projection = mapView.getProjection(); 
			for (int index = size() - 1; index >= 0; index--) { // 遍历mGeoList
				OverlayItem overLayItem = getItem(index); // 得到给定索引的item

				String title = overLayItem.getTitle();
				// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
				Point point = projection.toPixels(overLayItem.getPoint(), null); 

				// 可在此处添加您的绘制代码
				Paint paintText = new Paint();
				paintText.setColor(Color.BLUE);
				paintText.setTextSize(15);
				canvas.drawText(title, point.x-30, point.y, paintText); // 绘制文本
			}

			super.draw(canvas, mapView, shadow);
			//调整一个drawable边界，使得（0，0）是这个drawable底部最后一行中心的一个像素
			boundCenterBottom(mMarker);
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return mGeoList.get(i);
		}

		@Override
		public int size() {
			return mGeoList.size();
		}
		
		@Override
		protected boolean onTap(int i) {
			// 处理当点击事件
			setFocus(mGeoList.get(i));
			// 更新气泡位置,并使之显示
			GeoPoint pt = mGeoList.get(i).getPoint();
			mMapView.updateViewLayout(QuickOrderActivity.mPopView,
	                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	                		pt, MapView.LayoutParams.BOTTOM_CENTER));
			mPopView.setVisibility(View.VISIBLE);
			mPopTextView.setText(mGeoList.get(i).getSnippet());
			mPopTextView.setTag(mStoreList.get(i));
			mPopTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Store store = (Store) v.getTag();
					Intent intent = new Intent(QuickOrderActivity.this, StoreDetailActivity.class);
					intent.putExtra(StoreDetailActivity.FROM_WHERE_EXTRA, StoreDetailActivity.FROM_QUICK_ORDER);
					intent.putExtra(StoreDetailActivity.STORE_ID_EXTRA, store.mId);
					intent.putExtra(StoreDetailActivity.STORE_NAME_EXTRA, store.mName);					
					mContext.startActivity(intent);					
				}				
			});
//			Toast.makeText(this.mContext, mGeoList.get(i).getSnippet(),
//					Toast.LENGTH_SHORT).show();
			return true;
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// 消去弹出的气泡
			QuickOrderActivity.mPopView.setVisibility(View.GONE);
			return super.onTap(arg0, arg1);
		}
	}
}