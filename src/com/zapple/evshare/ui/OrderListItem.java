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
 * This class manages the view for given Order item.
 */
public class OrderListItem extends LinearLayout {
    private static final String TAG = "OrderListItem";
    private static final boolean DEBUG = true;
    
    private TextView mNameTextView;
    private TextView mStatusTextView;
    
    private OrderItem mOrderItem;
    private Context mContext;
    private int mFromWhere;

    public OrderListItem(Context context) {
        super(context);
        mContext = context;
    }

    public OrderListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.v(TAG, "OrderListItem");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
    	if (DEBUG) Log.v(TAG, "onFinishInflate");
        super.onFinishInflate();
        
        mNameTextView = (TextView) findViewById(R.id.name_text_view);
        mStatusTextView = (TextView) findViewById(R.id.status_text_view);
    }

    public final void bind(Context context, final OrderItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mOrderItem = item;

        mNameTextView.setText(item.getName());
        mStatusTextView.setText(item.getStatus());
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind");

    }
    
    public OrderItem getListItem() {
    	return mOrderItem;
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }    
}