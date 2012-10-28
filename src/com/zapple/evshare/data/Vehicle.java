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

/**
 * The Vehicle contains data related to Vehicle operation.
 */
public final class Vehicle {
    private static final String TAG = "Vehicle";
    private static final boolean DEBUG = true;
    private static final String AUTHORITY = "com.zapple.evshare.provider.vehicle";
    
    // Constructor
    public Vehicle() {
    	
    }

    public String mId;
    public String mBrand;
    public String mModel;
    public String mPrice;
    public String mDumpEnergy;
    public String mParkingGarage;
    public String mPhoto;
    
	public interface VehicleColumns {
	    /**
	     * The unique ID for a row/vehicle.
	     * <P>Type: INTEGER (long)</P>
	     */
	    public static final String REMOTE_ID = "remote_id";
	    
        /**
         * The brand of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String BRAND = "brand";

        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String MODEL = "model";        
        
        /**
         * The rent of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String PRICE = "price";
        
        /**
         * The remainder energy of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String DUMP_ENERGY = "dumpEnergy";
        
        /**
         * The parking garage of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String PARKING_GARAGE = "parkingGarage";        
        
        /**
         * The uri of the vehicle photo.
         * <P>Type: TEXT</P>
         */
        public static final String  PHOTO_URI = "photoUri";     
        
        /**
         * Whether the vehicle is favorite.
         * <P>Type: Boolean</P>
         */
        public static final String IS_FAVORITE = "is_favorite";         
	}

	public static final class VehicleTable implements BaseColumns, VehicleColumns {
		public static final String TABLE_NAME = "vehicle";
		/**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);	
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "remote_id DESC";  
        
        
	}
}
