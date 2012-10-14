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
package com.zapple.evshare.util;

public class Constants {
    /*** config ***/
    public static final String SERVER_HOST = "http://evshare.zapple.com.cn/";
    public static final String SOAP_ADDRESS = "WebServices/kiosk.asmx";
    public static final String FAVORITE_TYPE_STORE = "store";
    public static final String FAVORITE_TYPE_VEHICLE = "vehicle";
    
	public static final int FROM_QUICK_ORDER = 1;
	public static final int FROM_RESERVATION_VEHICLE = 2;
	public static final int FROM_STORE_LIST = 3;
	public static final int FROM_FAVORITE = 4;    
	
	// msg
	public static final int MSG_START_NEW_FRAGMENT = 100;
	
	// tab index
	public static final int TAB_INDEX_RESERVATION = 0;
	public static final int TAB_ORDER_MANAGEMENT = 0;
	public static final int TAB_ACCOUNT_MANAGEMENT = 1;
	public static final int TAB_MEMBER_MANAGEMENT = 2;
	
    /** Argument name(s) */
	public static final String ARG_FROM_WHERE_EXTRA = "from_were_extra";
	public static final String ARG_TAKE_VEHICLE_DATE_EXTRA = "take_vehicle_date_extra"; 
	public static final String ARG_STORE_NAME_EXTRA = "store_name_extra";
	public static final String ARG_STORE_ID_EXTRA = "store_id_extra";	
	
	// intent action
	public static final String TITLE_CHANGE_ACTION = "com.zapple.evshare.TITLE_CHANGE_ACTION";
	
	// extra
	public static final String TITLE_CHANGE_EXTRA = "com.zapple.evshare.TITLE_CHANGE_EXTRA";
	public static final String SUBMIT_ORDER_EXTRA = "submit_order_extra";
	public static final String WHICH_MANAGEMENT_EXTRA = "which_management_extra";
}