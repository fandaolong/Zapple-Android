/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;


import com.zapple.evshare.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class manages the view for given PilesSite item.
 */
public class PilesSiteListItem extends LinearLayout {
    private static final String TAG = "PilesSiteListItem";
    private static final boolean DEBUG = true;
    
    private TextView mNameTextView;
    private TextView mAddressTextView;
    private Button mFavoriteButton;
    
    private PilesSiteItem mPilesSiteItem;
    private Context mContext;
    private int mFromWhere;

    public PilesSiteListItem(Context context) {
        super(context);
        mContext = context;
    }

    public PilesSiteListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.v(TAG, "PilesSiteListItem");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
    	if (DEBUG) Log.v(TAG, "onFinishInflate");
        super.onFinishInflate();
        
        mNameTextView = (TextView) findViewById(R.id.name_text_view);
        mAddressTextView = (TextView) findViewById(R.id.address_text_view);
//        mFavoriteButton = (Button) findViewById(R.id.favorite_button);
        
//        mReservationButton.setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				if (PilesSiteDetailActivity.FROM_QUICK_ORDER == mFromWhere) {
//					Intent i = new Intent(mContext, QuickOrderTimeActivity.class);				
//					mContext.startActivity(i);					
//				} else if (PilesSiteDetailActivity.FROM_RESERVATION_VEHICLE == mFromWhere) {
//					Intent i = new Intent(mContext, OrderChooseServiceActivity.class);				
//					mContext.startActivity(i);					
//				}
//			}
//		});
    }

    public final void bind(Context context, final PilesSiteItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mPilesSiteItem = item;

        mNameTextView.setText(item.getName());
        mAddressTextView.setText(item.getAddress());
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind");

    }
    
    public PilesSiteItem getListItem() {
    	return mPilesSiteItem;
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }    
}