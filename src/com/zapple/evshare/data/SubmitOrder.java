/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Util for submitting order.
 */
public class SubmitOrder implements Parcelable {
	public String mUserName;
	public String mVehicleId;
	public String mTakeVehicleStoreId;
	public String mTakeStoreName;
	public String mTakeVehicleDate;
	public String mReturnVehicleStoreId;
	public String mReturnStoreName;
	public String mReturnVehicleDate;
	public String mChildSeat;
	public String mGpsDevice;
	public String mInvoice;
	public String mModel;
	public String mBrand;
	public String mVehicleFee;
	public String mAddedFee;
	public String mOtherFee;
	public String mVehiclePhotoUri;
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mUserName);
		dest.writeString(mVehicleId);
		dest.writeString(mTakeVehicleStoreId);
		dest.writeString(mTakeStoreName);
		dest.writeString(mTakeVehicleDate);
		dest.writeString(mReturnVehicleStoreId);
		dest.writeString(mReturnStoreName);
		dest.writeString(mReturnVehicleDate);
		dest.writeString(mChildSeat);
		dest.writeString(mGpsDevice);
		dest.writeString(mInvoice);
		dest.writeString(mModel);
		dest.writeString(mBrand);
		dest.writeString(mVehicleFee);
		dest.writeString(mAddedFee);
		dest.writeString(mOtherFee);
		dest.writeString(mVehiclePhotoUri);
	}
	
	public static final Parcelable.Creator<SubmitOrder> CREATOR
		= new Parcelable.Creator<SubmitOrder>() {

			@Override
			public SubmitOrder createFromParcel(Parcel source) {
				return new SubmitOrder(source);
			}

			@Override
			public SubmitOrder[] newArray(int size) {
				return new SubmitOrder[size];
			}
		
		};
		
	private SubmitOrder(Parcel source) {
		mUserName = source.readString();
		mVehicleId = source.readString();
		mTakeVehicleStoreId = source.readString();
		mTakeStoreName = source.readString();
		mTakeVehicleDate = source.readString();
		mReturnVehicleStoreId = source.readString();
		mReturnStoreName = source.readString();
		mReturnVehicleDate = source.readString();
		mChildSeat = source.readString();
		mGpsDevice = source.readString();
		mInvoice = source.readString();		
		mModel = source.readString();
		mBrand = source.readString();
		mVehicleFee = source.readString();
		mAddedFee = source.readString();
		mOtherFee = source.readString();
		mVehiclePhotoUri = source.readString();
	}
	
	public SubmitOrder() {
		
	}
}