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

package com.zapple.evshare.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Store contains data related to Store operation.
 */
public final class Store implements Parcelable {
    private static final String TAG = "Store";
    private static final boolean DEBUG = true;
    private static final String AUTHORITY = "com.zapple.evshare.provider.store";
    
    // Constructor
    public Store() {
    	
    }
    
    public String mName;
    public String mId;
    public String mAddress;
    public double mLongitude;
    public double mLatitude;
    public double mAltitude;

	public interface StoreColumns {
	    /**
	     * The unique ID for a row/vehicle.
	     * <P>Type: TEXT</P>
	     */
	    public static final String REMOTE_ID = "remote_id";
	    
        /**
         * The brand of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ADDRESS = "address";        
        
        /**
         * The rent of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String LONGITUDE = "longitude";
        
        /**
         * The remainder energy of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String LATITUDE = "latitude";
        
        /**
         * The parking garage of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ALTITUDE = "altitude";          
        
        /**
         * Whether the store is favorite.
         * <P>Type: Boolean</P>
         */
        public static final String IS_FAVORITE = "is_favorite";          
	}

	public static final class StoreTable implements BaseColumns, StoreColumns {
		public static final String TABLE_NAME = "store";
		/**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);	
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "remote_id DESC";  
        
        
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mId);
		dest.writeString(mAddress);
		dest.writeDouble(mLongitude);
		dest.writeDouble(mLatitude);		
		dest.writeDouble(mAltitude);
	}
	
	public static final Parcelable.Creator<Store> CREATOR 
		= new Parcelable.Creator<Store>() {

		@Override
		public Store createFromParcel(Parcel source) {
			return new Store(source);
		}

		@Override
		public Store[] newArray(int size) {
			return new Store[size];
		}
	
	};	
	
	private Store(Parcel source) {
		mName = source.readString();
		mId = source.readString();
		mAddress = source.readString();
		mLongitude = source.readDouble();
		mLatitude = source.readDouble();
		mAltitude = source.readDouble();
	}	
}