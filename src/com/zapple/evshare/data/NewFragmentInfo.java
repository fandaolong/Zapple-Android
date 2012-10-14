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