/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.service.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class AuthenticationService extends Service {
	private final static String TAG = AuthenticationService.class.getSimpleName();
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.v(TAG, this + " Service started.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, this + " Service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, this + " getBinder() intent=" + intent);
        return mAuthenticator.getIBinder();
    }

    public static class Basic extends AuthenticationService {
    }
}
