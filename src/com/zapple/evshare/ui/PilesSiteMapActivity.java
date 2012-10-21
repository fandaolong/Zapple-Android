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
import java.util.HashMap;
import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.zapple.evshare.EvShareApp;
import com.zapple.evshare.R;
import com.zapple.evshare.data.GetElecStationsResult;
import com.zapple.evshare.data.Station;
import com.zapple.evshare.transaction.WebServiceController;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PilesSiteMapActivity extends MapActivity {
	private static final String TAG = "PilesSiteMapActivity";
	private static final boolean DEBUG = true;
	public static final String TITLE_EXTRA = "title_extra";
	public static final String ENABLE_LIST_BUTTON_EXTRA = "show_list_button_extra";
	private static final int MSG_QUERY_SUCCESS = 1;
	private static final int MSG_QUERY_FAILURE = 2;
	private static final int MSG_QUERY_STATION = 3;
	
	private MKSearch mMKSearch;
	private TextView mTitleTextView;
	private Button mListButton;	
    // pop view for mark clicked
	static View mPopView = null;
	private static TextView mPopTextView = null;
	static MapView mMapView = null;
	private int mZoomLevel = 0;
	private OverItemT mOverItem = null;	
	private Context mContext;
	private String mCurrentCityName;
	private ArrayList<Station> mStationList;
    private ProgressDialog mGetStationDialog;
    private Thread mGetStationThread = null;	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_QUERY_SUCCESS: {
			        // ���ItemizedOverlay
					Drawable marker = getResources().getDrawable(R.drawable.map2);  //�õ���Ҫ���ڵ�ͼ�ϵ���Դ
					marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
							.getIntrinsicHeight());   //Ϊmaker����λ�úͱ߽�
					mOverItem = new OverItemT(marker, mContext, mStationList);
					mMapView.getOverlays().add(mOverItem); //���ItemizedOverlayʵ����mMapView
					
					// �������markʱ�ĵ�������
//					mPopView = mContext.getLayoutInflater().inflate(R.layout.pop_view, null);
//					mPopTextView = (TextView) mPopView.findViewById(R.id.pop_text_view);
//					mMapView.addView(mPopView,
//			                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
//			                		null, MapView.LayoutParams.TOP_LEFT));					
                    Toast.makeText(mContext, R.string.get_station_success_label, 
                   		 Toast.LENGTH_SHORT).show();					
					break;
				}
				case MSG_QUERY_FAILURE: {
                    Toast.makeText(mContext, R.string.get_station_failure_label, 
                      		 Toast.LENGTH_SHORT).show();					
					break;
				}		
				case MSG_QUERY_STATION: {
					break;
				}				
			}
		}
	};
    /** ��¼��ǰ��γ�ȵ�MAP*/
    private HashMap<String, Double> mCurLocation = new HashMap<String, Double>();
    /** ע�ᶨλ�¼� */
    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (location != null) {
                try {
                    int longitude = (int) (1e6 * location.getLongitude());
                    int latitude = (int) (1e6 * location.getLatitude());

                    /** ���浱ǰ��γ�� */
                    mCurLocation.put("longitude", location.getLongitude());
                    mCurLocation.put("latitude", location.getLatitude());

                    GeoPoint point = new GeoPoint(latitude, longitude);
                    mMapView.getController().setCenter(point);
                    /** ��ѯ�þ�γ��ֵ����Ӧ�ĵ�ַλ����Ϣ */
                    mMKSearch.reverseGeocode(new GeoPoint(latitude, longitude));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };	
	
//	private String mTitleString;
//	private boolean mEnableListButton;
	
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
		setContentView(R.layout.piles_site_layout);
				
		mContext = this;
		EvShareApp app = EvShareApp.getApplication();
		app.getBMapManager().start();
        // ���ʹ�õ�ͼSDK�����ʼ����ͼActivity
        super.initMapActivity(app.getBMapManager());
        
        mMKSearch = new MKSearch();  
        mMKSearch.init(app.getBMapManager(), new MySearchListener());        
        
        mTitleTextView = (TextView) findViewById(R.id.title_text_view);
        mListButton = (Button) findViewById(R.id.list_button);
        mMapView = (MapView)findViewById(R.id.bmapView);
        mMapView.setBuiltInZoomControls(true);
        //���������Ŷ���������Ҳ��ʾoverlay,Ĭ��Ϊ������
        mMapView.setDrawOverlayWhenZooming(true);
//        GeoPoint point =new GeoPoint((int)(39.90923*1e6), (int)(116.397428*1e6));
//        mMapView.getController().setCenter(point);
  
        // ���ItemizedOverlay
		Drawable marker = getResources().getDrawable(R.drawable.map2);  //�õ���Ҫ���ڵ�ͼ�ϵ���Դ
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
				.getIntrinsicHeight());   //Ϊmaker����λ�úͱ߽�
		
		mOverItem = new OverItemT(marker, this, mStationList);
		mMapView.getOverlays().add(mOverItem); //���ItemizedOverlayʵ����mMapView
		
		// �������markʱ�ĵ�������
		mPopView=super.getLayoutInflater().inflate(R.layout.pop_view, null);
		mPopTextView = (TextView) mPopView.findViewById(R.id.pop_text_view);
		mMapView.addView(mPopView,
                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                		null, MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);
		mZoomLevel = mMapView.getZoomLevel();
