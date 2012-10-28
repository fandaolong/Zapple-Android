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

package com.zapple.evshare.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zapple.evshare.R;
import com.zapple.evshare.data.LoginResult;
import com.zapple.evshare.util.Constants;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;

/**
 * The MessageTab activity that has one tab with the Inbox messages,
 * a tab with Spam messages, a tab with the Importance messages and
 * a tab with the Privacy messages. This is the container and the tabs are
 * embedded using intents.
 */
public class ManagementTabActivity extends TabActivity implements 
	TabHost.OnTabChangeListener{
    private static final String TAG = ManagementTabActivity.class.getSimpleName();
    private static final boolean DEBUG = true;
    
    private Context mContext;
    private TabHost mTabHost;
    private TextView mLoactionTextView;
    private int mWidth;
	private String mOrderManagementTitle;
	private String mAccountManagementTitle;
	private String mMemberManagementTitle;
	
    private TabSpec mReservationTabSpec;
	private TabSpec mMyZappleTabSpec;
	private TabSpec mHelptabSpec;
    private String[] mTagSpec;
    private SharedPreferences mSharedPreferences;
    private final BroadcastReceiver mTitleChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.TITLE_CHANGE_ACTION.equals(intent.getAction())) {
				doActionSetTitle(
						intent.getStringExtra(Constants.TITLE_CHANGE_EXTRA));
			}
		}
    };
    
    @Override
    protected void onCreate(Bundle icicle) {
    	if (DEBUG) Log.d(TAG, "onCreate");
        super.onCreate(icicle);
        
        final Intent intent = getIntent();

        setContentView(R.layout.main_tab_activity);

        mContext = this;
        mLoactionTextView = (TextView) findViewById(R.id.location_text_view);
        mTabHost = getTabHost();
        mTabHost.setOnTabChangedListener(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String locationLabel = mSharedPreferences.getString(Constants.LOCATION_KEY, null);
        /*
         * get window width and set the tab width.
         */
        mWidth = getWindowManager().getDefaultDisplay().getWidth();
        mWidth = mWidth/3;
        if (DEBUG) Log.d(TAG, "onCreate.mWidth." + mWidth);
        mOrderManagementTitle = getString(R.string.order_management_cell_item_name);
        mAccountManagementTitle = getString(R.string.account_management_cell_item_name);
        mMemberManagementTitle = getString(R.string.member_management_cell_item_name);
        mTagSpec = new String[] {
        		mOrderManagementTitle, 
        		mAccountManagementTitle,
        		mMemberManagementTitle};
        // Setup the tabs
        setupOrderManagementTab();
        setupAccountManagementTab();
        setupMemberManagementTab();

        setCurrentTab(intent);
//        GetLoactionTask task = new GetLoactionTask();
//        task.execute();
        mLoactionTextView.setText(locationLabel == null ? "" : locationLabel);
        registerReceiver(mTitleChangeReceiver, new IntentFilter(Constants.TITLE_CHANGE_ACTION));
    }

    @Override
    protected void onPause() {
    	if (DEBUG) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy(){
    	if (DEBUG) Log.d(TAG, "onDestroy");
    	super.onDestroy();
    	unregisterReceiver(mTitleChangeReceiver);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_tab_menu, menu);
//        menu.add(Menu.NONE, 1, 1, getString(R.string.action_bar_search));
//        menu.add(Menu.NONE, 2, 2, getString(R.string.action_bar_login));
//        menu.add(Menu.NONE, 3, 3, getString(R.string.action_bar_favorite));
//        menu.add(Menu.NONE, 4, 4, getString(R.string.action_bar_help));
//        menu.add(Menu.NONE, 5, 5, getString(R.string.action_bar_setting));
//        menu.add(Menu.NONE, 6, 6, getString(R.string.action_bar_about));
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchView.setOnQueryTextListener(this);
        return true;
    }    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.action_search: {
	    		doActionEnterSearch();
	    		break;
	    	}
	    	case R.id.action_logout: {
	    		doActionEnterLogout();
	    		break;
	    	}
	    	case R.id.action_favorite: {
	    		doActionEnterFavorite();
	    		break;
	    	}
	    	case R.id.action_help: {
	    		doActionEnterHelp();
	    		break;
	    	}
	    	case R.id.action_setting: {
	    		doActionEnterSetting();
	    		break;
	    	}
	    	case R.id.action_about: {
	    		doActionEnterAbout();
	    		break;
	    	}	    	
    	}
