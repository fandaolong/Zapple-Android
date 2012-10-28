/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */
package com.zapple.evshare.service;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Binder;
import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

public class GpsService extends Service {
	private final static String TAG = GpsService.class.getSimpleName();
	public static final String GPX_SERVICE = "com.zapple.rental.GPXService.SERVICE";
	public static final String EXTRA_UPDATE_RATE = "update-rate";
	public static final String EXTRA_GPS_ACCURACY = "gps_accuracy";

	private static LocationManager mLocationManager = null;
	private static Location mLastLocation = null;

	private int updateRate = -1;
	private int gpsAccuracy = 0;
	private int gpsRate = 100;
	private boolean inListen = false;
	private static final int UPDATE_NETWORK = 1;
	private static final int UPDATE_GPS = 2;
	private static final int NETWORK_TIMEOUT = 3;
	private static final int TEN_SECONDS = 1000 * 10;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	
	private static final int LOCATION_CHANGE = 1;

	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private MyBinder mBinder;
	private Handler mUiHandler;
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case UPDATE_NETWORK: {
					Log.d(TAG, "update network: inListen=" + inListen);
					if (false == inListen) {
						inListen = true;
						mLocationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, updateRate,
								gpsAccuracy * gpsRate, locationListener);
						Message tmrMsg = obtainMessage(NETWORK_TIMEOUT);
						// give 10s to retrive the network location.
						sendMessageDelayed(tmrMsg, TEN_SECONDS);
					}
					break;
				}
				case UPDATE_GPS: {
					// request again.
					Log.d(TAG, "update gps: inListen=" + inListen);
					if (false == inListen) {
						inListen = true;
						mLocationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, updateRate,
								gpsAccuracy * gpsRate, locationListener);
					}
					break;
				}
				case NETWORK_TIMEOUT:{
					Log.d(TAG, "update netowrk time out");
					inListen = false;
					Message gpsMsg = obtainMessage(UPDATE_GPS);
					sendMessage(gpsMsg);
					break;
				}
			}
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.getProvider(LocationManager.GPS_PROVIDER);

		HandlerThread thread = new HandlerThread("GPXServiceThread",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		mBinder = new MyBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);
		doServiceStart(intent, startId);
		return Service.START_REDELIVER_INTENT;
	}

	private void doServiceStart(Intent intent, int startId) {
		updateRate = intent.getIntExtra(EXTRA_UPDATE_RATE, -1);
		if (updateRate == -1) {
			updateRate = TEN_MINUTES;
		}

		gpsAccuracy = intent.getIntExtra(EXTRA_GPS_ACCURACY, 0);
		Log.d(TAG, "doServiceStart, the updateRate:" + updateRate
				+ " the gps accuracy is: " + gpsAccuracy);
		if (1 == gpsAccuracy) {
			//TODO: enable the accuracy.
		}

		openGps(this.getApplicationContext());

		if (false == inListen) {
			Message msg;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				msg = mServiceHandler.obtainMessage(UPDATE_NETWORK);
				Location location = mLocationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (isBetterLocation(location, mLastLocation)) {
					mLastLocation = location;
				}
			} else {
				Log.d(TAG, "no NETWORK provider available....");
				msg = mServiceHandler.obtainMessage(UPDATE_GPS);
				Location location = mLocationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (isBetterLocation(location, mLastLocation)) {
					mLastLocation = location;
				}
			}
			mServiceHandler.sendMessage(msg);
		}
	}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }	
	
	@Override
	public void onDestroy() {
		if (mLocationManager != null) {
			mLocationManager.removeUpdates(locationListener);
			mLocationManager = null;
		}
		super.onDestroy();
	}

    public class MyBinder extends Binder {
        public GpsService getService() {
            return GpsService.this;
        }
    }	
	
	private static void openGps(Context context) {

		boolean isEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!isEnabled) {
			Log.e(TAG, "the gps is not enale...");
			Intent gpsIntent = new Intent();
			gpsIntent.setClassName("com.android.settings",
					"com.android.settings.widget.SettingsAppWidgetProvider");
			gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
			gpsIntent.setData(Uri.parse("custom:3"));
			try {
				PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}

	public void setUiHandler(Handler handler) {
		mUiHandler = handler;
	}
	
	public static Location getLocation(Context context) {
		openGps(context);
		Log.d(TAG, "someone getLocation...");
		Location location = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (isBetterLocation(location, mLastLocation)) {
			Log.d(TAG, "someone getLocation, find better location.");
			mLastLocation = location;
		}
		return mLastLocation;
	}

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			if (isBetterLocation(location, mLastLocation)) {
				Log.d(TAG, "onLocationChanged, better location received.");
				mLastLocation = location;
			}
			// remove the listener first
			mLocationManager.removeUpdates(this);
			inListen = false;

			// arrange the next query
			Message msg = mServiceHandler.obtainMessage(UPDATE_GPS);
			if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
				Log.d(TAG, "onLocationChanged, GPS delay message");
				mServiceHandler.sendMessageDelayed(msg, updateRate);
			} else {
				Log.d(TAG, "onLocationChanged, network instant message.");
				mServiceHandler.removeMessages(NETWORK_TIMEOUT);
				mServiceHandler.sendMessage(msg);
			}		
			if (mUiHandler != null) {
				Message message = mUiHandler.obtainMessage(LOCATION_CHANGE);
				mUiHandler.sendMessage(message);
			}
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

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	private static boolean isBetterLocation(Location location,
			Location currentBestLocation) {
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
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

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
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
