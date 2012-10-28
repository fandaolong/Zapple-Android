/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */
package com.zapple.evshare.service.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

/**
 * Simple (minimal) sync adapter.
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
	private final static String TAG = SyncAdapter.class.getSimpleName();
    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.v(TAG, "SyncAdapter.autoInitialize." + autoInitialize);
        mContext = context.getApplicationContext();
        mAccountManager = AccountManager.get(mContext);
    }

    /**
     * Doesn't actually sync, but sweep up all existing local-only contacts.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.v(TAG, "onPerformSync() account=" + account);

        // First, claim all local-only contacts, if any.
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, account.name);
        values.put(RawContacts.ACCOUNT_TYPE, account.type);
        final int count = cr.update(RawContacts.CONTENT_URI, values,
                RawContacts.ACCOUNT_NAME + " IS NULL AND " + RawContacts.ACCOUNT_TYPE + " IS NULL",
                null);
        if (count > 0) {
            Log.v(TAG, "Claimed " + count + " local raw contacts");
        }

        // TODO: Clear isDirty flag
        // TODO: Remove isDeleted raw contacts
    }
}
