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

package com.zapple.evshare.service.location;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationUploadTask {
    private static final String TAG = LocationUploadTask.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int SEARCH_LOCATION = 0;
    private static final int UPLOAD_LOCATION = 1;
    private LocationTaskParas mParas;
    private Location mLoc;

    private static HandlerThread sLocationThread;
    static {
        sLocationThread = new HandlerThread("LocationServiceThread");
        sLocationThread.start();
    }
    /* 处理location信息handler */
    private Handler mLocalHandler = new Handler(sLocationThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_LOCATION: {
                    if (DEBUG) Log.d(TAG, "period search location");
                    mLoc = FullLocationManager.newInstance(mParas.mContext).getBestLocation();
                    sendEmptyMessageDelayed(SEARCH_LOCATION, mParas.mPeriod);
                    break;
                }
                case UPLOAD_LOCATION: {
                    if (DEBUG) Log.d(TAG, "period upload location");
                    if (checkLocation()) {
                        String sendData = getSendData();
                        // HttpWebAction.getInstance().getStringByPost(getAddr(),
                        // sendData);
                    }
                    sendEmptyMessageDelayed(UPLOAD_LOCATION, mParas.mPeriod);
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };

    public LocationUploadTask(LocationTaskParas paras) {
        mParas = paras;
    }

    public void kickOff() {
        mLocalHandler.sendEmptyMessage(SEARCH_LOCATION);
        if (mParas.mAutoUpload) {
            mLocalHandler.sendEmptyMessage(UPLOAD_LOCATION);
        }
    }

    private String getAddr() {
        // String header = PropertyMgr.getInstance().getWebAddress();
        // String end =
        // PropertyMgr.getgProperties().getProperty("WEB_ADDR_SAVE");
        // return header + end;
        return "";
    }

    private String getSendData() {
        String result = null;
        try {
            JSONObject json = new JSONObject();
            long timems = System.currentTimeMillis();
            // json.put("account", MSTGlobal.Empid);
            json.put("hidVal", String.valueOf(timems));
            // json.put("syncDate", MSTGlobal.CURRENTLY_DATE);
            json.put("method", "F_VISIT_LOCATION");
            // json.put(MSTGlobal.MOBILE_CLICK_TIME,
            // System.currentTimeMillis());
            {
                JSONObject js = new JSONObject();
                try {
                    js.put(FullLocationManager.JSON_PARA_LAT, mLoc.getLatitude());
                    js.put(FullLocationManager.JSON_PARA_LON, mLoc.getLongitude());
                    if (mLoc.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                        js.put(FullLocationManager.JSON_PARA_CTYPE, "NETWORK_PROVIDER");
                    } else {
                        js.put(FullLocationManager.JSON_PARA_CTYPE, "GPS_PROVIDER");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                json.put("jsonData", js);
            }
            // json.put("date", AGUtils.getyyyyMMddHHmmss(timems));
            result = json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean checkLocation() {
        if ((null != mLoc) && (mLoc.getLatitude() != 0) && (mLoc.getLongitude() != 0)) {
            //long offTime = FullLocationManager.getUTCTime() - mLoc.getTime();
        	long offTime = System.currentTimeMillis() - mLoc.getTime();
            if (offTime > FullLocationManager.TEN_MINUTES) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
