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
