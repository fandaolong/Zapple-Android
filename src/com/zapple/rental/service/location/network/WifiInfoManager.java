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

package com.zapple.rental.service.location.network;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WifiInfoManager {
	private static final String TAG = WifiInfoManager.class.getSimpleName();
	private static final boolean DEBUG = true;
    private WifiManager mWifiManager;

    public WifiInfoManager(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public List<WinWifiInfo> dump() {
        if (!mWifiManager.isWifiEnabled()) {
            return new ArrayList<WinWifiInfo>();
        }
        WifiInfo wifiConnection = mWifiManager.getConnectionInfo();
        WinWifiInfo activeWifi = null;
        if (wifiConnection != null) {
            String bssId = wifiConnection.getBSSID();
            int rssi = wifiConnection.getRssi();
            String ssId = wifiConnection.getSSID();
            activeWifi = new WinWifiInfo(bssId, rssi, ssId);
        }
        ArrayList<WinWifiInfo> allWifiInfo = new ArrayList<WinWifiInfo>();
        if (activeWifi != null) {
            allWifiInfo.add(activeWifi);
        }
        List<ScanResult> lsScanResult = mWifiManager.getScanResults();
        for (ScanResult result : lsScanResult) {
            WinWifiInfo scanWifi = new WinWifiInfo(result);
            if (!scanWifi.equals(activeWifi))
                allWifiInfo.add(scanWifi);
        }
        return allWifiInfo;
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public JSONArray wifiInfo() {
        JSONArray jsa = new JSONArray();
        for (WinWifiInfo wifi : dump()) {
            JSONObject js = wifi.info();
            jsa.put(js);
        }
        return jsa;
    }

    public WifiManager wifiManager() {
        return mWifiManager;
    }

    public JSONArray wifiTowers() {
        JSONArray jsa = new JSONArray();
        try {
            for (WinWifiInfo info : dump()) {
                jsa.put(info.wifi_tower());
            }
        } catch (Exception localException) {
        	if (DEBUG) Log.e("location", "", localException);
        }
        return jsa;
    }

    public class WinWifiInfo implements Comparable<WinWifiInfo> {
        public int compareTo(WinWifiInfo wifiinfo) {
            return wifiinfo.dBm - dBm;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else {
                if (obj instanceof WinWifiInfo) {
                    WinWifiInfo wifiinfo = (WinWifiInfo) obj;
                    if (this.ssid == wifiinfo.ssid && this.bssid.equals(wifiinfo.bssid)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public int hashCode() {
            int i = dBm;
            int j = bssid.hashCode();
            return i ^ j;
        }

        public JSONObject info() {
            JSONObject js = new JSONObject();
            try {
                js.put("mac", bssid);
                js.put("ssid", ssid);
                js.put("dbm", dBm);
            } catch (Exception ex) {
            }
            return js;
        }

        public JSONObject wifi_tower() {
            JSONObject js = new JSONObject();
            try {
                js.put("mac_address", bssid);
                js.put("signal_strength", dBm);
                js.put("ssid", ssid);
                js.put("age", 0);
            } catch (Exception ex) {
            }
            return js;
        }

        public final String bssid;
        public final int dBm;
        public final String ssid;

        public WinWifiInfo(ScanResult scanresult) {
            bssid = scanresult.BSSID;
            dBm = scanresult.level;
            ssid = scanresult.SSID;
        }

        public WinWifiInfo(String bssId, int rssi, String ssId) {
            this.bssid = bssId;
            this.dBm = rssi;
            this.ssid = ssId;
        }
    }
}
