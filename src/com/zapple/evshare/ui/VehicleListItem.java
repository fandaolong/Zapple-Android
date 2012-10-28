/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.ui;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zapple.evshare.R;
import com.zapple.evshare.data.SubmitOrder;
import com.zapple.evshare.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class manages the view for given Vehicle item.
 */
public class VehicleListItem extends LinearLayout {
    private static final String TAG = VehicleListItem.class.getSimpleName();
    private static final boolean DEBUG = true;
	
    private ImageView mVehiclePhotoImageView;
    private TextView mBrandModelTextView;
    private TextView mPriceTextView;
    private TextView mDumpEnergyTextView;
    private TextView mParkingGarageTextView;
    private TextView mFavoriteTextView;
    private TextView mReservationTextView;
    
    private VehicleItem mVehicleItem;
    private Context mContext;
    private Handler mHandler;
    private int mFromWhere;
    private SubmitOrder mSubmitOrder = new SubmitOrder();

    public VehicleListItem(Context context) {
        super(context);
        mContext = context;
    }

    public VehicleListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (DEBUG) Log.v(TAG, "VehicleListItem");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
    	if (DEBUG) Log.v(TAG, "onFinishInflate");
        super.onFinishInflate();
        
        mVehiclePhotoImageView = (ImageView) findViewById(R.id.vehicle_photo_image_view);
        mBrandModelTextView = (TextView) findViewById(R.id.brand_model_text_view);
        mPriceTextView = (TextView) findViewById(R.id.price_text_view);
        mDumpEnergyTextView = (TextView) findViewById(R.id.dump_energy_text_view);
        mParkingGarageTextView = (TextView) findViewById(R.id.parking_garage_text_view);
        mFavoriteTextView = (TextView) findViewById(R.id.favorite_text_view);
        mReservationTextView = (TextView) findViewById(R.id.reservation_text_view);
        
        if (Constants.FROM_FAVORITE == mFromWhere) {
        	mFavoriteTextView.setVisibility(View.GONE);
        }
        mFavoriteTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHandler != null) {
					mHandler.sendMessage(mHandler.obtainMessage(
							StoreDetailFragment.MSG_ADD_FAVORITE_VEHICLE, 
							mVehicleItem.getRemoteId()));					
				}
			}
		});
        mReservationTextView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSubmitOrder.mVehicleId = mVehicleItem.getRemoteId();
				mSubmitOrder.mTakeVehicleStoreId = mVehicleItem.getStoreId();
				mSubmitOrder.mReturnVehicleStoreId = mVehicleItem.getStoreId();
				mSubmitOrder.mBrand = mVehicleItem.getBrand();
				mSubmitOrder.mModel = mVehicleItem.getModel();
				mSubmitOrder.mVehicleFee = mVehicleItem.getPrice();
				mSubmitOrder.mVehiclePhotoUri = mVehicleItem.getPhotoUri();
				if (DEBUG) Log.v(TAG, "mReservationTextView.mFromWhere." + mFromWhere);
				if ((Constants.FROM_QUICK_ORDER == mFromWhere) ||
						(Constants.FROM_STORE_LIST == mFromWhere) ||
						(Constants.FROM_FAVORITE == mFromWhere)) {
					mHandler.sendMessage(mHandler.obtainMessage(
							StoreDetailFragment.MSG_RESERVATION, 
							mSubmitOrder));
//					Intent i = new Intent(mContext, QuickOrderTimeActivity.class);
//					i.putExtra(Constants.SUBMIT_ORDER_EXTRA, mSubmitOrder);
//					mContext.startActivity(i);					
				} else if (Constants.FROM_RESERVATION_VEHICLE == mFromWhere) {
					mHandler.sendMessage(mHandler.obtainMessage(
							StoreDetailFragment.MSG_RESERVATION, 
							mSubmitOrder));					
//					Intent i = new Intent(mContext, OrderChooseServiceActivity.class);	
//					mSubmitOrder.mTakeVehicleDate = mVehicleItem.getTakeVehicleDate();
//					mSubmitOrder.mReturnVehicleDate = mVehicleItem.getTakeVehicleDate();
//					i.putExtra(Constants.SUBMIT_ORDER_EXTRA, mSubmitOrder);
//					mContext.startActivity(i);					
				}
			}
		});
    }

    public final void bind(Context context, final VehicleItem item) {
        if (DEBUG) Log.v(TAG, "bind");
        mVehicleItem = item;
        if (!TextUtils.isEmpty(item.getPhotoUri())) {
        	// Get singletone instance of ImageLoader
        	ImageLoader imageLoader = ImageLoader.getInstance();
        	// Initialize ImageLoader with configuration. Do it once.
        	imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        	// Load and display image asynchronously
        	imageLoader.displayImage(Constants.SERVER_HOST + item.getPhotoUri(), mVehiclePhotoImageView);
        }
        mBrandModelTextView.setText(mContext.getString(R.string.vehicle_brand_model_label) + item.getBrand() + item.getModel());
        mPriceTextView.setText(mContext.getString(R.string.vehicle_price_label) + item.getPrice());
        mDumpEnergyTextView.setText(mContext.getString(R.string.vehicle_dump_energy_label) + item.getDumpEnergy());
        mParkingGarageTextView.setText(mContext.getString(R.string.vehicle_parking_garage_label) + item.getParkingGarage());
        if (Constants.FROM_FAVORITE == mFromWhere) {
        	mFavoriteTextView.setVisibility(View.GONE);
        }
    }

    public final void unbind() {
        if (DEBUG) Log.v(TAG, "unbind");

    }
    
    public VehicleItem getListItem() {
    	return mVehicleItem;
    }
    
    public void setFromWhere(int fromWhere) {
    	mFromWhere = fromWhere;
    }    
    
    public void setHandler(Handler handler) {
    	mHandler = handler;
    }
}
