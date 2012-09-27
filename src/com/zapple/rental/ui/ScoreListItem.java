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

package com.zapple.rental.ui;

import com.zapple.rental.R;

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
    private static final String TAG = "ScoreListItem";
    private static final boolean DEBUG = true;
    
    private TextView mDescriptionTextView;
    private TextView mScoreValueTextView;
    private TextView mDateTextView;
    private TextView mExpiryDateTextView;

    
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
        
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);
        mScoreValueTextView = (TextView) findViewById(R.id.score_value_text_view);
        mDateTextView = (TextView) findViewById(R.id.date_text_view);
        mExpiryDateTextView = (TextView) findViewById(R.id.expiry_date_text_view);
    }

    public final void bind(Context context, final ScoreItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mScoreItem = item;

        mDescriptionTextView.setText(item.getDescription());
        mScoreValueTextView.setText(item.getScoreValue());
        mDateTextView.setText(item.getDate());
        mExpiryDateTextView.setText(item.getExpiryDate());
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