//		mMapView.regMapViewListener(app.mBMapMan, new MKMapViewListener(){
//			public void onMapMoveFinish() {
//				if(mZoomLevel != mMapView.getZoomLevel())
//				{// �����߲�һ��
//					mZoomLevel = mMapView.getZoomLevel();
//					if(mOverItem.mGeoList.size() > 0)
//						mOverItem.mGeoList.remove(0);
//					//mOverItem.updateOverlay();
////					Drawable marker = getResources().getDrawable(R.drawable.iconmarka);  //�õ���Ҫ���ڵ�ͼ�ϵ���Դ
////					marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
////							.getIntrinsicHeight());   //Ϊmaker����λ�úͱ߽�
////					mMapView.getOverlays().add(new OverItemT(marker, ItemizedOverlayDemo.this, 3));
//				}
//			}
//		});		
		
		// find view section
//		mAccountEditText = (EditText) findViewById(R.id.account_edit_text);
//
//		mLoginButton.setOnClickListener(new View.OnClickListener() {			
//			public void onClick(View v) {
//				doActionLogin();
//			}
//		});
		Intent i = getIntent();
		if (i != null) {
//			mTitleString = i.getStringExtra(TITLE_EXTRA);
//			mEnableListButton = i.getBooleanExtra(ENABLE_LIST_BUTTON_EXTRA, false);
		}
		
		mListButton.setOnClickListener(new View.OnClickListener() {				
			@Override
			public void onClick(View v) {
				doActionEnterList();
			}
		});
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_quick_order, menu);
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
		EvShareApp app = (EvShareApp)this.getApplication();
		BMapManager mapManager = app.getBMapManager();
		mapManager.getLocationManager().requestLocationUpdates(mLocationListener);
		mapManager.start();		
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
		EvShareApp app = (EvShareApp)this.getApplication();
		BMapManager mapManager = app.getBMapManager();
		if(mapManager != null) {
			mapManager.getLocationManager().removeUpdates(mLocationListener);
			mapManager.stop();
		}					
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
//		Intent intent = new Intent(PilesSiteMapActivity.this, PilesSiteListActivity.class);
//		intent.putExtra(PilesSiteListActivity.CITY_NAME_EXTRA, mCurrentCityName);
//		try {
//			startActivity(intent);
//			finish();
//		} catch (ActivityNotFoundException e) {
//			Log.e(TAG, "doActionEnterScore->", e);
//		}
	}
	
    private void doActionGetStation(String cityName) {
		if (mGetStationDialog != null && mGetStationDialog.isShowing()) {
			mGetStationDialog.dismiss();
		}    	
        mGetStationDialog = new ProgressDialog(this);
        mGetStationDialog.setTitle(R.string.get_station_label);
        mGetStationDialog.setMessage(getString(R.string.get_station_prompt));
        mGetStationDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mGetStationThread != null && mGetStationThread.isInterrupted()) {
                    // indicate thread should cancel
                }
            }
        });
        mGetStationDialog.setOwnerActivity(this);
        mGetStationDialog.show();

        // do action getStore
        GetStationRunner getStoreRunner = new GetStationRunner(cityName);
        mGetStationThread = new Thread(getStoreRunner);
        mGetStationThread.start();
    }
	
	class OverItemT extends ItemizedOverlay<OverlayItem> {
		public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		private List<Station> mStationList;
		private Drawable mMarker;
		private Context mContext;

		private double mLat1 = 39.90923; // point1γ��
		private double mLon1 = 116.357428; // point1����

		private double mLat2 = 39.90923;
		private double mLon2 = 116.397428;

		private double mLat3 = 39.90923;
		private double mLon3 = 116.437428;

		public OverItemT(Drawable marker, Context context, ArrayList<Station> stationList) {
			super(boundCenterBottom(marker));

			mMarker = marker;
			mContext = context;
			mStationList = stationList;
			
			if (DEBUG) Log.d(TAG, "OverItemT.storeList." + stationList);
			if (stationList != null) {
				int size = stationList.size();
				int index = 0;
				while (index < size) {
					Station station = stationList.get(index);
					GeoPoint geoPoint = new GeoPoint(
							(int) (station.mLatitude * 1E6), 
							(int) (station.mLongitude * 1E6));
					// OverlayItem: item GEO, title, text
					mGeoList.add(new OverlayItem(geoPoint, station.mName, station.mAddress));
					index++;
					if (DEBUG) Log.d(TAG, "OverItemT.storeList.index." + index + station.mLongitude + station.mLatitude);
				}
			}			
//			// �ø����ľ�γ�ȹ���GeoPoint����λ��΢�� (�� * 1E6)
//			GeoPoint p1 = new GeoPoint((int) (mLat1 * 1E6), (int) (mLon1 * 1E6));
//			GeoPoint p2 = new GeoPoint((int) (mLat2 * 1E6), (int) (mLon2 * 1E6));
//			
//			// ����OverlayItem��������������Ϊ��item��λ�ã������ı�������Ƭ��
//			mGeoList.add(new OverlayItem(p1, "P1", "point1"));
//			mGeoList.add(new OverlayItem(p2, "P2", "point2"));
//			if(count == 3)
//			{
//				GeoPoint p3 = new GeoPoint((int) (mLat3 * 1E6), (int) (mLon3 * 1E6));
//				mGeoList.add(new OverlayItem(p3, "P3", "point3"));
//			}
			populate();  //createItem(int)��������item��һ���������ݣ��ڵ�����������ǰ�����ȵ����������
		}

		public void updateOverlay() {
			populate();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// Projection�ӿ�������Ļ��������;�γ������֮��ı任
			Projection projection = mapView.getProjection(); 
			for (int index = size() - 1; index >= 0; index--) { // ����mGeoList
				OverlayItem overLayItem = getItem(index); // �õ�����������item

				String title = overLayItem.getTitle();
				// �Ѿ�γ�ȱ任�������MapView���Ͻǵ���Ļ��������
				Point point = projection.toPixels(overLayItem.getPoint(), null); 

				// ���ڴ˴�������Ļ��ƴ���
				Paint paintText = new Paint();
				paintText.setColor(Color.BLUE);
				paintText.setTextSize(15);
				canvas.drawText(title, point.x-30, point.y, paintText); // �����ı�
			}

			super.draw(canvas, mapView, shadow);
			//����һ��drawable�߽磬ʹ�ã�0��0�������drawable�ײ����һ�����ĵ�һ������
			boundCenterBottom(mMarker);
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return mGeoList.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return mGeoList.size();
		}
		@Override
		// ��������¼�
		protected boolean onTap(int i) {
			setFocus(mGeoList.get(i));
			// ��������λ��,��ʹ֮��ʾ
			GeoPoint pt = mGeoList.get(i).getPoint();
			PilesSiteMapActivity.mMapView.updateViewLayout(PilesSiteMapActivity.mPopView,
	                new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
	                		pt, MapView.LayoutParams.BOTTOM_CENTER));
			PilesSiteMapActivity.mPopView.setVisibility(View.VISIBLE);
			PilesSiteMapActivity.mPopTextView.setText(mGeoList.get(i).getSnippet());
			mPopTextView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
//					Intent intent = new Intent(PilesSiteMapActivity.this, StoreDetailActivity.class);
//					intent.putExtra(StoreDetailActivity.FROM_WHERE_EXTRA, StoreDetailActivity.FROM_QUICK_ORDER);
//					mContext.startActivity(intent);					
				}				
			});
