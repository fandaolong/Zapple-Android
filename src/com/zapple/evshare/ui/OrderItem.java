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

import com.zapple.evshare.data.Order;

import android.database.Cursor;

/**
 * This object holds information used by the OrderAdapter to create 
 * OrderListItems.
 */
public class OrderItem {
	private long mId;
	private String mRemoteId;
	private String mName;
	private String mStatus;
	private Order mOrder;

    public OrderItem(Cursor cursor) {
    	if (cursor != null) {
    		mId = cursor.getLong(OrderListAdapter.COLUMN_ID);    		
    		mRemoteId = cursor.getString(OrderListAdapter.COLUMN_REMOTE_ID);
    		mName = cursor.getString(OrderListAdapter.COLUMN_NAME);
    		mStatus = cursor.getString(OrderListAdapter.COLUMN_STATUS);  		
    	}
    }
    
    public OrderItem(Order order) {
    	if (order != null) {
//    		mId = order.mId;    		
    		mRemoteId = order.mId;
    		mName = order.mName;
    		mStatus = order.mStatus;  		
    	}
    	mOrder = order;
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
    
    public String getStatus() {
    	return mStatus;
    }
    
    public Order getOrder(){
    	return mOrder;
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
