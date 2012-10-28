/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */
package com.zapple.evshare.service.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class SyncService extends Service {
	private final static String TAG = SyncService.class.getSimpleName();
    private static SyncAdapter sSyncAdapter;

    @Override
    public void onCreate() {
    	Log.v(TAG, "SyncService.sSyncAdapter." + sSyncAdapter);
        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    public static class Basic extends SyncService {
    }
}
