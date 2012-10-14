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