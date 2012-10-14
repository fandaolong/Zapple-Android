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

package com.zapple.evshare.service.location.gps;

import com.zapple.evshare.service.location.FullLocationManager;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public abstract class GpxLocationManager {
    private final static String TAG = GpxLocationManager.class.getSimpleName();
    private static final boolean DEBUG = true;
    private final static int START = 0;
    private final static int UPDATE = 1;
    private final static int STOP = 2;
    private Context mContext;
    private LocationManager mLocationManager;
    private Location mLastLocation;
    private Looper mGpxLooper;
    private WinGpxHandler mWinGpxLocationHandler;
    private long mMinTime = FullLocationManager.TWO_MINUTES;
    private float mMinDistance = FullLocationManager.MIN_DISTANCE;
    private long mInterVal = -1; // not get the GPS location timely.
    private boolean mInListen = false;

    public GpxLocationManager(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public abstract void onLocationChanged(Location location);

    public void prepare(long time, float distance, long interval) {
        if (DEBUG) Log.d(TAG, "gpx location prepare");
        if (-1 != time) {
            mMinTime = time;
        }
        if (-1 != distance) {
            mMinDistance = distance;
        }
        if (-1 != interval) {
            mInterVal = interval;
        }
    }

    public void reset(long time, float distance, long interval) {
        stop();
        prepare(time, distance, interval);
        start();
    }

    public void start() {
        if (DEBUG) Log.d(TAG, "gpx location start");
        openGps();
        HandlerThread thread = new HandlerThread("WinGpxLocationManagerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mGpxLooper = thread.getLooper();
        mWinGpxLocationHandler = new WinGpxHandler(mGpxLooper);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (FullLocationManager.isBetterLocation(location, mLastLocation)) {
            mLastLocation = location;
            onLocationChanged(location);
        }
        mWinGpxLocationHandler.sendEmptyMessage(START);
    }

    public void stop() {
        if (DEBUG) Log.d(TAG, "gpx location stop");
        if (true == mInListen) {
            mLocationManager.removeUpdates(mLocationListener);
            setListenStatus(false);
        }
        mWinGpxLocationHandler.removeCallbacksAndMessages(null);
        mGpxLooper.quit();
    }

    public void pause() {
        if (true == mInListen) {
            mLocationManager.removeUpdates(mLocationListener);
            setListenStatus(false);
        }
        mWinGpxLocationHandler.removeCallbacksAndMessages(null);
    }

    public void resume() {
        if (DEBUG) Log.d(TAG, "gpx location resume");
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, mLocationListener);
        setListenStatus(true);
        if (-1 != mInterVal) {
            mWinGpxLocationHandler.sendEmptyMessageDelayed(UPDATE, mInterVal);
        }
    }

    private class WinGpxHandler extends Handler {
        public WinGpxHandler(Looper mGpxLooper) {
            super(mGpxLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) Log.d(TAG, "WinGpxLooper what:" + msg.what);
            switch (msg.what) {
                case START:
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, mLocationListener);
                    setListenStatus(true);
                    if (-1 != mInterVal) {
                        mWinGpxLocationHandler.sendEmptyMessageDelayed(UPDATE, mInterVal);
                    }
                    break;
                case UPDATE:
                    if (false == mInListen) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, mMinDistance, mLocationListener);
                        setListenStatus(true);
                    }
                    mWinGpxLocationHandler.sendEmptyMessageDelayed(UPDATE, mInterVal);
                    break;
                case STOP:
                    break;
            }
        }
    }

    private void setListenStatus(boolean listen) {
        mInListen = listen;
        if (mInListen) {
            if (DEBUG) Log.d(TAG, "start listen gps");
        } else {
            if (DEBUG) Log.d(TAG, "stop listen gps");
        }
    }

    private void openGps() {
        boolean isEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isEnabled) {
            if (DEBUG) Log.e(TAG, "the gps is not enale...");
            Intent gpsIntent = new Intent();
            gpsIntent.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
            gpsIntent.setData(Uri.parse("custom:3"));
            try {
                PendingIntent.getBroadcast(mContext, 0, gpsIntent, 0).send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (DEBUG) Log.d(TAG, "LocationListener, onLocationChanged , stop listen");
            if (FullLocationManager.isBetterLocation(location, mLastLocation)) {
                if (DEBUG) Log.d(TAG, "onLocationChanged, better location received.");
                location.setTime(System.currentTimeMillis());
                mLastLocation = location;
                GpxLocationManager.this.onLocationChanged(location);
            }
            mLocationManager.removeUpdates(mLocationListener);
            setListenStatus(false);
        }

        @Override
        public void onProviderDisabled(String s) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
    };
}
