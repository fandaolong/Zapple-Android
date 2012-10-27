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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.zapple.evshare.R;
import com.zapple.evshare.data.Order;
import com.zapple.evshare.util.Constants;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class manages the view for given Order item.
 */
public class OrderListItem extends LinearLayout {
    private static final String TAG = OrderListItem.class.getSimpleName();
    private static final boolean DEBUG = true;
    
    private TextView mBrandModelTextView;
    private TextView mOrderStatusTextView;
    private TextView mCancelOrderTextView;
    private TextView mDateTextView;
    private TextView mTakeVehicleTextView;
    
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
        
        mBrandModelTextView = (TextView) findViewById(R.id.brand_model_text_view);
        mOrderStatusTextView = (TextView) findViewById(R.id.order_status_text_view);
        mCancelOrderTextView = (TextView) findViewById(R.id.cancel_order_text_view);
        mDateTextView = (TextView) findViewById(R.id.date_text_view);
        mTakeVehicleTextView = (TextView) findViewById(R.id.take_vehicle_address_text_view);
        
//        mCancelOrderTextView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				
//			}
//        	
//        });
    }

    public final void bind(Context context, final OrderItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mOrderItem = item;
        Order order = item.getOrder();
        if (order != null) {
        	String brandModelLabel = mContext.getResources().getString(R.string.order_management_brand_model_label) + order.mVehicleBrand + " ( " + order.mVehicleModel + " ) ";
        	int statusCode = 0;
        	try {
        		statusCode = Integer.parseInt(order.mStatus);
        	} catch (Exception e) {
        		
        	}
        	
        	String orderStatusLabel = mContext.getResources().getString(R.string.order_management_order_id_status_label) + order.mId + "-" + getOrderStatusLabel(statusCode);
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	final Calendar c = Calendar.getInstance();
        	c.setTimeInMillis(order.mTakeVehicleDate);
        	String date1 = sdf.format(c.getTime());
        	c.setTimeInMillis(order.mReturnVehicleDate);
        	String date2 = sdf.format(c.getTime());        	
        	String dateLabel = mContext.getResources().getString(R.string.order_management_take_vehicle_date_label) + date1 + mContext.getResources().getString(R.string.order_management_return_vehicle_date_label) + date2;
        	String takeVehicleAddress =  mContext.getResources().getString(R.string.order_management_take_address_label) + order.mTakeVehicleStoreName;
            mBrandModelTextView.setText(brandModelLabel);
            mOrderStatusTextView.setText(orderStatusLabel);
            if (Constants.ORDER_STATUS_WAITING_TAKE == statusCode) {
            	mCancelOrderTextView.setText(R.string.order_management_cancel_order_label);
            } else if (Constants.ORDER_STATUS_UNPAID == statusCode) {
            	mCancelOrderTextView.setText(R.string.order_management_pay_order_label);
            }
            
            mDateTextView.setText(dateLabel);
            mTakeVehicleTextView.setText(takeVehicleAddress);
        }

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
    
    private String getOrderStatusLabel(int statusCode) {
    	String statusLabel;
    	if (Constants.ORDER_STATUS_WAITING_TAKE == statusCode) {
    		statusLabel = mContext.getResources().getString(R.string.order_status_waiting_take_label);
    	} else if (Constants.ORDER_STATUS_UNPAID == statusCode) {
    		statusLabel = mContext.getResources().getString(R.string.order_status_unpaid_label);
    	} else if (Constants.ORDER_STATUS_RENTING == statusCode) {    		
    		statusLabel = mContext.getResources().getString(R.string.order_status_renting_label);
    	} else if (Constants.ORDER_STATUS_RETURNED == statusCode) {
    		statusLabel = mContext.getResources().getString(R.string.order_status_returned_label);
    	} else {
    		statusLabel = "";
    	}
    	return statusLabel;
    }
}