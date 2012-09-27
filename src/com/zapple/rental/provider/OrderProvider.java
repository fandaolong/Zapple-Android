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

package com.zapple.rental.provider;

import com.zapple.rental.data.Order;
import com.zapple.rental.data.Order.OrderTable;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * This class provides the ability to query the order databases.
 */
public class OrderProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "OrderProvider";
    private static final boolean DEBUG = true;

    private static final int URI_ORDER         = 0;
    private static final int URI_ORDER_ID      = 1;

    private static final String AUTHORITY = Order.AUTHORITY;
    
    static {
        URI_MATCHER.addURI(AUTHORITY, "order_table", URI_ORDER);
        // In these patterns, "#" is the ID.
        URI_MATCHER.addURI(AUTHORITY, "order_table/#", URI_ORDER_ID);
    }

    private SQLiteOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = ZappleDatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        switch(URI_MATCHER.match(uri)) {
            case URI_ORDER: {
                cursor = db.query(OrderTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_ORDER_ID: {
            	break;
            }
        }
        if (cursor != null) {
            getContext().getContentResolver().notifyChange(
                    OrderTable.CONTENT_URI, null);
        }
        return cursor;
    }
    
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Context context = getContext();
        int affectedRows = 0;

        switch(URI_MATCHER.match(uri)) {
            case URI_ORDER: {
            	affectedRows =db.delete(
            			OrderTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_ORDER_ID: {
            	affectedRows =db.delete(
            			OrderTable.TABLE_NAME, selection, selectionArgs);            	
            	break;
            }
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(
            		OrderTable.CONTENT_URI, null);
        }
        return affectedRows;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();    
        Context context = getContext();
        long rowID = 0;
        Uri returnUri;
		if (DEBUG) {
			Log.d(TAG, "insert->uri:" + uri);															
		}
        switch (URI_MATCHER.match(uri)) {
            case URI_ORDER: {
				if (DEBUG) {
					Log.d(TAG, "insert->URI_ORDER:" + URI_ORDER);															
				}
            	rowID = db.insert(OrderTable.TABLE_NAME, null, values);
            	break;
            }                
        }       

        if (rowID > 0) {
        	returnUri = Uri.parse("content://" + AUTHORITY + "/" + OrderTable.TABLE_NAME + "/" + rowID);
            context.getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        } else {
            Log.e(TAG,"insert: failed! " + values.toString());
        }

        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int affectedRows = 0;
        
        switch(URI_MATCHER.match(uri)) {
            case URI_ORDER: {
            	affectedRows  = db.update(OrderTable.TABLE_NAME, 
            			values, selection, selectionArgs);
            	break;
            }
            case URI_ORDER_ID: {
            	break;
            }
        }

        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(
                    OrderTable.CONTENT_URI, null);
        }
        return affectedRows;
    }
}
