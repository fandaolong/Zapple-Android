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
 * This class manages the view for given Score item.
 */
public class ScoreListItem extends LinearLayout {
    private static final String TAG = ScoreListItem.class.getSimpleName();
    private static final boolean DEBUG = true;
    
    private TextView mIdTextView;
    private TextView mDescriptionTextView;
    private TextView mDateTextView;
    private TextView mPaidChannelTypeTextView;

    
    private ScoreItem mScoreItem;
    private Context mContext;
    private int mFromWhere;

    public ScoreListItem(Context context) {
        super(context);
        mContext = context;
    }

    public ScoreListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.v(TAG, "ScoreListItem");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
    	if (DEBUG) Log.v(TAG, "onFinishInflate");
        super.onFinishInflate();
        
        mIdTextView = (TextView) findViewById(R.id.id_text_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);
        mDateTextView = (TextView) findViewById(R.id.date_text_view);
        mPaidChannelTypeTextView = (TextView) findViewById(R.id.paid_channel_type_text_view);
    }

    public final void bind(Context context, final ScoreItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mScoreItem = item;

		if (item != null) {
			mIdTextView.setText(mContext.getResources().getString(
					R.string.score_list_item_id_label)
					+ item.getRemoteId());     
			mDescriptionTextView.setText(mContext.getResources().getString(
					R.string.score_list_item_description_label) + item.getDescription());
			mDateTextView.setText(mContext.getResources().getString(
					R.string.score_list_item_date_label) + item.getDate());
			// TODO: remove the dead string, use the data from server.
			mPaidChannelTypeTextView
					.setText(mContext
							.getResources()
							.getString(
									R.string.score_list_item_paid_channel_type_label)
							+ "招商银行网银-在线支付");			
		}
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind");

    }
    
    public ScoreItem getListItem() {
    	return mScoreItem;
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }    
}