/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.service.location.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.CellLocation;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.zapple.evshare.service.location.network.CellInfoManager.Cell;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public abstract class NwLocationManager {
    private static final String TAG = NwLocationManager.class.getSimpleName();
    private static boolean DEBUG = true;
    private static int CHECK_INTERVAL = 15000;
    private static final int STATE_IDLE = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_COLLECTING = 2;
    private static final int STATE_SENDING = 3;
    private static long mForcePeek = 0;
    private static final long EXPIRES_TIME = CHECK_INTERVAL * 20;

    private static final int MESSAGE_INITIALIZE = 1;
    private static final int MESSAGE_COLLECTING_CELL = 2;
    private static final int MESSAGE_COLLECTING_WIFI = 5;
    private static final int MESSAGE_BEFORE_FINISH = 10;

    private Context mContext;
    private int mBid;
    private List<Cell> mGsmCells;
    private long mStartScanTimestamp;
    private int mState;
    private Task mTask;
    private boolean mWaiting4WifiEnable;
    private boolean mPaused;
    private boolean mDisableWifiAfterScan;
    private CellInfoManager mCellInfoManager;
    private WifiInfoManager mWifiInfoManager;
    private Looper mNwLooper;
    private WinNwHandler mWinNwLocationHandler;
    private final BroadcastReceiver mWifiReceiver;

    public NwLocationManager(Context context, CellInfoManager cellinfomanager, WifiInfoManager wifiinfomanager) {
        mWifiReceiver = new WifiBroadcastReceiver();
        mContext = context;
        mCellInfoManager = cellinfomanager;
        mWifiInfoManager = wifiinfomanager;
        
    }

    private void debug(Object paramObject) {
    	if (DEBUG) Log.d(TAG, paramObject.toString());
    }

    public abstract void onLocationChanged(Location location);

    public void pause() {
        if (mState > STATE_IDLE && !mPaused) {
            mWinNwLocationHandler.removeMessages(MESSAGE_BEFORE_FINISH);
            mPaused = true;
        }
    }

    public void requestUpdate() {
        if (mState != STATE_READY) {
            return;
        }
        boolean bStartScanSuccessful = false;
        
        CellLocation.requestLocationUpdate();
        
        mState = STATE_COLLECTING;
        
        mWinNwLocationHandler.sendEmptyMessage(MESSAGE_INITIALIZE);
        
        mStartScanTimestamp = System.currentTimeMillis();
        
        if (mWifiInfoManager.wifiManager().isWifiEnabled()) {
            debug("wifi enable, start scan now...");
            mWifiInfoManager.wifiManager().startScan();
            mWaiting4WifiEnable = false;
        } else {
            if (true == mWifiInfoManager.wifiManager().setWifiEnabled(true)) {
                int nDelay = 0;
                bStartScanSuccessful = mWifiInfoManager.wifiManager().startScan();
                if (bStartScanSuccessful) {
                    nDelay = 8000;
                }
                mWinNwLocationHandler.sendEmptyMessageDelayed(MESSAGE_COLLECTING_WIFI, nDelay);
                mDisableWifiAfterScan = true;
                debug("enabe wifi now,  delay UPDATE wifi infos");
            } else {
                mWaiting4WifiEnable = true;
            }
        }
    }

    public void resume() {
        if (mState > 0 && mPaused) {
            mPaused = false;
            mWinNwLocationHandler.removeMessages(MESSAGE_BEFORE_FINISH);
            mWinNwLocationHandler.sendEmptyMessage(MESSAGE_BEFORE_FINISH);
        }
    }

    public void start() {
        if (mState <= STATE_IDLE) {
            debug("WinNwLocationManager" + " Starting...");
            mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            
            HandlerThread thread = new HandlerThread("WinNwLocationManagerThread",
                    Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
            // Get the HandlerThread's Looper and use it for our Handler
            mNwLooper = thread.getLooper();
            mWinNwLocationHandler = new WinNwHandler(mNwLooper);
            mState = STATE_READY;
            mPaused = false;
            mWaiting4WifiEnable = false;
            mDisableWifiAfterScan = false;
            debug("CELL LOCATION START");
            requestUpdate();
        }
    }

    public void stop() {
        if (mState > STATE_IDLE) {
            mContext.unregisterReceiver(mWifiReceiver);
            debug("CELL LOCATION STOP");
            mWinNwLocationHandler.removeCallbacksAndMessages(null);
            mWinNwLocationHandler = null;
            mNwLooper.quit();
            mState = STATE_IDLE;
            if (mDisableWifiAfterScan) {
                mDisableWifiAfterScan = false;
                mWifiInfoManager.wifiManager().setWifiEnabled(false);
            }
            mCellInfoManager.quit();
        }
    }

    protected boolean isConnectedWithInternet() {
        ConnectivityManager conManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    private class WinNwHandler extends Handler {
        private float mCellScore;
        private JSONArray mCellTowersJson;

        public WinNwHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INITIALIZE:
                    debug("looper: MESSAGE_INITIALIZE");
                    mCellTowersJson = null;
                    mCellScore = 1.401298E-045F;
                case MESSAGE_COLLECTING_CELL: {
                    debug("looper: MESSAGE_COLLECTING_CELL");
                    if (mState != NwLocationManager.STATE_COLLECTING) {
                        break;
                    }
                    JSONArray cellTowers = mCellInfoManager.cellTowers();
                    float score = mCellInfoManager.score();
                    if (cellTowers != null) {
                        if (score > mCellScore) {
                            this.mCellTowersJson = cellTowers;
                            this.mCellScore = score;
                        }
                    }
                    sendEmptyMessageDelayed(MESSAGE_COLLECTING_CELL, 1000L);
                    break;
                }
                case MESSAGE_COLLECTING_WIFI: {
                    if (mState != NwLocationManager.STATE_COLLECTING) {
                        break;
                    }
                    removeMessages(MESSAGE_COLLECTING_CELL);
                    removeMessages(MESSAGE_BEFORE_FINISH);
                    mState = NwLocationManager.STATE_SENDING;
                    if (mTask != null) {
                        mTask.cancel(true);
                    }
                    List<Cell> cells = mCellInfoManager.dumpCells();

                    int nBid = mCellInfoManager.bid();
                    mTask = new Task(cells, nBid);
                    JSONArray[] aryJsonArray = new JSONArray[2];
                    aryJsonArray[0] = mCellTowersJson;
                    aryJsonArray[1] = mWifiInfoManager.wifiTowers();
                    debug("Post json");
                    mTask.execute(aryJsonArray);
                    break;
                }
                case MESSAGE_BEFORE_FINISH: {
                    debug("MESSAGE_BEFORE_FINISH");
                    mForcePeek += NwLocationManager.CHECK_INTERVAL;
                    if (mState != NwLocationManager.STATE_READY || mPaused) {
                        break;
                    }

                    if (mDisableWifiAfterScan && mWifiInfoManager.wifiManager().setWifiEnabled(false)) {
                        mDisableWifiAfterScan = false;
                    }
                    if (!mCellInfoManager.isGsm()) {
                        if (mBid != mCellInfoManager.bid()) {
                            requestUpdate();
                            mForcePeek = 0;
                        } else {
                            if (mForcePeek > EXPIRES_TIME) {
                                requestUpdate();
                                mForcePeek = 0;
                            } else {
                                sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH, NwLocationManager.CHECK_INTERVAL);
                            }
                        }
                    } else {
                        if (mGsmCells == null || mGsmCells.size() == 0) {
                            //sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH, WinNwLocationManager.CHECK_INTERVAL);
                            requestUpdate();
                            mForcePeek = 0;
                            break;
                        } else {
                            List<Cell> cells = mCellInfoManager.dumpCells();
                            if (cells.size() > 0) {
                                if (0 == cells.get(0).compareTo(mGsmCells.get(0))) {
                                    if (mForcePeek > EXPIRES_TIME) {
                                        requestUpdate();
                                        mForcePeek = 0;
                                    } else {
                                        sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH, NwLocationManager.CHECK_INTERVAL);
                                    }
                                    break;
                                } else {
                                    debug("PRIMARY CELL CHANGED, need update now...");
                                    requestUpdate();
                                    mForcePeek = 0;
                                }
                            } else {
                                if (mForcePeek > EXPIRES_TIME) {
                                    requestUpdate();
                                    mForcePeek = 0;
                                } else {
                                    sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH, NwLocationManager.CHECK_INTERVAL);
                                }
                                break;
                            }
                        }
                    }
                }
                default:
                    break;
            }
        }
    }

    class Task extends AsyncTask<JSONArray, Void, Void> {
        int bid;
        List<Cell> cells;
        Location location;

        public Task(List<Cell> cells, int bid) {
            this.cells = cells;
            this.bid = bid;
        }

        @Override
        protected Void doInBackground(JSONArray... params) {
            try {
                JSONObject js = new JSONObject();
                js.put("version", "1.1.0");
                js.put("host", "maps.google.com");
                js.put("address_language", "zh_CN");
                js.put("request_address", true);
                if (mCellInfoManager.isCdma()) {
                    js.put("radio_type", "cdma");
                } else {
                    js.put("radio_type", "gsm");
                }
                js.put("cell_towers", params[0]);
                js.put("wifi_towers", params[1]);
                DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
                HttpPost localHttpPost = new HttpPost("http://www.google.com/loc/json");
                String strJson = js.toString();
                debug(strJson);
                StringEntity objJsonEntity = new StringEntity(strJson);
                localHttpPost.setEntity(objJsonEntity);
                HttpResponse objResponse = localDefaultHttpClient.execute(localHttpPost);

                int nStateCode = objResponse.getStatusLine().getStatusCode();
                HttpEntity httpEntity = objResponse.getEntity();
                byte[] arrayOfByte = null;
                if (nStateCode / 100 == 2) {
                    arrayOfByte = EntityUtils.toByteArray(httpEntity);
                }
                httpEntity.consumeContent();
                String strResponse = new String(arrayOfByte, "UTF-8");
                js = new JSONObject(strResponse);
                debug("rsp:" + js);

                location = new Location(LocationManager.NETWORK_PROVIDER);
                location.setLatitude(js.getJSONObject("location").getDouble("latitude"));
                location.setLongitude(js.getJSONObject("location").getDouble("longitude"));
                location.setAccuracy(js.getJSONObject("location").getInt("accuracy"));
                //location.setTime(WinLocationManager.getUTCTime());
                location.setTime(System.currentTimeMillis());
            } catch (Exception localException) {
                return null;
            }
            return null;
        }

        public void onPostExecute(Void paramVoid) {
            if (mState != NwLocationManager.STATE_SENDING || mTask != this) {
                return;
            }
            if ((null != location) && (location.getLatitude() != 0.0D) && (location.getLongitude() != 0.0D)) {
                mGsmCells = this.cells;
                mBid = this.bid;
                StringBuilder sb = new StringBuilder("CELL LOCATION DONE: (");
                sb.append(location.getLatitude()).append(",").append(location.getLongitude()).append(")");
                debug(sb.toString());
                mState = STATE_READY;
                mWinNwLocationHandler.sendEmptyMessage(MESSAGE_BEFORE_FINISH);
                onLocationChanged(location);
            } else {
                mTask = null;
                mState = NwLocationManager.STATE_READY;
                mWinNwLocationHandler.sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH, 5000L);
            }
        }
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NwLocationManager.this.mState != NwLocationManager.STATE_COLLECTING) {
                return;
            }
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                debug("WIFI SCAN COMPLETE");
                mWinNwLocationHandler.removeMessages(MESSAGE_COLLECTING_WIFI);
                long scanInterval = System.currentTimeMillis() - mStartScanTimestamp;
                if (scanInterval > 4000L) {
                    mWinNwLocationHandler.sendEmptyMessageDelayed(MESSAGE_COLLECTING_WIFI, 4000L);
                } else {
                    mWinNwLocationHandler.sendEmptyMessage(MESSAGE_COLLECTING_WIFI);
                }
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (!mWaiting4WifiEnable) {
                    return;
                }
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    mWifiInfoManager.wifiManager().startScan();
                    mDisableWifiAfterScan = true;
                    mPaused = false;
                    debug("WIFI ENABLED");
                }
            }
        }
    }
}
