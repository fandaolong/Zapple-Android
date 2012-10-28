/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.service.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import com.zapple.evshare.service.location.gps.GpxLocationManager;
import com.zapple.evshare.service.location.network.CellInfoManager;
import com.zapple.evshare.service.location.network.NwLocationManager;
import com.zapple.evshare.service.location.network.WifiInfoManager;

public class FullLocationManager {
    private static final String TAG = FullLocationManager.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static FullLocationManager sFullLocationManager;
    private NwLocationManager mNwLocationManager;
    private GpxLocationManager mGpxLocationManager;
    private Location mLastBestLocation;
    private Context mContext;
    private boolean mGpxStart = false;
    private boolean mNwStart = false;

    public static final long TWO_SECONDS = 1000 * 2;
    public static final long TWO_MINUTES = 1000 * 60 * 2;
    public static final long TEN_MINUTES = 1000 * 60 * 10;
    public static final int MIN_DISTANCE = 100;
    public static final String PARA_KEY_TIME = "time";
    public static final String PARA_KEY_DISTANCE = "distance";
    public static final String PARA_KEY_INTERVAL = "interval";
    public static final String JSON_PARA_LAT = "lat";
    public static final String JSON_PARA_LON = "lon";
    public static final String JSON_PARA_ACR = "acr";
    public static final String JSON_PARA_TME = "tme";
    public static final String JSON_PARA_CTYPE = "Gtype";

    public static FullLocationManager newInstance(Context context) {
        if (null == sFullLocationManager) {
            sFullLocationManager = new FullLocationManager(context);
        }

        return sFullLocationManager;
    }

    private FullLocationManager(Context context) {
        mContext = context;
    }

    public synchronized void startGPX(Bundle para) {
        if (DEBUG) Log.d(TAG, "startGPX");
        if (false == mGpxStart) {
            mGpxLocationManager = new GpxLocationManager(mContext) {
                @Override
                public void onLocationChanged(Location location) {
                    if (isBetterLocation(location, mLastBestLocation)) {
                        mLastBestLocation = location;
                        saveLastLocation(location);
                        if (DEBUG) Log.d(TAG, "gps location changed");
                        Toast.makeText(mContext, "gps location changed", Toast.LENGTH_LONG).show();
                    }
                }

            };
            mGpxLocationManager.prepare(para.getLong(PARA_KEY_TIME, -1), para.getFloat(PARA_KEY_DISTANCE, -1), para.getLong(PARA_KEY_INTERVAL, -1));
            mGpxLocationManager.start();
            mGpxStart = true;
        } else {
            resetGPX(para);
        }
    }

    public synchronized void stopGPX() {
        if (true == mGpxStart) {
            mGpxLocationManager.stop();
            mGpxStart = false;
        }
    }

    private void resetGPX(Bundle para) {
        mGpxLocationManager.reset(para.getLong(PARA_KEY_TIME, -1), para.getFloat(PARA_KEY_DISTANCE, -1), para.getLong(PARA_KEY_INTERVAL, -1));
    }

    public synchronized void startNw() {
        if (DEBUG) Log.d(TAG, "startNw");
        if (false == mNwStart) {
            CellInfoManager cellInfoManager = new CellInfoManager(mContext);
            WifiInfoManager wifiInfoManager = new WifiInfoManager(mContext);
            mNwLocationManager = new NwLocationManager(mContext, cellInfoManager, wifiInfoManager) {

                @Override
                public void onLocationChanged(Location loc) {
                    if (isBetterLocation(loc, mLastBestLocation)) {
                        mLastBestLocation = loc;
                        saveLastLocation(loc);
                        if (DEBUG) Log.d(TAG, "network location changed");
                        Toast.makeText(mContext, "network location changed", Toast.LENGTH_LONG).show();
                    }
                }
            };
            mNwLocationManager.start();
            mNwStart = true;
        } else {
            mNwLocationManager.resume();
        }
    }

    public synchronized void stopNw() {
        if (true == mNwStart) {
            mNwLocationManager.stop();
            mNwStart = false;
        }
    }

    public synchronized void stopAll() {
        stopGPX();
        stopNw();
    }

    public synchronized void requestUpdateLocation() {
        if (null != mLastBestLocation) {
            //long offTime = getUTCTime() - mLastBestLocation.getTime();
        	long offTime = System.currentTimeMillis() - mLastBestLocation.getTime();
            if (offTime > TEN_MINUTES) {
                // force to retrieve the cell-id location again.
                if (null != mNwLocationManager) {
                    System.out.println("requestUpdate");
                    mNwLocationManager.requestUpdate();
                }
            }
        }
    }

    public synchronized Location getBestLocation() {
        if (null == mLastBestLocation) {
            mLastBestLocation = retriveLastLocation();
        }
        if (null != mLastBestLocation) {
            //long offTime = getUTCTime() - mLastBestLocation.getTime();
        	long offTime = System.currentTimeMillis() - mLastBestLocation.getTime();
            if (offTime > TEN_MINUTES) {
                // force to retrieve the cell-id location again.
                mNwLocationManager.requestUpdate();
            }
        }
        return mLastBestLocation;
    }

    private void saveLastLocation(Location loc) {
        if (null != loc) {
            SharedPreferences mPrefs = mContext.getSharedPreferences("lastlocation", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.putString(JSON_PARA_CTYPE, loc.getProvider());
            ed.putString(JSON_PARA_LAT, String.valueOf(loc.getLatitude()));
            ed.putString(JSON_PARA_LON, String.valueOf(loc.getLongitude()));
            ed.putFloat(JSON_PARA_ACR, loc.getAccuracy());
            ed.putLong(JSON_PARA_TME, loc.getTime());
            ed.commit();
        }
    }

    private Location retriveLastLocation() {
        Location loc = null;
        SharedPreferences mPrefs = mContext.getSharedPreferences("lastlocation", Context.MODE_PRIVATE);
        String type = mPrefs.getString(JSON_PARA_CTYPE, null);
        String lat = mPrefs.getString(JSON_PARA_LAT, null);
        String lon = mPrefs.getString(JSON_PARA_LON, null);
        Float acr = mPrefs.getFloat(JSON_PARA_ACR, 0);
        Long tme = mPrefs.getLong(JSON_PARA_TME, 0);
        if (null == type) {
            return loc;
        }
        try {
            if (LocationManager.NETWORK_PROVIDER.equals(type)) {
                loc = new Location(LocationManager.NETWORK_PROVIDER);
                loc.setLatitude(Double.valueOf(lat));
                loc.setLongitude(Double.valueOf(lon));
                loc.setAccuracy(acr);
                loc.setTime(tme);
            } else if (LocationManager.GPS_PROVIDER.equals(type)) {
                loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(Double.valueOf(lat));
                loc.setLongitude(Double.valueOf(lon));
                loc.setAccuracy(acr);
                loc.setTime(tme);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "failed to retrive the location", e);
        }
        return loc;
    }

    public static long getUTCTime() {
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis();
    }

    /**
     * Determines whether one Location reading is better than the current
     * Location fix
     * 
     * @param location The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to
     *            compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        if (null == location) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use
        // the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be
            // worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
