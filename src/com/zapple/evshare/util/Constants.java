/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
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
	public static final String LOCATION_CHANGE_ACTION = "com.zapple.evshare.LOCATION_CHANGE_ACTION";
	
	// extra
	public static final String TITLE_CHANGE_EXTRA = "com.zapple.evshare.TITLE_CHANGE_EXTRA";
	public static final String LOCATION_CHANGE_EXTRA = "com.zapple.evshare.LOCATION_CHANGE_EXTRA";
	public static final String SUBMIT_ORDER_EXTRA = "submit_order_extra";
	public static final String WHICH_MANAGEMENT_EXTRA = "which_management_extra";
	public static final String FLOW_MODE_EXTRA = "flow_mode_extra";
	public static final String ACCOUNT_NAME_EXTRA = "account_name_extra";
	public static final String ACCOUNT_PASSWORD_EXTRA = "account_password_extra";
	public static final String LOCATION_EXTRA = "location_extra";
	
	// key prefrence
	public static final String LOCATION_KEY = "location_key";
	public static final String LONGITUDE_KEY = "longitude_key";
	public static final String LATITUDE_KEY = "latitude_key";
	
	
	public static final int ORDER_STATUS_WAITING_TAKE = 0;
	public static final int ORDER_STATUS_UNPAID = 1;
	public static final int ORDER_STATUS_RENTING = 2;
	public static final int ORDER_STATUS_RETURNED = 3;
}