//        Toast.makeText(this, "Selected Item: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }    
    
    private void setupOrderManagementTab() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this, OrderManagementActivity.class);
        final LayoutInflater inflater = getLayoutInflater();
        TextView tabItem = (TextView) inflater.inflate(R.layout.tab_item, mTabHost, false);
        tabItem.setWidth(mWidth);
        tabItem.setText(mOrderManagementTitle);
        
        mReservationTabSpec = mTabHost.newTabSpec(mTagSpec[0]);
        mReservationTabSpec.setIndicator(tabItem);
//        mReservationTabSpec.setIndicator(mReservationTitle);
		mReservationTabSpec.setContent(intent);
        mTabHost.addTab(mReservationTabSpec);     
//        mTabHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.tab_indicator);
        
    }
   
    private void setupAccountManagementTab() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this, AccountManagementActivity.class);
        final LayoutInflater inflater = getLayoutInflater();
        TextView tabItem = (TextView) inflater.inflate(R.layout.tab_item, mTabHost, false);
        tabItem.setWidth(mWidth);
        tabItem.setText(mAccountManagementTitle);
        mMyZappleTabSpec = mTabHost.newTabSpec(mTagSpec[1]);
        mMyZappleTabSpec.setIndicator(tabItem);
//        mMyZappleTabSpec.setIndicator(mMyZappleTitle);        
        mMyZappleTabSpec.setContent(intent);
        mTabHost.addTab(mMyZappleTabSpec);  
//        mTabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.tab_indicator);
    }

    private void setupMemberManagementTab() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this, MemberManagementActivity.class);
        final LayoutInflater inflater = getLayoutInflater();
        TextView tabItem = (TextView) inflater.inflate(R.layout.tab_item, mTabHost, false);
        tabItem.setWidth(mWidth);
        tabItem.setText(mMemberManagementTitle);
        mHelptabSpec = mTabHost.newTabSpec(mTagSpec[2]);
        mHelptabSpec.setIndicator(tabItem);
//        mHelptabSpec.setIndicator(mHelpTitle);
        mHelptabSpec.setContent(intent);
        mTabHost.addTab(mHelptabSpec); 
