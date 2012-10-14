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
package com.zapple.evshare.provider;

import com.zapple.evshare.data.BaseColumns;
import com.zapple.evshare.data.Order.OrderTable;
import com.zapple.evshare.data.PilesSite.PilesSiteTable;
import com.zapple.evshare.data.Score.ScoreTable;
import com.zapple.evshare.data.Store.StoreTable;
import com.zapple.evshare.data.Vehicle.VehicleTable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ZappleDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ZappleDatabaseHelper";

    private static ZappleDatabaseHelper sInstance = null;
    private static boolean sTriedAutoIncrement = false;
    private static boolean sFakeLowStorageTest = false;     // for testing only

    static final String DATABASE_NAME = "zapple.db";
    static final int DATABASE_VERSION = 1;
    private final Context mContext;
    private LowStorageMonitor mLowStorageMonitor;

    private ZappleDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    /**
     * Return a singleton helper for the combined MMS and SMS
     * database.
     */
    /* package */ static synchronized ZappleDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ZappleDatabaseHelper(context);
        }
        return sInstance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	createVehicleTables(db);
    	createStoreTables(db);
    	createScoreTables(db);
    	createOrderTables(db);
    	createPilesSiteTables(db);
        createCommonTriggers(db);
    }

    private void createVehicleTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + VehicleTable.TABLE_NAME + " (" +
        		BaseColumns._ID + " INTEGER PRIMARY KEY," +
        		VehicleTable.REMOTE_ID + " TEXT," +
        		VehicleTable.BRAND + " TEXT," +
        		VehicleTable.MODEL + " TEXT," +
        		VehicleTable.PRICE + " TEXT," +
        		VehicleTable.DUMP_ENERGY + " TEXT," +
        		VehicleTable.PARKING_GARAGE + " TEXT," +
        		VehicleTable.PHOTO_URI + " TEXT," +
        		VehicleTable.IS_FAVORITE + " INTEGER DEFAULT 0);");    	
    }
    
    private void createStoreTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + StoreTable.TABLE_NAME + " (" +
        		BaseColumns._ID + " INTEGER PRIMARY KEY," +
        		StoreTable.REMOTE_ID + " TEXT," +
        		StoreTable.NAME + " TEXT," +
        		StoreTable.ADDRESS + " TEXT," +
        		StoreTable.LONGITUDE + " TEXT," +
        		StoreTable.LATITUDE + " TEXT," +
        		StoreTable.ALTITUDE + " TEXT," +
        		StoreTable.IS_FAVORITE + " INTEGER DEFAULT 0);");    	
    }    
    
    private void createScoreTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ScoreTable.TABLE_NAME + " (" +
        		BaseColumns._ID + " INTEGER PRIMARY KEY," +
        		ScoreTable.REMOTE_ID + " INTEGER," +
        		ScoreTable.DESCRIPTION + " TEXT," +
        		ScoreTable.SCORE_VALUE + " TEXT," +
        		ScoreTable.DATE + " TEXT," +
        		ScoreTable.EXPIRY_DATE + " TEXT);");    	
    }    
    
    private void createOrderTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + OrderTable.TABLE_NAME + " (" +
        		BaseColumns._ID + " INTEGER PRIMARY KEY," +
        		OrderTable.REMOTE_ID + " TEXT," +
        		OrderTable.NAME + " TEXT," +
        		OrderTable.STATUS + " TEXT," +
        		OrderTable.ORDER_STATUS + " TEXT," +
        		OrderTable.VEHICLE_MODEL + " TEXT," +
        		OrderTable.VEHICLE_BRAND + " TEXT," +
        		OrderTable.TAKE_VEHICLE_STORE_NAME + " TEXT," +
        		OrderTable.TAKE_VEHICLE_DATE + " INTEGER," +
        		OrderTable.RETURN_VEHICLE_STORE_NAME + " TEXT," +
        		OrderTable.RETURN_VEHICLE_DATE + " INTEGER," +
        		OrderTable.VEHICLE_RENTS + " TEXT," +
        		OrderTable.ADDED_SERVICE_FEE + " TEXT," +        		
        		OrderTable.OTHER_FEE + " TEXT);");              
    }

    private void createPilesSiteTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + PilesSiteTable.TABLE_NAME + " (" +
        		BaseColumns._ID + " INTEGER PRIMARY KEY," +
        		PilesSiteTable.REMOTE_ID + " INTEGER," +
        		PilesSiteTable.NAME + " TEXT," +
        		PilesSiteTable.ADDRESS + " TEXT," +
        		PilesSiteTable.LONGITUDE + " TEXT," +
        		PilesSiteTable.LATITUDE + " TEXT," +
        		PilesSiteTable.ALTITUDE + " TEXT);");    	
    }    
    
    private void createCommonTriggers(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion
                + " to " + currentVersion + ".");

        switch (oldVersion) {
            // fall through
        case 1:
            if (currentVersion <= 1) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion1(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            return;
        }

        Log.e(TAG, "Destroying all old data.");
        dropAll(db);
        onCreate(db);
    }

    private void dropAll(SQLiteDatabase db) {
        // Clean the database out in order to start over from scratch.
        // We don't need to drop our triggers here because SQLite automatically
        // drops a trigger when its attached database is dropped.
        db.execSQL("DROP TABLE IF EXISTS canonical_addresses");
    }

    private void upgradeDatabaseToVersion1(SQLiteDatabase db) {

    }    

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();

        if (!sTriedAutoIncrement) {
            sTriedAutoIncrement = true;
            boolean hasAutoIncrementThreads = hasAutoIncrement(db, "threads");
            boolean hasAutoIncrementAddresses = hasAutoIncrement(db, "canonical_addresses");
            Log.d(TAG, "[getWritableDatabase] hasAutoIncrementThreads: " + hasAutoIncrementThreads +
                    " hasAutoIncrementAddresses: " + hasAutoIncrementAddresses);
            boolean autoIncrementThreadsSuccess = true;
            boolean autoIncrementAddressesSuccess = true;
            if (!hasAutoIncrementThreads) {
                db.beginTransaction();
                try {
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(TAG, "Failed to add autoIncrement to threads;: " + ex.getMessage(), ex);
                    autoIncrementThreadsSuccess = false;
                } finally {
                    db.endTransaction();
                }
            }
            if (!hasAutoIncrementAddresses) {
                db.beginTransaction();
                try {
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(TAG, "Failed to add autoIncrement to canonical_addresses: " +
                            ex.getMessage(), ex);
                    autoIncrementAddressesSuccess = false;
                } finally {
                    db.endTransaction();
                }
            }
            if (autoIncrementThreadsSuccess && autoIncrementAddressesSuccess) {
                if (mLowStorageMonitor != null) {
                    // We've already updated the database. This receiver is no longer necessary.
                    Log.d(TAG, "Unregistering mLowStorageMonitor - we've upgraded");
                    mContext.unregisterReceiver(mLowStorageMonitor);
                    mLowStorageMonitor = null;
                }
            } else {
                if (sFakeLowStorageTest) {
                    sFakeLowStorageTest = false;
                }

                // We failed, perhaps because of low storage. Turn on a receiver to watch for
                // storage space.
                if (mLowStorageMonitor == null) {
                    Log.d(TAG, "[getWritableDatabase] turning on storage monitor");
                    mLowStorageMonitor = new LowStorageMonitor();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
                    intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
                    mContext.registerReceiver(mLowStorageMonitor, intentFilter);
                }
            }
        }
        return db;
    }

    // Determine whether a particular table has AUTOINCREMENT in its schema.
    private boolean hasAutoIncrement(SQLiteDatabase db, String tableName) {
        boolean result = false;
        String query = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" +
                        tableName + "'";
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String schema = c.getString(0);
                    result = schema != null ? schema.contains("AUTOINCREMENT") : false;
                    Log.d(TAG, "tableName: " + tableName + " hasAutoIncrement: " +
                            schema + " result: " + result);
                }
            } finally {
                c.close();
            }
        }
        return result;
    }

    private class LowStorageMonitor extends BroadcastReceiver {

        public LowStorageMonitor() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "[LowStorageMonitor] onReceive intent " + action);

            if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                sTriedAutoIncrement = false;    // try to upgrade on the next getWriteableDatabase
            }
        }
    }
}
