/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

public class LoginResult {
	public static final String LOGIN_RESULT_ACCOUNT_KEY = "login_result_account_key";
	public static final String LOGIN_RESULT_PASSWORD_KEY = "login_result_password_key";	
	public static final String LOGIN_RESULT_TOTAL_SCORES_KEY = "login_result_total_scores_key";
	public static final String LOGIN_RESULT_EXPIRY_DATE_KEY = "login_result_expiry_date_key";
	public static final String LOGIN_RESULT_ACCOUNT_DATA_KEY = "login_result_account_data_key";
	
    /**
     * The unique ID for a row.
     * <P>Type: INTEGER (long)</P>
     */
	public String mLoginResult;
	public long mTotalScores;
	public long mExpiryDate;
	public PersonalInfo mPersonalInfo;    
    public String mAccountData;
}