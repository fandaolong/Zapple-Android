/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.zapple.evshare.data.Store;

import android.database.Cursor;

/**
 * This object holds information used by the StoreAdapter to create 
 * StoreListItems.
 */
public class StoreItem {
	private long mId;
	private String mRemoteId;
	private String mName;
	private String mAddress;
	private String mLongitude;
	private String mLatitude;
	private String mAltitude;

    public StoreItem(Cursor cursor) {
    	if (cursor != null) {
    		mId = cursor.getLong(StoreListAdapter.COLUMN_ID);    		
    		mRemoteId = cursor.getString(StoreListAdapter.COLUMN_REMOTE_ID);
    		mName = cursor.getString(StoreListAdapter.COLUMN_NAME);
    		mAddress = cursor.getString(StoreListAdapter.COLUMN_ADDRESS);
    		mLongitude = cursor.getString(StoreListAdapter.COLUMN_LONGITUDE);
    		mLatitude = cursor.getString(StoreListAdapter.COLUMN_LATITUDE);
    		mAltitude = cursor.getString(StoreListAdapter.COLUMN_ALTITUDE);   		
    	}
    }    
    
    public StoreItem(Store store) {
    	if (store != null) {
//    		mId = 0;    		
    		mRemoteId = store.mId;
    		mName = store.mName;
    		mAddress = store.mAddress;
    		mLongitude = String.valueOf(store.mLongitude);
    		mLatitude = String.valueOf(store.mLatitude);
    		mAltitude = String.valueOf(store.mAltitude);   		
    	}
    }    
    
    public long getId(){
    	return mId;
    }
    
    public String getRemoteId(){
    	return mRemoteId;
    }        
    
    public String getName() {
    	return mName;
    }
    
    public String getAddress() {
    	return mAddress;
    }
    
    public String getLongitude() {
    	return mLongitude;
    }
    
    public String getLatitude() {
    	return mLatitude;
    }
    
    public String getAltitude() {
    	return mAltitude;
    }    
  
//    @Override
//    public String toString() {
//    	return "mId" + mId + "," + 
//    			"mRemoteId:" + mRemoteId + "," +
//    			"mBrand:" + mBrand + "," +
//    			"mModel:" + mModel + "," +
//    			"mPrice:" + mPrice + "," +
//    			"mDumpEnergy:" + mDumpEnergy + "," +
//    			"mParkingGarage:" + mParkingGarage + "," +
//    			"mPhotoUri:" + mPhotoUri;
//    }
}
