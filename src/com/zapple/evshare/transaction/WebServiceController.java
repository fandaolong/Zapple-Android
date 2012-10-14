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

package com.zapple.evshare.transaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.zapple.evshare.data.AdvertiseUpgradeResult;
import com.zapple.evshare.data.GetCitiesResult;
import com.zapple.evshare.data.GetElecStationsResult;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.data.Order;
import com.zapple.evshare.data.OrderDetail;
import com.zapple.evshare.data.PersonalInfo;
import com.zapple.evshare.data.QueryFavoriteResult;
import com.zapple.evshare.data.Score;
import com.zapple.evshare.data.Station;
import com.zapple.evshare.data.Store;
import com.zapple.evshare.data.SubmitOrder;
import com.zapple.evshare.data.Vehicle;
import com.zapple.evshare.util.Constants;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class WebServiceController {
	public final static String TAG = "WebServiceController";
	private static final boolean DEBUG = true;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	public final static boolean DEBUG_HTTP_INPUT_OUTPUT = false;
	private static final boolean LOG_RESPONSE_STREAM_TO_FILE = true;
	private static String sCookie;
//	private static final String sPath = "http://rentcar.bbcwx.com/WebServices/kiosk.asmx";
	private static final String sPath = Constants.SERVER_HOST + Constants.SOAP_ADDRESS;
	
	private static InputStream logResponseStream(InputStream responseStream, String saveAsFileName) {
		InputStream is = responseStream;
		if (LOG_RESPONSE_STREAM_TO_FILE) {
			String fileName = Environment.getExternalStorageDirectory()+ "/" + saveAsFileName;
	    	File saveAsFile = new File(fileName);
	    	try {
				saveAsFile.createNewFile();
		    	FileOutputStream out = new FileOutputStream(saveAsFile);
		    	ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		    	byte[] buffer = new byte[1024];
		    	int len = 0;
		    	while( (len = responseStream.read(buffer)) !=-1 ){
		    		out.write(buffer, 0, len);
		    		outSteam.write(buffer, 0, len);
		    	}            	
		    	out.flush();
		    	out.close();	
		        outSteam.close();  
		        responseStream.close();   
	            String response = new String(outSteam.toByteArray()); 
	            Log.d(TAG, saveAsFileName + "->response->" + response);     
		    	is = new FileInputStream(saveAsFile);					
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "logResponseStream->IOException->" + e);
			}
    	
		}		
		return is;
	}	
	
	private static HttpURLConnection httpPostMethod(byte[] data) throws Exception {
        URL url = new URL(sPath);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        //set cookie. sCookie is my static cookie string  
        if (sCookie != null && sCookie.length() > 0 ) {  
            conn.setRequestProperty("Cookie", sCookie);     
            Log.d(TAG, "login->sCookie->" + sCookie);
        }        
        conn.setConnectTimeout(5 * 1000);  
        conn.setRequestMethod("POST");  
        conn.setDoOutput(true);  
        conn.setUseCaches(true);
        conn.setRequestProperty("Content-Type",  
                "application/soap+xml; charset=utf-8");  
        conn.setRequestProperty("Content-Length", String.valueOf(data.length));  
        OutputStream outStream = conn.getOutputStream();  
        outStream.write(data);  
        outStream.flush();  
        outStream.close();  
        
        return conn;
	}
		
	public static AdvertiseUpgradeResult advertiseUpgrade(String currentAdvertiseVersion) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/advertise_upgrade.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$currentAdvertiseVersion", currentAdvertiseVersion);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "advertiseUpgrade->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "advertiseUpgrade->responseCode->" + responseCode);
    	AdvertiseUpgradeResult result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "advertiseUpgrade->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "advertise_upgrade_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseLoginXML->response->" + response);                
            }
		    result = parseAdvertiseUpgradeXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}
	
	public static String cancelOrder(String userName, String password, String orderId) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/cancel_order.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$orderId", orderId);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "cancelOrder->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "cancelOrder->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "cancelOrder->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "cancel_order_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseCancelOrderXML->response->" + response);                
            }
		    result = parseCancelOrderXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}	
	
	public static String continueOrder(String userName, String password, String orderId) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/continue_order.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$orderId", orderId);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "continueOrder->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "continueOrder->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "continueOrder->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "continue_order_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseContinueOrderXML->response->" + response);                
            }
		    result = parseContinueOrderXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}	
	
	public static ArrayList<Store> getStores(
			String longitude, 
			String latitude, 
			String altitude, 
			String cityName, 
			String startIndex, 
			String endIndex) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/get_stores.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap;
        if (longitude == null) {
        	longitude = "";
        }
        
        if (latitude == null) {
        	latitude = "";
        }
        
        if (altitude == null) {
        	altitude = "";
        }
        
        if (cityName == null) {
        	cityName = "";
        }
        
        if (startIndex == null) {
        	startIndex = "0";
        }
        
        if (endIndex == null) {
        	endIndex = "100";
        }        
        
        soap = xml.replaceAll("\\$longitude", longitude);
        soap = soap.replaceAll("\\$latitude", latitude);
        soap = soap.replaceAll("\\$altitude", altitude);
        soap = soap.replaceAll("\\$cityName", cityName);
        soap = soap.replaceAll("\\$startIndex", startIndex);
        soap = soap.replaceAll("\\$endIndex", endIndex);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "getStores->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "getStores->responseCode->" + responseCode);
    	ArrayList<Store> result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "getStores->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "get_stores_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseContinueOrderXML->response->" + response);                
            }
		    result = parseGetStoresXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}	
	
	public static List<Vehicle> getVehicles(
			String storeId, 
			String startIndex, 
			String endIndex) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/get_vehicles.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$storeId", storeId);
        soap = soap.replaceAll("\\$startIndex", startIndex);
        soap = soap.replaceAll("\\$endIndex", endIndex);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "getVehicles->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "getVehicles->responseCode->" + responseCode);
    	List<Vehicle> result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "getVehicles->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "get_vehicles_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseGetVehiclesXML->response->" + response);                
            }
		    result = parseGetVehiclesXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}	
	
	public static LoginResult login(String userName, String password) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/login.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "login->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "login->responseCode->" + responseCode);
    	LoginResult result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "login->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "login_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseLoginXML->response->" + response);                
            }
		    result = parseLoginXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  
    }  	
		
	public static String modifyPassword(
			String userName, 
			String oldPassword, 
			String newPassword) throws Exception {
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/modify_password.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$oldPassword", oldPassword);
        soap = soap.replaceAll("\\$newPassword", newPassword);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "modifyPassword->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "modifyPassword->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "modifyPassword->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "modify_password_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseModifyPasswordXML->response->" + response);                
            }
		    result = parseModifyPasswordXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  		
	}		
	
	public static String modifyPersonalInfo(String userName, String password, PersonalInfo personalInfo) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/modify_personal_info.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$name", personalInfo.mName);
        soap = soap.replaceAll("\\$userRank", personalInfo.mUserRank);
        soap = soap.replaceAll("\\$idType", personalInfo.mIdType);
        soap = soap.replaceAll("\\$id", personalInfo.mId);
        soap = soap.replaceAll("\\$phoneNumber", personalInfo.mPhoneNumber);
        soap = soap.replaceAll("\\$emailAddress", personalInfo.mEmailAddress);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "modifyPersonalInfo->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "modifyPersonalInfo->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "modifyPersonalInfo->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "modify_personal_info_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseModifyPersonalInfoXML->response->" + response);                
            }
		    result = parseModifyPersonalInfoXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  
    }	
	
	public static String passwordRetrieval(String phoneNumber, String emailAddress) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/password_retrieval.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$phoneNumber", phoneNumber);
        soap = soap.replaceAll("\\$emailAddress", emailAddress);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "passwordRetrieval->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "passwordRetrieval->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "passwordRetrieval->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "password_retrieval_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parsePasswordRetrievalXML->response->" + response);
            }		    
            result = parsePasswordRetrievalXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }		
	
	public static QueryFavoriteResult queryFavorite(
			String userName, 
			String password, 
			String startStoreIndex,
			String endStoreIndex,
			String startVehicleIndex,
			String endVehicleIndex) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/query_favorite.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$startStoreIndex", startStoreIndex);
        soap = soap.replaceAll("\\$endStoreIndex", endStoreIndex);
        soap = soap.replaceAll("\\$startVehicleIndex", startVehicleIndex);
        soap = soap.replaceAll("\\$endVehicleIndex", endVehicleIndex);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "queryFavorite->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data);
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "queryFavorite->responseCode->" + responseCode);
    	QueryFavoriteResult result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "queryFavorite->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    InputStream is = logResponseStream(responseStream, "query_favorite_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseQueryFavoriteInfoXML->response->" + response);                
            }
		    result = parseQueryFavoriteXML(is); 		    
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        	
        } 
        return result;  
    }	
	
	public static OrderDetail queryOrder(String userName, String password, String orderId) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/query_order.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$orderId", orderId);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "queryOrder->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "queryOrder->responseCode->" + responseCode);
    	OrderDetail result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "queryOrder->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "query_order_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseQueryOrderXML->response->" + response);
            }		    
            result = parseQueryOrderXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static List<Order> queryOrders(String userName, String password, String startIndex, String endIndex) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/query_orders.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$startIndex", startIndex);
        soap = soap.replaceAll("\\$endIndex", endIndex);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "queryOrders->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "queryOrders->responseCode->" + responseCode);
    	List<Order> result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "queryOrders->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "query_orders_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseQueryOrdersXML->response->" + response);
            }		    
            result = parseQueryOrdersXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static List<Score> queryScores(
			String userName, 
			String password, 
			String startIndex, 
			String endIndex) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/query_scores.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$password", password);
        soap = soap.replaceAll("\\$startIndex", startIndex);
        soap = soap.replaceAll("\\$endIndex", endIndex);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "queryScores->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "queryScores->responseCode->" + responseCode);
    	List<Score> result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "queryScores->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "query_scores_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseQueryScoresXML->response->" + response);
            }		    
            result = parseQueryScoresXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static String submitComplaintAndSuggestion(
			String userName, 
			String content, 
			String phoneNumber, 
			String emailAddress) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/submit_complaint_and_suggestion.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName);
        soap = soap.replaceAll("\\$content", content);
        soap = soap.replaceAll("\\$phoneNumber", phoneNumber);
        soap = soap.replaceAll("\\$emailAddress", emailAddress);
  
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "submitComplaintAndSuggestion->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "submitComplaintAndSuggestion->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "submitComplaintAndSuggestion->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "submit_complaint_and_suggestion_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseSubmitComplaintAndSuggestionXML->response->" + response);
            }		    
            result = parseSubmitComplaintAndSuggestionXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }
	
	public static String submitOrder(SubmitOrder submitOrder) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/submit_order.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", submitOrder.mUserName == null ? "" : submitOrder.mUserName);
        soap = soap.replaceAll("\\$vehicleId", submitOrder.mVehicleId == null ? "" : submitOrder.mVehicleId);
        soap = soap.replaceAll("\\$takeVehicleStoreId", submitOrder.mTakeVehicleStoreId == null ? "" : submitOrder.mTakeVehicleStoreId);
        soap = soap.replaceAll("\\$takeVehicleDate", submitOrder.mTakeVehicleDate == null ? "" : submitOrder.mTakeVehicleDate);
        soap = soap.replaceAll("\\$returnVehicleStoreId", submitOrder.mReturnVehicleStoreId == null ? "" : submitOrder.mReturnVehicleStoreId);
        soap = soap.replaceAll("\\$returnVehicleDate", submitOrder.mReturnVehicleDate == null ? "" : submitOrder.mReturnVehicleDate);
        soap = soap.replaceAll("\\$childSeat", submitOrder.mChildSeat == null ? "" : submitOrder.mChildSeat);
        soap = soap.replaceAll("\\$gpsDevice", submitOrder.mGpsDevice == null ? "" : submitOrder.mGpsDevice);
        soap = soap.replaceAll("\\$invoice", submitOrder.mInvoice == null ? "" : submitOrder.mInvoice);
        
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "submitOrder->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "submitOrder->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "submitOrder->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "submit_order_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseSubmitOrderXML->response->" + response);
            }		    
            result = parseSubmitOrderXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static GetCitiesResult getCities() throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/get_cities.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  

        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "getCities->post data->" + xml);
        data = xml.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "getCities->responseCode->" + responseCode);
    	GetCitiesResult result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "getCities->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "get_cities_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseGetCitiesXML->response->" + response);
            }		    
            result = parseGetCitiesXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static String addFavorite(String userName, String favoriteType, String favoriteCode) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/add_favorite.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$userName", userName == null ? "" : userName);
        soap = soap.replaceAll("\\$favoriteType", favoriteType == null ? "" : favoriteType);
        soap = soap.replaceAll("\\$favoriteCode", favoriteCode == null ? "" : favoriteCode);
        
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "addFavorite->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "addFavorite->responseCode->" + responseCode);
    	String result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "addFavorite->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "add_favorite_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseAddFavoriteXML->response->" + response);
            }		    
            result = parseAddFavoriteXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }	
	
	public static GetElecStationsResult getElecStations(String cityName, String startIndex, String endIndex) throws Exception {  		  
        InputStream inStream = WebServiceController.class.getResourceAsStream("/assets/get_elec_stations.xml");  
        byte[] data = readInputStream(inStream);  
        String xml = new String(data);  
        String soap = xml.replaceAll("\\$cityName", cityName == null ? "" : cityName);
        soap = soap.replaceAll("\\$startIndex", startIndex == null ? "" : startIndex);
        soap = soap.replaceAll("\\$endIndex", endIndex == null ? "" : endIndex);
        
        /**  
         * 正则表达式$为特殊正则中的特殊符号须转义，即\$mobile  
         * 而\为字符串中的特殊符号，所以用两个反斜杠，即"\\{1}quot;  
         */  
        Log.d(TAG, "getElecStations->post data->" + soap);
        data = soap.getBytes();// get entity data from xml  
        HttpURLConnection conn = httpPostMethod(data); 
    	int responseCode = conn.getResponseCode();
    	Log.d(TAG, "getElecStations->responseCode->" + responseCode);
    	GetElecStationsResult result = null;
        if (responseCode == HttpURLConnection.HTTP_OK) {  
            InputStream responseStream = conn.getInputStream();  
		    //Get the cookie  
		    String cookie = conn.getHeaderField("set-cookie");  
		    Log.d(TAG, "getElecStations->cookie->" + cookie);
		    if (cookie != null && cookie.length() > 0) {  
		        sCookie = cookie;                
		    }
		    /*   many cookies handling:                   
		    String responseHeaderName = null; 
		    for (int i=1; (responseHeaderName = conn.getHeaderFieldKey(i))!=null; i++) { 
		        if (responseHeaderName.equals("Set-Cookie")) {                   
		        String cookie = conn.getHeaderField(i);    
		        } 
		    }*/
		    
		    InputStream is = logResponseStream(responseStream, "get_elec_stations_response.xml");
            if (DEBUG_HTTP_INPUT_OUTPUT) {
                byte[] responseData = readInputStream(responseStream);
                String response = new String(responseData); 
                Log.d(TAG, "parseGetElecStationsXML->response->" + response);
            }		    
            result = parseGetElecStationXML(is);  
        } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {

        } 
        return result;  
    }		
	
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static AdvertiseUpgradeResult parseAdvertiseUpgradeXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        AdvertiseUpgradeResult advertiseUpgradeResult = null;
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();         	
                if ("advertiseUpgradeResponse".equals(name)) {  
                	advertiseUpgradeResult = new AdvertiseUpgradeResult();
                } else if ("advertiseVersion".equals(name)) {  
                	advertiseUpgradeResult.mAdvertiseVersion = parser.nextText(); 
                } else if ("advertiseData".equals(name)) {
                	advertiseUpgradeResult.mAdvertiseData = parser.nextText();                 	
                }
                break;  
            }
            event = parser.next();  
        }  
        return advertiseUpgradeResult;           
    }	
	
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseCancelOrderXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();            	
                if ("cancelOrderResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseContinueOrderXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();            	
                if ("continueOrderResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static ArrayList<Store> parseGetStoresXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        ArrayList<Store> stores = null;
        Store store = null;

        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	stores = new ArrayList<Store>(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("store".equals(name)) {  
	                	store = new Store();
	                } else if ("name".equals(name)) {  
	                	store.mName = parser.nextText();
	                } else if ("id".equals(name)) {
	                	store.mId = parser.nextText();
	                } else if ("address".equals(name)) {  
	                	store.mAddress = parser.nextText();
	                } else if ("longitude".equals(name)) {
	                	store.mLongitude = Double.parseDouble(parser.nextText());
	                } else if ("latitude".equals(name)) {
	                	store.mLatitude = Double.parseDouble(parser.nextText());
	                } else if ("altitude".equals(name)) {
	                	store.mAltitude = Double.parseDouble(parser.nextText());
	                } else if ("result".equals(name)) {
	                	
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("store".equals(parser.getName())) {
		        		stores.add(store);
		        		store = null;
		        	}
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {
		    		if (stores != null && stores.size() > 0) {
		    			if (DEBUG) {
		    				Log.d(TAG, "parseGetStoresXML->stores:" + stores.toArray().toString());
		    			}	        			
		    		}	        		
		    		break;
		    	}            	             
            }
            event = parser.next();
        }  
        return stores;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static List<Vehicle> parseGetVehiclesXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        List<Vehicle> vehicles = null;
        Vehicle vehicle = null;

        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	vehicles = new ArrayList<Vehicle>(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("vehicle".equals(name)) {  
	                	vehicle = new Vehicle();
	                } else if ("id".equals(name)) {  
	                	vehicle.mId = parser.nextText();
	                } else if ("brand".equals(name)) {
	                	vehicle.mBrand = parser.nextText();
	                } else if ("model".equals(name)) {  
	                	vehicle.mModel = parser.nextText();
	                } else if ("price".equals(name)) {
	                	vehicle.mPrice = parser.nextText();
	                } else if ("dumpEnergy".equals(name)) {
	                	vehicle.mDumpEnergy = parser.nextText();
	                } else if ("parkingGarage".equals(name)) {
	                	vehicle.mParkingGarage = parser.nextText();
	                } else if ("photo".equals(name)) {
	                	vehicle.mPhoto = parser.nextText();
	                } else if ("result".equals(name)) {
	                	
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("vehicle".equals(parser.getName())) {
		        		vehicles.add(vehicle);
		        		vehicle = null;
		        	}
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {
		    		if (vehicles != null && vehicles.size() > 0) {
		    			if (DEBUG) {
		    				Log.d(TAG, "parseGetVehiclesXML->vehicles:" + vehicles.toArray().toString());
		    			}	        			
		    		}	        		
		    		break;
		    	}            	              
            }
            event = parser.next();
        }  
        return vehicles;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static LoginResult parseLoginXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        LoginResult loginResult = null;
        PersonalInfo personalInfo = null;
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();            	
                if ("loginResponse".equals(name)) {  
                	loginResult = new LoginResult();
                } else if ("result".equals(name)) {  
                	loginResult.mLoginResult = parser.nextText();
                } else if ("totalScores".equals(name)) {
                	loginResult.mTotalScores = Long.parseLong(parser.nextText());
                } else if ("expiryDate".equals(name)) {  
                	loginResult.mExpiryDate = Long.parseLong(parser.nextText());
                } else if ("personalInfo".equals(name)) {
                	personalInfo = new PersonalInfo();
                	loginResult.mPersonalInfo = personalInfo;
                } else if ("name".equals(name)) {
                	personalInfo.mName = parser.nextText();
                } else if ("userRank".equals(name)) {
                	personalInfo.mUserRank = parser.nextText();
                } else if ("idType".equals(name)) {
                	personalInfo.mIdType = parser.nextText();
                } else if ("id".equals(name)) {
                	personalInfo.mId = parser.nextText();
                } else if ("phoneNumber".equals(name)) {
                	personalInfo.mPhoneNumber = parser.nextText();
                } else if ("emailAddress".equals(name)) {
                	personalInfo.mEmailAddress = parser.nextText(); 
                } else if ("accountData".equals(name)) {
                	loginResult.mAccountData = parser.nextText();                 	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return loginResult;           
    }
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseModifyPasswordXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();           	
                if ("modifyPasswordResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parsePasswordRetrievalXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();           	
                if ("retrievalResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static QueryFavoriteResult parseQueryFavoriteXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        QueryFavoriteResult result = null;
        Vehicle vehicle = null;
        Store store = null;
        
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	result = new QueryFavoriteResult(); 
	            	result.mStoreList = new ArrayList<Store>();
	            	result.mVehicleList = new ArrayList<Vehicle>();
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("vehicle".equals(name)) {  
	                	vehicle = new Vehicle();
	                } else if (vehicle != null && "id".equals(name)) {  
	                	vehicle.mId = parser.nextText();
	                } else if ("brand".equals(name)) {
	                	vehicle.mBrand = parser.nextText();
	                } else if ("model".equals(name)) {  
	                	vehicle.mModel = parser.nextText();
	                } else if ("price".equals(name)) {
	                	vehicle.mPrice = parser.nextText();
	                } else if ("dumpEnergy".equals(name)) {
	                	vehicle.mDumpEnergy = parser.nextText();
	                } else if ("parkingGarage".equals(name)) {
	                	vehicle.mParkingGarage = parser.nextText();
	                } else if ("photo".equals(name)) {
	                	vehicle.mPhoto = parser.nextText();
	                } else if ("store".equals(name)) {  
	                	store = new Store();
	                } else if ("name".equals(name)) {  
	                	store.mName = parser.nextText();
	                } else if (store != null && "id".equals(name)) {
	                	store.mId = parser.nextText();
	                } else if ("address".equals(name)) {  
	                	store.mAddress = parser.nextText();
	                } else if ("longitude".equals(name)) {
	                	store.mLongitude = Double.parseDouble(parser.nextText());
	                } else if ("latitude".equals(name)) {
	                	store.mLatitude = Double.parseDouble(parser.nextText());
	                } else if ("altitude".equals(name)) {
	                	store.mAltitude = Double.parseDouble(parser.nextText());
	                } else if ("result".equals(name)) {
	                	
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("vehicle".equals(parser.getName())) {
		        		result.mVehicleList.add(vehicle);
		        		vehicle = null;
		        	} else if ("store".equals(parser.getName())) {
		        		result.mStoreList.add(store);
		        		store = null;		        		
		        	}
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {        		
		    		break;
		    	}            	              
            }
            event = parser.next();
        }  
        return result;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseModifyPersonalInfoXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();
            	if (DEBUG) {
            		Log.d(TAG, "modifyPersonalInfoResult->name:" + name);
            	}            	
                if ("modifyPersonalInfoResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    

    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static OrderDetail parseQueryOrderXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();          
        OrderDetail orderDetail = null;

        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	orderDetail = new OrderDetail(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("orderStatus".equals(name)) {  
	                	orderDetail.mOrderStatus = parser.nextText();
	                } else if ("vehicleModel".equals(name)) {  
	                	orderDetail.mVehicleModel = parser.nextText();
	                } else if ("vehicleBrand".equals(name)) {
	                	orderDetail.mVehicleBrand = parser.nextText();
	                } else if ("takeVehicleStoreName".equals(name)) {  
	                	orderDetail.mTakeVehicleStoreName = parser.nextText();
	                } else if ("takeVehicleDate".equals(name)) {
	                	orderDetail.mTakeVehicleDate = parser.nextText();
	                } else if ("returnVehicleStoreName".equals(name)) {
	                	orderDetail.mReturnVehicleStoreName = parser.nextText();
	                } else if ("returnVehicleDate".equals(name)) {
	                	orderDetail.mReturnVehicleDate = parser.nextText();
	                } else if ("vehicleRents".equals(name)) {
	                	orderDetail.mVehicleRents = parser.nextText();
	                } else if ("addedServiceFee".equals(name)) {
	                	orderDetail.mAddedServiceFee = parser.nextText();
	                } else if ("otherFee".equals(name)) {
	                	orderDetail.mOtherFee = parser.nextText();	                
	                }
	                break;  
	            }              	              
            }
            event = parser.next();
        }  
        return orderDetail;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static List<Order> parseQueryOrdersXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        List<Order> orders = null;
        Order order = null;

        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	orders = new ArrayList<Order>(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("order".equals(name)) {  
	                	order = new Order();
	                } else if ("id".equals(name)) {  
	                	order.mId = parser.nextText();
	                } else if ("name".equals(name)) {
	                	order.mName = parser.nextText();
	                } else if ("status".equals(name)) {  
	                	order.mStatus = parser.nextText();	            
	                } else if ("orderStatus".equals(name)) {  
	                	order.mOrderStatus = parser.nextText();	   
	                } else if ("vehicleModel".equals(name)) {  
	                	order.mVehicleModel = parser.nextText();	   
	                } else if ("vehicleBrand".equals(name)) {  
	                	order.mVehicleBrand = parser.nextText();	   
	                } else if ("takeVehicleStoreName".equals(name)) {  
	                	order.mTakeVehicleStoreName = parser.nextText();	   
	                } else if ("takeVehicleDate".equals(name)) {  
	                	order.mTakeVehicleDate = Long.parseLong(parser.nextText());	   
	                } else if ("returnVehicleStoreName".equals(name)) {  
	                	order.mReturnVehicleStoreName = parser.nextText();	   
	                } else if ("returnVehicleDate".equals(name)) {  
	                	order.mReturnVehicleDate = Long.parseLong(parser.nextText());	   
	                } else if ("vehicleRents".equals(name)) {  
	                	order.mVehicleRents = parser.nextText();	   
	                } else if ("addedServiceFee".equals(name)) {  
	                	order.mAddedServiceFee = parser.nextText();	   
	                } else if ("otherFee".equals(name)) {  
	                	order.mOtherFee = parser.nextText();	   	                	
	                } else if ("result".equals(name)) {
	                	
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("order".equals(parser.getName())) {
		        		orders.add(order);
		        		order = null;
		        	}
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {
		    		if (orders != null && orders.size() > 0) {
		    			if (DEBUG) {
		    				Log.d(TAG, "parseGetVehiclesXML->orders:" + orders.toArray().toString());
		    			}	        			
		    		}	        		
		    		break;
		    	}            	              
            }
            event = parser.next();
        }  
        return orders;           
    }       
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static List<Score> parseQueryScoresXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        List<Score> scores = null;
        Score score = null;

        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	scores = new ArrayList<Score>(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("score".equals(name)) {  
	                	score = new Score();
	                } else if ("description".equals(name)) {  
	                	score.mDescription = parser.nextText();
	                } else if ("scoreValue".equals(name)) {
	                	score.mScoreValue = Long.parseLong(parser.nextText());
	                } else if ("date".equals(name)) {  
	                	score.mDate = Long.parseLong(parser.nextText());	                
	                } else if ("expiryDate".equals(name)) {
	                	score.mExpiryDate = Long.parseLong(parser.nextText());
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("score".equals(parser.getName())) {
		        		scores.add(score);
		        		score = null;
		        	}
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {
		    		if (scores != null && scores.size() > 0) {
		    			if (DEBUG) {
		    				Log.d(TAG, "parseGetVehiclesXML->scores:" + scores.toArray().toString());
		    			}	        			
		    		}	        		
		    		break;
		    	}            	              
            }
            event = parser.next();
        }  
        return scores;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseSubmitComplaintAndSuggestionXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
            case XmlPullParser.START_TAG:  
            	String name = parser.getName();
            	if (DEBUG) {
            		Log.d(TAG, "submitComplaintAndSuggestionResult->name:" + name);
            	}            	
                if ("submitComplaintAndSuggestionResult".equals(name)) {  
                	return parser.nextText();           	
                }
                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseSubmitOrderXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_TAG:  
	            	String name = parser.getName();
//	            	if (DEBUG) {
//	            		Log.d(TAG, "submitOrderResult->name:" + name);
//	            	}            	
	                if ("result".equals(name)) {  
	                	return parser.nextText();           	
	                }
	                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static GetCitiesResult parseGetCitiesXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        GetCitiesResult result = null;
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	result = new GetCitiesResult(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("result".equals(name)) {  
	                	result.mResult = parser.nextText();
	                } else if ("citiesCount".equals(name)) {  
	                	result.mCitiesCount = Long.parseLong(parser.nextText());	                
	                } else if ("cities".equals(name)) {
	                	result.mCityList = new ArrayList<String>();
	                } else if ("name".equals(name)) {
	                	result.mCityList.add(parser.nextText());
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {	        		
		    		break;
		    	} 
            }  
            event = parser.next();  
        }  
        return result;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static String parseAddFavoriteXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_TAG:  
	            	String name = parser.getName();
	                if ("addFavoriteResult".equals(name)) {  
	                	return parser.nextText();           	
	                }
	                break;  
            }  
            event = parser.next();  
        }  
        return null;           
    }    
    
    /**  
     * Parser the xml type data which responses from web service. 
     *   
     * @param responseStream  
     * @return  
     * @throws Exception  
     */  
    private static GetElecStationsResult parseGetElecStationXML(InputStream responseStream) throws Exception {  
        XmlPullParser parser = Xml.newPullParser();  
        parser.setInput(responseStream, "UTF-8");  
        GetElecStationsResult result = null;
        Station station = null;
        int event = parser.getEventType();  
        while (event != XmlPullParser.END_DOCUMENT) {  
            switch (event) {  
	            case XmlPullParser.START_DOCUMENT: {
	            	result = new GetElecStationsResult(); 
	            	break;
	            }            
	            case XmlPullParser.START_TAG: {  
	            	String name = parser.getName();            	
	                if ("result".equals(name)) {  
	                	result.mResult = parser.nextText();
	                } else if ("stationsCount".equals(name)) {  
	                	result.mStationsCount = Long.parseLong(parser.nextText());	                
	                } else if ("stations".equals(name)) {
	                	result.mStationList = new ArrayList<Station>();
	                } else if ("station".equals(name)) {
	                	station = new Station();
	                } else if ("name".equals(name)) {
	                	station.mName = parser.nextText();
	                } else if ("address".equals(name)) {
	                	station.mAddress = parser.nextText();
	                } else if ("longitude".equals(name)) {
	                	station.mLongitude = Double.parseDouble(parser.nextText());
	                } else if ("latitude".equals(name)) {
	                	station.mLatitude = Double.parseDouble(parser.nextText());
	                }
	                break;  
	            }  
		        case XmlPullParser.END_TAG: {
		        	if ("station".equals(parser.getName())) {
		        		result.mStationList.add(station);
		        		station = null;
		        	}		        	
		        	break;
		        }
		    	case XmlPullParser.END_DOCUMENT: {	        		
		    		break;
		    	} 
            }  
            event = parser.next();  
        }  
        return result;           
    }     
    
    /**  
     * Read data from input stream
     * 
     * @param inStream  
     * @return  
     * @throws Exception  
     */  
    public static byte[] readInputStream(InputStream inStream) throws Exception{  
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        while( (len = inStream.read(buffer)) !=-1 ){  
            outSteam.write(buffer, 0, len);  
        }  
        outSteam.close();  
        inStream.close();  
        return outSteam.toByteArray();  
    }          
}