//			Toast.makeText(this.mContext, mGeoList.get(i).getSnippet(),
//					Toast.LENGTH_SHORT).show();
			return true;
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// TODO Auto-generated method stub
			// ��ȥ����������
			PilesSiteMapActivity.mPopView.setVisibility(View.GONE);
			return super.onTap(arg0, arg1);
		}
	}
	
	  
    /** 
     * �ڲ���ʵ��MKSearchListener�ӿ�,����ʵ���첽�������� 
     *  
     * @author liufeng 
     */  
    public class MySearchListener implements MKSearchListener {  
        /** 
         * ���ݾ�γ��������ַ��Ϣ��� 
         *  
         * @param result ������� 
         * @param iError ����ţ�0��ʾ��ȷ���أ� 
         */  
        @Override  
        public void onGetAddrResult(MKAddrInfo result, int iError) {  
            if (result == null) {  
                return;  
            }  
            StringBuffer sb = new StringBuffer();  
            // ��γ������Ӧ��λ��   
            sb.append(result.strAddr).append("/n");  
            MKGeocoderAddressComponent mk = result.addressComponents;
            if (mk != null) {
            	sb.append(mk.city).append("/n");
            }
            // �жϸõ�ַ�����Ƿ���POI��Point of Interest,����Ȥ�㣩   
            if (null != result.poiList) {  
                // �������е���Ȥ����Ϣ   
                for (MKPoiInfo poiInfo : result.poiList) {  
                    sb.append("----------------------------------------").append("/n");  
                    sb.append("���ƣ�").append(poiInfo.name).append("/n");  
                    sb.append("��ַ��").append(poiInfo.address).append("/n");  
                    sb.append("���ȣ�").append(poiInfo.pt.getLongitudeE6() / 1000000.0f).append("/n");  
                    sb.append("γ�ȣ�").append(poiInfo.pt.getLatitudeE6() / 1000000.0f).append("/n");  
                    sb.append("�绰��").append(poiInfo.phoneNum).append("/n");  
                    sb.append("�ʱࣺ").append(poiInfo.postCode).append("/n");  
                    // poi���ͣ�0����ͨ�㣬1������վ��2��������·��3������վ��4��������·   
                    sb.append("���ͣ�").append(poiInfo.ePoiType).append("/n");  
                }  
            }  
            Log.d(TAG, "onGetAddrResult->" + sb.toString());
            // ����ַ��Ϣ����Ȥ����Ϣ��ʾ��TextView��   
//            addressTextView.setText(sb.toString());  
            if (mCurrentCityName == null || (mCurrentCityName != null && !mCurrentCityName.equalsIgnoreCase(mk.city))) {
            	doActionGetStation(mk.city);
            }
            mCurrentCityName = mk.city;      
        }  
  
        /** 
         * �ݳ�·��������� 
         *  
         * @param result ������� 
         * @param iError ����ţ�0��ʾ��ȷ���أ� 
         */  
        @Override  
        public void onGetDrivingRouteResult(MKDrivingRouteResult result, int iError) {  
        }  
  
        /** 
         * POI�����������Χ����������POI�������ܱ߼����� 
         *  
         * @param result ������� 
         * @param type ���ؽ�����ͣ�11,12,21:poi�б� 7:�����б� 
         * @param iError ����ţ�0��ʾ��ȷ���أ� 
         */  
        @Override  
        public void onGetPoiResult(MKPoiResult result, int type, int iError) {  
        	MKPoiInfo mkPointInfo = result.getPoi(0);
        	Log.d(TAG, "onGetAddrResult->" + result.getAllPoi());
        	Log.d(TAG, "onGetAddrResult->" + mkPointInfo != null ? mkPointInfo.city : null);
        }  
  
        /** 
         * ��������·��������� 
         *  
         * @param result ������� 
         * @param iError ����ţ�0��ʾ��ȷ���أ� 
         */  
        @Override  
        public void onGetTransitRouteResult(MKTransitRouteResult result, int iError) {  
        }  
  
        /** 
         * ����·��������� 
         *  
         * @param result ������� 
         * @param iError ����ţ�0��ʾ��ȷ���أ� 
         */  
        @Override  
        public void onGetWalkingRouteResult(MKWalkingRouteResult result, int iError) {  
        }

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}  
    }  	
    
	private class GetStationRunner implements Runnable {
		private String mCityName;
		
		GetStationRunner(String cityName) {
			mCityName = cityName;
		}
		public void run() {
			GetElecStationsResult result = null;
			String startIndex = "0";
			String endIndex = "100";
			try {
				result = WebServiceController.getElecStations(mCityName, startIndex, endIndex);
			} catch (Exception e) {
				e.printStackTrace();
			}				
			
			// Deal with get station result
			Message msg = new Message();
			if (result != null && TextUtils.isEmpty(result.mResult)) {
				mStationList = result.mStationList;
				if (DEBUG) Log.d(TAG, "GetStationRunner->getElecStations success");
        		msg.what = MSG_QUERY_SUCCESS;
			} else {
				msg.what = MSG_QUERY_FAILURE;
				if (DEBUG) Log.d(TAG, "GetStationRunner->getElecStations failure.");
			}
			if (mGetStationDialog != null && mGetStationDialog.isShowing()) {
				mGetStationDialog.dismiss();
			}
			mHandler.sendMessage(msg);
		}		
	}    
}