/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.provider;

import com.zapple.evshare.data.Vehicle.VehicleTable;

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
 * This class provides the ability to query the vehicle databases.
 */
public class VehicleProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "VehicleProvider";
    private static final boolean DEBUG = true;

    private static final int URI_VEHICLE         = 0;
    private static final int URI_VEHICLE_ID      = 1;

    private static final String AUTHORITY = "com.zapple.evshare.provider.vehicle";
    
    static {
        URI_MATCHER.addURI(AUTHORITY, "vehicle", URI_VEHICLE);
        // In these patterns, "#" is the ID.
        URI_MATCHER.addURI(AUTHORITY, "vehicle/#", URI_VEHICLE_ID);
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
            case URI_VEHICLE: {
                cursor = db.query(VehicleTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_VEHICLE_ID: {
            	break;
            }
        }
        if (cursor != null) {
            getContext().getContentResolver().notifyChange(
                    VehicleTable.CONTENT_URI, null);
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
            case URI_VEHICLE: {
            	affectedRows =db.delete(
            			VehicleTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_VEHICLE_ID: {
            	affectedRows =db.delete(
            			VehicleTable.TABLE_NAME, selection, selectionArgs);            	
            	break;
            }
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(
            		VehicleTable.CONTENT_URI, null);
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
            case URI_VEHICLE: {
				if (DEBUG) {
					Log.d(TAG, "insert->URI_VEHICLE:" + URI_VEHICLE);															
				}
            	rowID = db.insert(VehicleTable.TABLE_NAME, null, values);
            	break;
            }                
        }       

        if (rowID > 0) {
        	returnUri = Uri.parse("content://" + AUTHORITY + "/" + VehicleTable.TABLE_NAME + "/" + rowID);
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
            case URI_VEHICLE: {
            	affectedRows  = db.update(VehicleTable.TABLE_NAME, 
            			values, selection, selectionArgs);
            	break;
            }
            case URI_VEHICLE_ID: {
            	break;
            }
        }

        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(
                    VehicleTable.CONTENT_URI, null);
        }
        return affectedRows;
    }
}
