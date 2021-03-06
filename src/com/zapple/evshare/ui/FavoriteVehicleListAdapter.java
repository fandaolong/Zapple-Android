/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.zapple.evshare.R;
import com.zapple.evshare.data.Vehicle.VehicleTable;

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
public class FavoriteVehicleListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "VehicleListAdapter";
    private static final boolean DEBUG = true;
    static final String[] VEHICLE_PROJECTION = new String[] {
    	VehicleTable._ID,             // 0
    	VehicleTable.REMOTE_ID,       // 1
    	VehicleTable.BRAND,           // 2
    	VehicleTable.MODEL,           // 3
    	VehicleTable.PRICE,           // 4
    	VehicleTable.DUMP_ENERGY,     // 5
    	VehicleTable.PARKING_GARAGE,  // 6
    	VehicleTable.PHOTO_URI,       // 7
    };
    public static final int COLUMN_ID              = 0;
    public static final int COLUMN_REMOTE_ID       = 1; 
    public static final int COLUMN_BRAND           = 2;     
    public static final int COLUMN_MODEL           = 3;
    public static final int COLUMN_PRICE           = 4;
    public static final int COLUMN_DUMP_ENERY      = 5;
    public static final int COLUMN_PARKING_GARAGE  = 6;
    public static final int COLUMN_PHOTO_URI       = 7;    
    
    private final LayoutInflater mFactory;
    private OnContentChangedListener mOnContentChangedListener;
    private int mFromWhere;

    public FavoriteVehicleListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (!(view instanceof VehicleListItem)) {
            Log.e(TAG, "Unexpected bound view: " + view);
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "bindView.view." + view);
        }

        VehicleListItem headerView = (VehicleListItem) view;
        VehicleItem item = new VehicleItem(cursor);
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
        VehicleListItem listItem = (VehicleListItem)view;
        listItem.unbind();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.v(TAG, "newView");
        return mFactory.inflate(R.layout.vehicle_list_item, parent, false);
    }
    
    public interface OnContentChangedListener {
        void onContentChanged(FavoriteVehicleListAdapter adapter);
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