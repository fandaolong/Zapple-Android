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
import com.zapple.evshare.data.Store.StoreTable;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

/**
 * The back-end data adapter for FavoriteList.
 */
public class FavoriteStoreListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "FavoriteStoreListAdapter";
    private static final boolean DEBUG = true;
    static final String[] STORE_PROJECTION = new String[] {
    	StoreTable._ID,            // 0
    	StoreTable.REMOTE_ID,      // 1
    	StoreTable.NAME,           // 2
    	StoreTable.ADDRESS,        // 3
    	StoreTable.LONGITUDE,      // 4
    	StoreTable.LATITUDE,       // 5
    	StoreTable.ALTITUDE        // 6
    };
    public static final int COLUMN_ID        = 0;
    public static final int COLUMN_REMOTE_ID = 1; 
    public static final int COLUMN_NAME      = 2;     
    public static final int COLUMN_ADDRESS   = 3;
    public static final int COLUMN_LONGITUDE = 4;
    public static final int COLUMN_LATITUDE  = 5;
    public static final int COLUMN_ALTITUDE  = 6; 
    
    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private int mFromWhere;

    public FavoriteStoreListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof StoreListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "bindView.view." + view);
        }

        StoreListItem headerView = (StoreListItem) view;
        StoreItem item = new StoreItem(cursor);
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
        StoreListItem listItem = (StoreListItem)view;
        listItem.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.v(TAG, "newView");
        return mFactory.inflate(R.layout.store_list_item, parent, false);
    }
    
    public interface OnContentChangedListener {
        void onContentChanged(FavoriteStoreListAdapter adapter);
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