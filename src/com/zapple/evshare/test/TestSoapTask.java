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

package com.zapple.evshare.test;

import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.PersonalInfo;
import com.zapple.evshare.data.SubmitOrder;
import com.zapple.evshare.transaction.WebServiceController;

import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * This task providers test soap feature.
 */
public class TestSoapTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "TestSoapTask";
    private static final boolean DEBUG = true;
    private Context mContext;
    
    public TestSoapTask(Context context) {
    	mContext = context;
    }
    
	@Override
	protected String doInBackground(Void... params) {
		String result = null;
		
		// 1. advertise upgrade
		if (DEBUG) Log.d(TAG, "-----------------1. advertiseUpgrade-------------");
		String currentAdvertiseVersion = "1.0";
		try {
			WebServiceController.advertiseUpgrade(currentAdvertiseVersion);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 2. cancel order
		if (DEBUG) Log.d(TAG, "-----------------2. cancelOrder-------------");
		String userName = PreferenceManager.getDefaultSharedPreferences(mContext).getString(LoginResult.LOGIN_RESULT_ACCOUNT_KEY, null);
		String password = PreferenceManager.getDefaultSharedPreferences(mContext).getString(LoginResult.LOGIN_RESULT_PASSWORD_KEY, null);
		String orderId = "3154";
		try {
			WebServiceController.cancelOrder(userName, password, orderId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 3. continue order
		if (DEBUG) Log.d(TAG, "-----------------3. continueOrder-------------");
		try {
			WebServiceController.continueOrder(userName, password, orderId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 4. get stores
		if (DEBUG) Log.d(TAG, "-----------------4. getStores-------------");
		String longitude = "116.357428";
		String latitude = "39.90923";
		String altitude = "0";
		String cityName = "…œ∫£";
		String startIndex = "0";
		String endIndex = "100";
		try {
			WebServiceController.getStores(longitude, latitude, altitude, cityName, startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 5. get vehicles
		if (DEBUG) Log.d(TAG, "-----------------5. getVehicles-------------");
		String storeId = "001245";
		try {
			WebServiceController.getVehicles(storeId, startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 6. login
		if (DEBUG) Log.d(TAG, "-----------------6. login-------------");
		try {
			WebServiceController.login(userName, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 7. modify password
		if (DEBUG) Log.d(TAG, "-----------------7. modifyPassword-------------");
		String oldPassword = password;
		String newPassword = password;
		try {
			WebServiceController.modifyPassword(userName, oldPassword, newPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 8. modify personal info
		if (DEBUG) Log.d(TAG, "-----------------8. modifyPersonalInfo-------------");
		PersonalInfo personalInfo = new PersonalInfo();
		personalInfo.mName = "licong";
		personalInfo.mUserRank = "";
		personalInfo.mIdType = "ID Card";
		personalInfo.mId = "10013013500455200";
		personalInfo.mPhoneNumber = "13911223344";
		personalInfo.mEmailAddress = "forlong401@163.com";
		try {
			WebServiceController.modifyPersonalInfo(userName, newPassword, personalInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 9. password retrieval
		if (DEBUG) Log.d(TAG, "-----------------9. passwordRetrieval-------------");
		String phoneNumber = "13911223344";
		String emailAddress = "forlong401@163.com";
		try {
			WebServiceController.passwordRetrieval(phoneNumber, emailAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 10. query favorite
		if (DEBUG) Log.d(TAG, "-----------------10. queryFavorite-------------");
		String startStoreIndex = "0";
		String endStoreIndex = "100";
		String startVehicleIndex = "0";
		String endVehicleIndex = "100";
		try {
			WebServiceController.queryFavorite(userName, newPassword, startStoreIndex, endStoreIndex, startVehicleIndex, endVehicleIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 11. query order
		if (DEBUG) Log.d(TAG, "-----------------11. queryOrder-------------");
		try {
			WebServiceController.queryOrder(userName, newPassword, orderId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 12. query orders
		if (DEBUG) Log.d(TAG, "-----------------12. queryOrders-------------");
		try {
			WebServiceController.queryOrders(userName, newPassword, startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		// 13. query scores
		if (DEBUG) Log.d(TAG, "-----------------13. queryScores-------------");
		try {
			WebServiceController.queryScores(userName, newPassword, startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 14. submit complaint and suggestion
		if (DEBUG) Log.d(TAG, "-----------------14. submitComplaintAndSuggestion-------------");
		String content = "no complaint and suggestion.";
		try {
			WebServiceController.submitComplaintAndSuggestion(userName, content, phoneNumber, emailAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 15. submit order
		if (DEBUG) Log.d(TAG, "-----------------15. submitOrder-------------");
//		String vehicleId = "v123444"; 
//		String takeVehicleStoreId = "tvs3333"; 
//		String takeVehicleDate = "12465654555";
//		String returnVehicleStoreId = "rvs3444";
//		String returnVehicleDate = "12465654559";
//		String childSeat = "true";
//		String gpsDevice = "false";
//		String invoice = "true";	
		SubmitOrder submitOrder = new SubmitOrder();
		submitOrder.mUserName = userName;
		submitOrder.mVehicleId = "v123444";
		submitOrder.mTakeVehicleStoreId = "tvs3333";
		submitOrder.mTakeVehicleDate = "12465654555";
		submitOrder.mReturnVehicleStoreId = "rvs3444";
		submitOrder.mReturnVehicleDate = "12465654559";
		submitOrder.mChildSeat = "true";
		submitOrder.mGpsDevice = "true";
		submitOrder.mInvoice = "true";
		try {
			WebServiceController.submitOrder(submitOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 16. getCities
		if (DEBUG) Log.d(TAG, "-----------------16. getCities-------------");
		try {
			WebServiceController.getCities();
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		// 17. addFavorite
		if (DEBUG) Log.d(TAG, "-----------------17. addFavorite-------------");
		// vehicle,store
		String favoriteType = "store";
		String favoriteCode = "zp009";
		try {
			WebServiceController.addFavorite(userName, favoriteType, favoriteCode);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		// 18. getElecStations
		if (DEBUG) Log.d(TAG, "-----------------18. getElecStations-------------");
		try {
			WebServiceController.getElecStations(cityName, startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		return result;
	}
	 
	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(mContext, "Soap Interface Test Done.", 
				Toast.LENGTH_SHORT).show();
	}
}