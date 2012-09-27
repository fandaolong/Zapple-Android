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
import com.zapple.rental.data.Score.ScoreTable;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

/**
 * The back-end data adapter for ScoreList.
 */
public class ScoreListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "ScoreListAdapter";
    private static final boolean DEBUG = true;
    static final String[] SCORE_PROJECTION = new String[] {
    	ScoreTable._ID,            // 0
    	ScoreTable.REMOTE_ID,      // 1
    	ScoreTable.DESCRIPTION,           // 2
    	ScoreTable.SCORE_VALUE,        // 3
    	ScoreTable.DATE,      // 4
    	ScoreTable.EXPIRY_DATE       // 5
    };
    public static final int COLUMN_ID           = 0;
    public static final int COLUMN_REMOTE_ID    = 1; 
    public static final int COLUMN_DESCRIPTION  = 2;     
    public static final int COLUMN_SCORE_VALUE  = 3;
    public static final int COLUMN_DATE         = 4;
    public static final int COLUMN_EXPIRY_DATE  = 5;
    
    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private int mFromWhere;

    public ScoreListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof ScoreListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "bindView.view." + view);
        }

        ScoreListItem headerView = (ScoreListItem) view;
        ScoreItem item = new ScoreItem(cursor);
        headerView.bind(context, item);
        headerView.setFromWhere(mFromWhere);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        return v;
    }

    public void onMovedToScrapHeap(View view) {
        if (DEBUG) Log.v(TAG, "onMovedToScrapHeap");
        ScoreListItem listItem = (ScoreListItem)view;
        listItem.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.v(TAG, "newView");
        return mFactory.inflate(R.layout.score_list_item, parent, false);
    }
    
    public interface OnContentChangedListener {
        void onContentChanged(ScoreListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    @Override
    public void notifyDataSetChanged() {
        if (DEBUG) Log.v(TAG, "notifyDataSetChanged");
        super.notifyDataSetChanged();
//        if (mCursor != null && !mCursor.isClosed()) {
//            if (mOnContentChangedListener != null) {
//                mOnContentChangedListener.onContentChanged(this);
//            }
//        }        
    }
    
    @Override
    protected void onContentChanged() {
        if (this.getCursor() != null && !this.getCursor().isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }
}
