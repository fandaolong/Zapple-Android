/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Order contains data related to Order operation.
 */
public final class Order implements Parcelable {
    private static final String TAG = "Order";
    private static final boolean DEBUG = true;
    public static final String AUTHORITY = "com.zapple.evshare.provider.order_table";
    
    // Constructor
    public Order() {
    	
    }
    
    public String mName;
    public String mStatus;
    public String mId;
    
    public String mOrderStatus;
    public String mVehicleModel;
    public String mVehicleBrand;
    public String mTakeVehicleStoreName;
    public long mTakeVehicleDate;
    public String mReturnVehicleStoreName;
    public long mReturnVehicleDate;
    public String mVehicleRents;
    public String mAddedServiceFee;
    public String mOtherFee;

	public interface OrderColumns {
	    /**
	     * The unique ID for a row/vehicle.
	     * <P>Type: INTEGER (long)</P>
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
        public static final String STATUS = "status";     
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ORDER_STATUS = "order_status";
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String VEHICLE_MODEL = "vehicle_model";     
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String VEHICLE_BRAND = "vehicle_brand";
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String TAKE_VEHICLE_STORE_NAME = "take_vehicle_store_name";      
        
        /**
         * The model of the vehicle.
         * <P>Type: INTEGER</P>
         */
        public static final String TAKE_VEHICLE_DATE = "take_vehicle_date";
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String RETURN_VEHICLE_STORE_NAME = "return_vehicle_store_name";      
        
        /**
         * The model of the vehicle.
         * <P>Type: INTEGER</P>
         */
        public static final String RETURN_VEHICLE_DATE = "return_vehicle_date";        
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String VEHICLE_RENTS = "vehicle_rents";  
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ADDED_SERVICE_FEE = "added_service_fee";         
        
        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String OTHER_FEE = "other_fee";
	}

	public static final class OrderTable implements BaseColumns, OrderColumns {
		public static final String TABLE_NAME = "order_table";
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
		dest.writeString(mStatus);
		dest.writeString(mId);
		dest.writeString(mOrderStatus);
		dest.writeString(mVehicleModel);		
		dest.writeString(mVehicleBrand);
		dest.writeString(mTakeVehicleStoreName);
		dest.writeLong(mTakeVehicleDate);
		dest.writeString(mReturnVehicleStoreName);		
		dest.writeLong(mReturnVehicleDate);
		dest.writeString(mVehicleRents);
		dest.writeString(mAddedServiceFee);
		dest.writeString(mOtherFee);
	}    
	
	public static final Parcelable.Creator<Order> CREATOR
	= new Parcelable.Creator<Order>() {

		@Override
		public Order createFromParcel(Parcel source) {
			return new Order(source);
		}

		@Override
		public Order[] newArray(int size) {
			return new Order[size];
		}
	
	};	
	
	private Order(Parcel source) {
		mName = source.readString();
		mStatus = source.readString();
		mId = source.readString();
		mOrderStatus = source.readString();
		mVehicleModel = source.readString();
		mVehicleBrand = source.readString();
		mTakeVehicleStoreName = source.readString();
		mTakeVehicleDate = source.readLong();
		mReturnVehicleStoreName = source.readString();		
		mReturnVehicleDate = source.readLong();
		mVehicleRents = source.readString();
		mAddedServiceFee = source.readString();
		mOtherFee = source.readString();
	}	
}