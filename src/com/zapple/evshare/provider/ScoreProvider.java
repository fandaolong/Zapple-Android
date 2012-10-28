/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.provider;

import com.zapple.evshare.data.Score.ScoreTable;

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
 * This class provides the ability to query the score databases.
 */
public class ScoreProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String TAG = "ScoreProvider";
    private static final boolean DEBUG = true;

    private static final int URI_SCORE         = 0;
    private static final int URI_SCORE_ID      = 1;

    private static final String AUTHORITY = "com.zapple.evshare.provider.score";
    
    static {
        URI_MATCHER.addURI(AUTHORITY, "score", URI_SCORE);
        // In these patterns, "#" is the ID.
        URI_MATCHER.addURI(AUTHORITY, "score/#", URI_SCORE_ID);
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
            case URI_SCORE: {
                cursor = db.query(ScoreTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case URI_SCORE_ID: {
            	break;
            }
        }
        if (cursor != null) {
            getContext().getContentResolver().notifyChange(
                    ScoreTable.CONTENT_URI, null);
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
            case URI_SCORE: {
            	affectedRows =db.delete(
            			ScoreTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case URI_SCORE_ID: {
            	affectedRows =db.delete(
            			ScoreTable.TABLE_NAME, selection, selectionArgs);            	
            	break;
            }
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(
            		ScoreTable.CONTENT_URI, null);
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
            case URI_SCORE: {
				if (DEBUG) {
					Log.d(TAG, "insert->URI_SCORE:" + URI_SCORE);															
				}
            	rowID = db.insert(ScoreTable.TABLE_NAME, null, values);
            	break;
            }                
        }       

        if (rowID > 0) {
        	returnUri = Uri.parse("content://" + AUTHORITY + "/" + ScoreTable.TABLE_NAME + "/" + rowID);
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
            case URI_SCORE: {
            	affectedRows  = db.update(ScoreTable.TABLE_NAME, 
            			values, selection, selectionArgs);
            	break;
            }
            case URI_SCORE_ID: {
            	break;
            }
        }

        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(
                    ScoreTable.CONTENT_URI, null);
        }
        return affectedRows;
    }
}
