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

import com.zapple.evshare.ui.AccountSetupActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Simple authenticator.  It has no "login" dialogs/activities.  When you add a new account, it'll
 * just create a new account with a unique name.
 */
public class Authenticator extends AbstractAccountAuthenticator {
	private final static String TAG = Authenticator.class.getSimpleName();
	public final static String ACCOUNT_TYPE = "com.zapple.evshare.service.account.auth.basic";
    private static final String PASSWORD = "xxx"; // any string will do.
    public static final String OPTIONS_USERNAME = "username";
    public static final String OPTIONS_PASSWORD = "password";

    // To remember the last user-ID.
    private static final String PREF_KEY_LAST_USER_ID = "TestAuthenticator.PREF_KEY_LAST_USER_ID";

    private final Context mContext;

    public Authenticator(Context context) {
        super(context);
        mContext = context.getApplicationContext();
    }

    /**
     * @return a new, unique username.
     */
//    private String newUniqueUserName() {
//    	Log.v(TAG, "newUniqueUserName()");
//        final SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(mContext);
//        final int nextId = prefs.getInt(PREF_KEY_LAST_USER_ID, 0) + 1;
//        prefs.edit().putInt(PREF_KEY_LAST_USER_ID, nextId).apply();
//
//        return "User-" + nextId;
//    }

    /**
     * Create a new account with the name generated by {@link #newUniqueUserName()}.
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options) {
        Log.v(TAG, "addAccount() type=" + accountType);
//        final Bundle bundle = new Bundle();
//
//        final Account account = new Account(newUniqueUserName(), accountType);
//
//        // Create an account.
//        AccountManager.get(mContext).addAccountExplicitly(account, PASSWORD, null);
//
//        // And return it.
//        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
//        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
//        return bundle;
        
        // There are two cases here:
        // 1) We are called with a username/password; this comes from the traditional email
        //    app UI; we simply create the account and return the proper bundle
        if (options != null && options.containsKey(OPTIONS_PASSWORD)
                && options.containsKey(OPTIONS_USERNAME)) {
            final Account account = new Account(options.getString(OPTIONS_USERNAME),
            		ACCOUNT_TYPE);
            AccountManager.get(mContext).addAccountExplicitly(
                        account, options.getString(OPTIONS_PASSWORD), null);

            Bundle b = new Bundle();
            b.putString(AccountManager.KEY_ACCOUNT_NAME, options.getString(OPTIONS_USERNAME));
            b.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
            return b;
        // 2) The other case is that we're creating a new account from an Account manager
        //    activity.  In this case, we add an intent that will be used to gather the
        //    account information...
        } else {
            Bundle b = new Bundle();
            Intent intent =
                AccountSetupActivity.actionSetupAccountIntent(mContext);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            b.putParcelable(AccountManager.KEY_INTENT, intent);
            return b;
        }        
    }

    /**
     * Just return the user name as the authtoken.
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "getAuthToken() account=" + account);
        final Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        bundle.putString(AccountManager.KEY_AUTHTOKEN, account.name);

        return bundle;
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response, Account account, Bundle options) {
        Log.v(TAG, "confirmCredentials()");
        return null;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.v(TAG, "editProperties()");
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // null means we don't support multiple authToken types
        Log.v(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response, Account account, String[] features) {
        // This call is used to query whether the Authenticator supports
        // specific features. We don't expect to get called, so we always
        // return false (no) for any queries.
        Log.v(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle loginOptions) {
        Log.v(TAG, "updateCredentials()");
        return null;
    }
}
