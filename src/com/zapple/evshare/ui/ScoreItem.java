/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.zapple.evshare.data.Score;

/**
 * This object holds information used by the ScoreAdapter to create 
 * ScoreListItems.
 */
public class ScoreItem {
	private long mId;
	private long mRemoteId;
	private String mDescription;
	private String mScoreValue;
	private String mDate;
	private String mExpiryDate;

    public ScoreItem(Score score) {
    	if (score != null) {
//    		mId = 0;    		
//    		mRemoteId = "";
    		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd  HH:mm");
    		mDescription = score.mDescription;
    		mScoreValue = String.valueOf(score.mScoreValue);
    		mDate =  sdf.format(new Date(score.mDate));
    		mExpiryDate = sdf.format(new Date(score.mExpiryDate));  		
    	}
    }    
    
    public long getId(){
    	return mId;
    }
    
    public long getRemoteId(){
    	return mRemoteId;
    }        
    
    public String getDescription() {
    	return mDescription;
    }
    
    public String getScoreValue() {
    	return mScoreValue;
    }
    
    public String getDate() {
    	return mDate;
    }
    
    public String getExpiryDate() {
    	return mExpiryDate;
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
