/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */
package com.zapple.evshare.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.zapple.evshare.R;
import com.zapple.evshare.data.NewFragmentInfo;
import com.zapple.evshare.util.Constants;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;

/**
 * Combining a TabHost with a ViewPager to implement a tab UI
 * that switches between tabs and also allows the user to perform horizontal
 * flicks to move between the tabs.
 */
public class MainActivity extends FragmentActivity {	
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = true;	
	private String mReservationTitle;
	private String mMyZappleTitle;
	private String mHelpTitle;
    private TabHost mTabHost;
    private ViewPager  mViewPager;
    private TabsAdapter mTabsAdapter;
    private HashMap<String, Class<?>> mClassPool = new HashMap<String, Class<?>>();
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
        	if (DEBUG) Log.e(TAG, "handleMessage.what." + msg.what);
            switch (msg.what) {
                case Constants.MSG_START_NEW_FRAGMENT: {
                	if (mTabsAdapter != null) {
                		NewFragmentInfo info = (NewFragmentInfo) msg.obj;
                		mTabsAdapter.setTabClass(info.mTabIndex, mClassPool.get(info.mName), info.mArgs);
                	}
                	break;
                }
                default: {
                	if (DEBUG) Log.e(TAG, "handleMessage.default." + msg);
                }
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (ViewPager)findViewById(R.id.pager);

        mClassPool.put(StoreDetailFragment.class.getSimpleName(), StoreDetailFragment.class);
        mClassPool.put(ReservationServiceFragment.class.getSimpleName(), ReservationServiceFragment.class);
        
        mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
        mReservationTitle = getString(R.string.reservation_label);
        mMyZappleTitle = getString(R.string.my_zapple_label);
        mHelpTitle = getString(R.string.help_label);
        mTabsAdapter.addTab(
        		mTabHost
        		.newTabSpec(mReservationTitle)
        		.setIndicator(mReservationTitle),
        		ReservationServiceFragment.class, 
        		null);
        mTabsAdapter.addTab(
        		mTabHost
        		.newTabSpec(mMyZappleTitle)
        		.setIndicator(mMyZappleTitle),
        		ReservationServiceFragment.class, 
        		null);
        mTabsAdapter.addTab(
        		mTabHost
        		.newTabSpec(mHelpTitle)
        		.setIndicator(mHelpTitle),
        		ReservationServiceFragment.class, 
        		null);

        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
        
        // just for test
        Intent intent = new Intent(this, FragmentTabsMainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }    

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final TabHost mTabHost;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabsAdapter(FragmentActivity activity, TabHost tabHost, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mTabHost = tabHost;
            mViewPager = pager;
            mTabHost.setOnTabChangedListener(this);
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
            notifyDataSetChanged();
        }
        
        public void setTabClass(int index, Class<?> clss, Bundle args) {
        	if (index < 0 || clss == null) {
        		if (DEBUG) Log.v(TAG, "setTabClass.index." + index + ", clss." + clss);
        		return;
        	}
        	TabInfo info = mTabs.get(index);
        	TabInfo newInfo = new TabInfo(info.tag, clss, args);
        	mTabs.set(index, newInfo);
        	notifyDataSetChanged();
        	mTabHost.setCurrentTab(index);
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
        	if (DEBUG) Log.v(TAG, "getItem.position." + position);
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(), info.args);
        }

        @Override
        public void onTabChanged(String tabId) {
            int position = mTabHost.getCurrentTab();
            mViewPager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Unfortunately when TabHost changes the current tab, it kindly
            // also takes care of putting focus on it when not in touch mode.
            // The jerk.
            // This hack tries to prevent this from pulling focus out of our
            // ViewPager.
            TabWidget widget = mTabHost.getTabWidget();
            int oldFocusability = widget.getDescendantFocusability();
            widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            mTabHost.setCurrentTab(position);
            widget.setDescendantFocusability(oldFocusability);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}