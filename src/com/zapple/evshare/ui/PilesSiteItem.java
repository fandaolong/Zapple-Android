/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.zapple.evshare.data.Station;

import android.database.Cursor;

/**
 * This object holds information used by the PilesSiteAdapter to create 
 * PilesSiteListItems.
 */
public class PilesSiteItem {
	private long mId;
	private long mRemoteId;
	private String mName;
	private String mAddress;
	private String mLongitude;
	private String mLatitude;
	private String mAltitude;

    public PilesSiteItem(Cursor cursor) {
    	if (cursor != null) {
    		mId = cursor.getLong(PilesSiteListAdapter.COLUMN_ID);    		
    		mRemoteId = cursor.getLong(PilesSiteListAdapter.COLUMN_REMOTE_ID);
    		mName = cursor.getString(PilesSiteListAdapter.COLUMN_NAME);
    		mAddress = cursor.getString(PilesSiteListAdapter.COLUMN_ADDRESS);
    		mLongitude = cursor.getString(PilesSiteListAdapter.COLUMN_LONGITUDE);
    		mLatitude = cursor.getString(PilesSiteListAdapter.COLUMN_LATITUDE);
    		mAltitude = cursor.getString(PilesSiteListAdapter.COLUMN_ALTITUDE);   		
    	}
    }
    
    public PilesSiteItem(Station station) {
    	if (station != null) {
    		mName = station.mName;
    		mAddress = station.mAddress;
    		mLatitude = String.valueOf(station.mLatitude);
    		mLongitude = String.valueOf(station.mLongitude);
    	}
    }
    
    public long getId(){
    	return mId;
    }
    
    public long getRemoteId(){
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
