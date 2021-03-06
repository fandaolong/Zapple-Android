/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.zapple.evshare.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class manages the view for given Store item.
 */
public class StoreListItem extends LinearLayout {
    private static final String TAG = "StoreListItem";
    private static final boolean DEBUG = true;
    
    private TextView mNameTextView;
    private TextView mAddressTextView;
    private CheckBox mFavoriteCheckBox;
    
    private StoreItem mStoreItem;
    private Context mContext;
    private int mFromWhere;

    public StoreListItem(Context context) {
        super(context);
        mContext = context;
    }

    public StoreListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.v(TAG, "StoreListItem");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
    	if (DEBUG) Log.v(TAG, "onFinishInflate");
        super.onFinishInflate();
        
        mNameTextView = (TextView) findViewById(R.id.name_text_view);
        mAddressTextView = (TextView) findViewById(R.id.address_text_view);
//        mFavoriteCheckBox = (CheckBox) findViewById(R.id.favorite_check_box);
//        
//        mFavoriteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView,
//					boolean isChecked) {
//				if (DEBUG) Log.v(TAG, "mFavoriteCheckBox." + isChecked);
//			}        	
//        });
//        mRefrigeratorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		        if (null != mHandler) {
//		        	int what;
//			        if (isChecked) {
//			            what = MSG_LIST_CHECKED;
//			        } else {
//			            what = MSG_LIST_UNCHECKED;
//			        }		        
//		            Message msg = Message.obtain(mHandler, what);
//		           // msg.obj = mRefrigeratorItem.getIceBoxId();
//		            msg.obj = mRefrigeratorItem;
//		            msg.sendToTarget();
//		        }				
//			}
//		});
    }

    public final void bind(Context context, final StoreItem item) {
        if (DEBUG) Log.v(TAG, "bind");        
        mStoreItem = item;

//        setLongClickable(false);
//        setClickable(false);    // let the list view handle clicks on the item normally. When
//                                // clickable is true, clicks bypass the listview and go straight
//                                // to this listitem. We always want the listview to handle the
//                                // clicks first.        
        
        mNameTextView.setText(item.getName());
        mAddressTextView.setText(item.getAddress());
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind");

    }
    
    public StoreItem getListItem() {
    	return mStoreItem;
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }    
}