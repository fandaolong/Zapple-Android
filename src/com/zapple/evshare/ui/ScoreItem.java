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