//        mTabHost.getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.tab_indicator);
    }
    
    /**
     * Sets the current tab based on the intent's request type
     *
     * @param recentCallsRequest true is the recent calls tab is desired, false otherwise
     */
    private void setCurrentTab(Intent intent) {
        // Dismiss menu provided by any children activities
//        Activity activity = getLocalActivityManager().
//                getActivity(mTabHost.getCurrentTabTag());
//        if (activity != null) {
//            activity.closeOptionsMenu();
//        }

        // Tell the children activities that they should ignore any possible saved
        // state and instead reload their state from the parent's intent
//        intent.putExtra(EXTRA_IGNORE_STATE, true);
        int currentIndex = 0;
        if (intent != null) {
        	currentIndex = intent.getIntExtra(Constants.WHICH_MANAGEMENT_EXTRA, 0);
        }
        mTabHost.setCurrentTab(currentIndex);

        // Tell the children activities that they should honor their saved states
        // instead of the state from the parent's intent
//        intent.putExtra(EXTRA_IGNORE_STATE, false);
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        setCurrentTab(newIntent);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            // Instead of stopping, simply push this to the back of the stack.
            // This is only done when running at the top of the stack;
            // otherwise, we have been launched by someone else so need to
            // allow the user to go back to the caller.
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
        }
    } 
    
    /** {@inheritDoc} */
    public void onTabChanged(String tabId) {
        // Because we're using Activities as our tab children, we trigger
        // onWindowFocusChanged() to let them know when they're active.  This may
        // seem to duplicate the purpose of onResume(), but it's needed because
        // onResume() can't reliably check if a keyguard is active.
//		if (tabId.equals(mTagSpec[3])) {
//			mTabHost.setCurrentTab(mCurrentTab);
////				privacyInputDialog();
//		} else {
//			for (int i = 0; i < 4; i++) {
//				if (tabId.equals(mTagSpec[i])) {
//					if (i != mCurrentTab) {
//						mCurrentTab = i;
//					}
//					break;
//				}
//			}
//		}
    	
//        Activity activity = getLocalActivityManager().getActivity(tabId);
//        if (activity != null) {
//            activity.onWindowFocusChanged(true);
//        }
    }
    
    // private method do action section
    private void doActionSetTitle(String title) {
    	if (DEBUG) Log.d(TAG, "doActionSetTitle.title." + title);
    	if (!TextUtils.isEmpty(title)) {
    		this.setTitle(title);
    	}    	
    }
    
    private void doActionEnterSearch() {
    	Intent intent = new Intent(mContext, QueryStoreActivity.class);
    	startActivity(intent);
    }
    
    private void doActionEnterLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.confirm_logout_dialog_title)
            .setCancelable(true)
            .setPositiveButton(R.string.cancel_label, null)
            .setNegativeButton(R.string.ok_label, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					exitApp();
				}            	
            })
            .show();	
    }    
    
    private void doActionEnterLogin() {
    	Intent intent = new Intent(mContext, LoginActivity.class);
    	startActivity(intent);    	
    }
    
    private void doActionEnterFavorite() {
    	Intent intent = new Intent(mContext, FavoriteActivity.class);
    	startActivity(intent);    	
    }
    
    private void doActionEnterHelp() {
    	Intent intent = new Intent(mContext, HelpActivity.class);
    	startActivity(intent);    	
    }
    
    private void doActionEnterSetting() {
    	Intent intent = new Intent(mContext, SettingActivity.class);
    	startActivity(intent);    	
    }
    
    private void doActionEnterAbout() {
    	Intent intent = new Intent(mContext, AboutActivity.class);
    	startActivity(intent);    	
    }
    

    // private method section
	private void exitApp() {
		String stringSDK = android.os.Build.VERSION.SDK;
		int sdkversion = Integer.valueOf(stringSDK).intValue();
		if (sdkversion <= 7) {
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			// require for restart permission
			am.restartPackage(getPackageName());
		} else {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
    
    /**
     * 抓取当前的城市信息
     * 
     * @return String 城市名
     */
    private String getCurrentCityName() {
		if (DEBUG) Log.d(TAG, "getCurrentCityName");
		String city = "";
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation glc = (GsmCellLocation) telManager.getCellLocation();

		if (glc != null) {
			int cid = glc.getCid(); // value 基站ID号
			int lac = glc.getLac();// 写入区域代码
			String strOperator = telManager.getNetworkOperator();
			int mcc = Integer.valueOf(strOperator.substring(0, 3));// 写入当前城市代码
			int mnc = Integer.valueOf(strOperator.substring(3, 5));// 写入网络代码
			String getNumber = "";
			getNumber += ("cid:" + cid + "\n");
			getNumber += ("cid:" + lac + "\n");
			getNumber += ("cid:" + mcc + "\n");
			getNumber += ("cid:" + mnc + "\n");
			DefaultHttpClient client = new DefaultHttpClient();
			BasicHttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, 20000);
			HttpPost post = new HttpPost("http://www.google.com/loc/json");

			try {
				JSONObject jObject = new JSONObject();
				jObject.put("version", "1.1.0");
				jObject.put("host", "maps.google.com");
				jObject.put("request_address", true);
				if (mcc == 460)
					jObject.put("address_language", "zh_CN");
				else
					jObject.put("address_language", "en_US");

				JSONArray jArray = new JSONArray();
				JSONObject jData = new JSONObject();
				jData.put("cell_id", cid);
				jData.put("location_area_code", lac);
				jData.put("mobile_country_code", mcc);
				jData.put("mobile_network_code", mnc);
				jArray.put(jData);
				jObject.put("cell_towers", jArray);
				StringEntity se = new StringEntity(jObject.toString());
				post.setEntity(se);

				HttpResponse resp = client.execute(post);
				BufferedReader br = null;
				if (DEBUG) Log.d(TAG, "getCurrentCityName.resp:" + resp);
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					br = new BufferedReader(new InputStreamReader(resp
							.getEntity().getContent()));
					StringBuffer sb = new StringBuffer();

					String result = br.readLine();
					while (result != null) {
						sb.append(getNumber);
						sb.append(result);
						result = br.readLine();
					}
					String s = sb.toString();
					if (DEBUG) Log.d(TAG, "getCurrentCityName:" + s);
					s = s.substring(s.indexOf("{"));
					JSONObject jo = new JSONObject(s);
					JSONObject arr = jo.getJSONObject("location");
					JSONObject address = arr.getJSONObject("address");
					city = address.getString("city");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				post.abort();
				client = null;
			}
		}
		return city;
	} 
	
    // private class section
	private class GetLoactionTask extends AsyncTask<Void, Void, String> {
		public GetLoactionTask() {
			
		}

		@Override
		protected String doInBackground(Void... params) {
			return getCurrentCityName();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (DEBUG) Log.d(TAG, "get location result:" + result);
			if (TextUtils.isEmpty(result)) {
				return;
			}
			mLoactionTextView.setText(result);
		}
	}
}