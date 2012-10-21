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
package com.zapple.evshare;

//import com.baidu.mapapi.BMapManager;
//import com.baidu.mapapi.MKEvent;
//import com.baidu.mapapi.MKGeneralListener;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;

import android.app.Application;
import android.content.ContentValues;
import android.util.Log;
import android.widget.Toast;

/**
 * This application providers the application initialize, clean 
 * and deal other common case.
 */
public class EvShareApp extends Application {
	private static final String TAG = EvShareApp.class.getSimpleName();
	private static final boolean DEBUG = true;

	private static boolean sIsLogined = false;
	private static EvShareApp sZappleRentalApp = null;
	//�ٶ�MapAPI�Ĺ�����
	private static BMapManager sBMapManager = null;
	
	// ��ȨKey
	// TODO: ����������Key,
	// �����ַ��http://dev.baidu.com/wiki/static/imap/key/
	String mStrKey = "FC92852DC62913E3A3566CE81BE7AE0226571711";
	boolean m_bKeyRight = true;	// ��ȨKey��ȷ����֤ͨ��
	
    @Override
    public void onCreate() {
		if (DEBUG) Log.d(TAG, "onCreate");
    	super.onCreate();
    	
    	sZappleRentalApp = this;
    	
    	// Load the default preference values.
    	sIsLogined = false;
    	// Initialize manager, parser, controller, cache, config, ... , etc.
		sBMapManager = new BMapManager(this);
		sBMapManager.init(this.mStrKey, new MyGeneralListener());
		sBMapManager.getLocationManager().setNotifyInternal(10, 5);
//		if (sBMapManager != null) {
//			sBMapManager.destroy();
//			sBMapManager = null;
//		}    			
    }
    
    @Override
    public void onTerminate() {
    	if (DEBUG) Log.d(TAG, "onTerminate");
    	// Clean storage, memory, cache, ... , etc.
		if (sBMapManager != null) {
			sBMapManager.destroy();
			sBMapManager = null;
		}
    }
    
    // public method section
    public synchronized static EvShareApp getApplication() {
    	return sZappleRentalApp;
    }
    
    public synchronized BMapManager getBMapManager() {
    	return sBMapManager;
    }
    
    // private method do action section
    
	// private method section
	
	// private class section     
    
	// �����¼���������������ͨ�������������Ȩ��֤�����
	static class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
			Toast.makeText(EvShareApp.sZappleRentalApp.getApplicationContext(), "���������������",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onGetPermissionState(int iError) {
			Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
			if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
				// ��ȨKey����
				Toast.makeText(EvShareApp.sZappleRentalApp.getApplicationContext(), 
						"����BMapApiDemoApp.java�ļ�������ȷ����ȨKey��",
						Toast.LENGTH_LONG).show();
				EvShareApp.sZappleRentalApp.m_bKeyRight = false;
			}
		}
	}    
	
    public synchronized static boolean isLogined() {
    	if (DEBUG) Log.v(TAG, "sIsLogined." + sIsLogined);
        return sIsLogined;
    }
    
    public synchronized void setLogined(boolean isLogined) {
    	if (DEBUG) Log.v(TAG, "sIsLogined." + sIsLogined);
        sIsLogined = isLogined;
    }
}