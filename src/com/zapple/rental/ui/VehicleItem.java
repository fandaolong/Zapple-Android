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

package com.zapple.rental.ui;

import com.zapple.rental.data.Vehicle;

import android.database.Cursor;

/**
 * This object holds information used by the VehicleAdapter to create 
 * VehicleListItems.
 */
public class VehicleItem {
	private long mId;
	private String mRemoteId;
	private String mBrand;
	private String mModel;
	private String mPrice;
	private String mDumpEnergy;
	private String mParkingGarage;
	private String mPhotoUri;
	
	private String mStoreId;
	private String mTakeVehicleDate;

    public VehicleItem(Cursor cursor) {
    	if (cursor != null) {
    		mId = cursor.getLong(VehicleListAdapter.COLUMN_ID);    		
    		mRemoteId = cursor.getString(VehicleListAdapter.COLUMN_REMOTE_ID);
    		mBrand = cursor.getString(VehicleListAdapter.COLUMN_BRAND);
    		mModel = cursor.getString(VehicleListAdapter.COLUMN_MODEL);
    		mPrice = cursor.getString(VehicleListAdapter.COLUMN_PRICE);
    		mDumpEnergy = cursor.getString(VehicleListAdapter.COLUMN_DUMP_ENERY);
    		mParkingGarage = cursor.getString(VehicleListAdapter.COLUMN_PARKING_GARAGE);
    		mPhotoUri = cursor.getString(VehicleListAdapter.COLUMN_PHOTO_URI);   		
    	}
    }
    
    public VehicleItem(Vehicle vehicle) {
    	if (vehicle != null) {
//    		mId = 0;    		
    		mRemoteId = vehicle.mId;
    		mBrand = vehicle.mBrand;
    		mModel = vehicle.mModel;
    		mPrice = vehicle.mPrice;
    		mDumpEnergy = vehicle.mDumpEnergy;
    		mParkingGarage = vehicle.mParkingGarage;
    		mPhotoUri = vehicle.mPhoto;   		
    	}
    }    
    
    public long getId(){
    	return mId;
    }
    
    public String getRemoteId(){
    	return mRemoteId;
    }        
    
    public String getBrand() {
    	return mBrand;
    }
    
    public String getModel() {
    	return mModel;
    }
    
    public String getPrice() {
    	return mPrice;
    }
    
    public String getDumpEnergy() {
    	return mDumpEnergy;
    }
    
    public String getParkingGarage() {
    	return mParkingGarage;
    }
    
    public String getPhotoUri() {
    	return mPhotoUri;
    }         
  
    public void setStoreId(String storeId) {
    	mStoreId = storeId;
    }
    
    public String getStoreId() {
    	return mStoreId;
    }
    
    public void setTakeVehicleDate(String takeVehicleDate) {
    	mTakeVehicleDate = takeVehicleDate;
    }
    
    public String getTakeVehicleDate() {
    	return mTakeVehicleDate;
    }    
    
    @Override
    public String toString() {
    	return "mId" + mId + "," + 
    			"mRemoteId:" + mRemoteId + "," +
    			"mBrand:" + mBrand + "," +
    			"mModel:" + mModel + "," +
    			"mPrice:" + mPrice + "," +
    			"mDumpEnergy:" + mDumpEnergy + "," +
    			"mParkingGarage:" + mParkingGarage + "," +
    			"mPhotoUri:" + mPhotoUri;
    }
}
