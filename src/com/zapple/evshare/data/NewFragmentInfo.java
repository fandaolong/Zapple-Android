/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * The fragment info of starting.
 */
public final class NewFragmentInfo implements Parcelable {
    public int mTabIndex;
    public Bundle mArgs;
    public String mName;
    
    // Constructor
    public NewFragmentInfo() {
    }
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mTabIndex);
		dest.writeBundle(mArgs);
		dest.writeString(mName);		
	}
	
	public static final Parcelable.Creator<NewFragmentInfo> CREATOR 
		= new Parcelable.Creator<NewFragmentInfo>() {

		@Override
		public NewFragmentInfo createFromParcel(Parcel source) {
			return new NewFragmentInfo(source);
		}

		@Override
		public NewFragmentInfo[] newArray(int size) {
			return new NewFragmentInfo[size];
		}
	
	};	
	
	private NewFragmentInfo(Parcel source) {
		mTabIndex = source.readInt();
		mArgs = source.readBundle();
		mName = source.readString();
	}	
}