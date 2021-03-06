/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
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
 * This class manages the view for given account management item.
 */
public class AccountManagementListItem extends LinearLayout {
	private static final String TAG = AccountManagementListItem.class
			.getSimpleName();
	private static final boolean DEBUG = true;

	private TextView mIdTextView;
	private TextView mFeeTextView;
	private TextView mDateTextView;
	private TextView mPaidChannelTypeTextView;

	private Context mContext;
	private Order mOrder;

	public AccountManagementListItem(Context context) {
		super(context);
		mContext = context;
	}

	public AccountManagementListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG)
			Log.v(TAG, "OrderListItem");
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		if (DEBUG)
			Log.v(TAG, "onFinishInflate");
		super.onFinishInflate();

		mIdTextView = (TextView) findViewById(R.id.id_text_view);
		mFeeTextView = (TextView) findViewById(R.id.fee_text_view);
		mDateTextView = (TextView) findViewById(R.id.date_text_view);
		mPaidChannelTypeTextView = (TextView) findViewById(R.id.paid_channel_type_text_view);
	}

	public final void bind(Context context, final Order order) {
		mOrder = order;
		if (DEBUG)
			Log.v(TAG, "bind");
		if (order != null) {
			mIdTextView.setText(mContext.getResources().getString(
					R.string.account_management_list_item_id_label)
					+ order.mId);
			// TODO: should amount all fees.
			String feeLabel = mContext.getResources().getString(
					R.string.account_management_list_item_fee_label)
					+ order.mVehicleRents;
			mFeeTextView.setText(feeLabel);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(order.mTakeVehicleDate);
			String date1 = sdf.format(c.getTime());
			mDateTextView.setText(mContext.getResources().getString(
					R.string.account_management_list_item_date_label)
					+ date1);
			// TODO: remove the dead string, use the data from server.
			mPaidChannelTypeTextView
					.setText(mContext
							.getResources()
							.getString(
									R.string.account_management_list_item_paid_channel_type_label)
							+ "招商银行网银-在线支付");
		}

	}

	public final void unbind() {
		if (DEBUG)
			Log.v(TAG, "unbind");

	}

	public Order getListItem() {
		return mOrder;
	}

}