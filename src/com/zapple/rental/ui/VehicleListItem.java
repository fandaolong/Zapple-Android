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

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.zapple.rental.R;
import com.zapple.rental.data.SubmitOrder;
import com.zapple.rental.util.Constants;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class manages the view for given Vehicle item.
 */
public class VehicleListItem extends LinearLayout {
    private static final String TAG = "VehicleListItem";
    private static final boolean DEBUG = true;
	
    private ImageView mVehiclePhotoImageView;
    private TextView mBrandTextView;
    private TextView mModelTextView;
    private TextView mPriceTextView;
    private TextView mDumpEnergyTextView;
    private TextView mParkingGarageTextView;
    private Button mFavoriteButton;
    private Button mReservationButton;
    
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
        mBrandTextView = (TextView) findViewById(R.id.brand_text_view);
        mModelTextView = (TextView) findViewById(R.id.model_text_view);
        mPriceTextView = (TextView) findViewById(R.id.price_text_view);
        mDumpEnergyTextView = (TextView) findViewById(R.id.dump_energy_text_view);
        mParkingGarageTextView = (TextView) findViewById(R.id.parking_garage_text_view);
        mFavoriteButton = (Button) findViewById(R.id.favorite_button);
        mReservationButton = (Button) findViewById(R.id.reservation_button);
        
        if (StoreDetailActivity.FROM_FAVORITE == mFromWhere) {
        	mFavoriteButton.setVisibility(View.GONE);
        }
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHandler != null) {
					mHandler.sendMessage(mHandler.obtainMessage(
							StoreDetailActivity.MSG_ADD_FAVORITE_VEHICLE, 
							mVehicleItem.getRemoteId()));					
				}
			}
		});
        mReservationButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mSubmitOrder.mVehicleId = mVehicleItem.getRemoteId();
				mSubmitOrder.mTakeVehicleStoreId = mVehicleItem.getStoreId();
				mSubmitOrder.mReturnVehicleStoreId = mVehicleItem.getStoreId();
				mSubmitOrder.mBrand = mVehicleItem.getBrand();
				mSubmitOrder.mModel = mVehicleItem.getModel();
				mSubmitOrder.mVehicleFee = mVehicleItem.getPrice();
				
				if ((StoreDetailActivity.FROM_QUICK_ORDER == mFromWhere) ||
						(StoreDetailActivity.FROM_STORE_LIST == mFromWhere) ||
						(StoreDetailActivity.FROM_FAVORITE == mFromWhere)) {
					Intent i = new Intent(mContext, QuickOrderTimeActivity.class);
					i.putExtra(OrderCheckActivity.SUBMIT_ORDER_EXTRA, mSubmitOrder);
					mContext.startActivity(i);					
				} else if (StoreDetailActivity.FROM_RESERVATION_VEHICLE == mFromWhere) {
					Intent i = new Intent(mContext, OrderChooseServiceActivity.class);	
					mSubmitOrder.mTakeVehicleDate = mVehicleItem.getTakeVehicleDate();
					mSubmitOrder.mReturnVehicleDate = mVehicleItem.getTakeVehicleDate();
					i.putExtra(OrderCheckActivity.SUBMIT_ORDER_EXTRA, mSubmitOrder);
					mContext.startActivity(i);					
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
        mBrandTextView.setText(mContext.getString(R.string.vehicle_brand_label) + item.getBrand());
        mModelTextView.setText(mContext.getString(R.string.vehicle_model_label) + item.getModel());
        mPriceTextView.setText(mContext.getString(R.string.vehicle_price_label) + item.getPrice());
        mDumpEnergyTextView.setText(mContext.getString(R.string.vehicle_dump_energy_label) + item.getDumpEnergy());
        mParkingGarageTextView.setText(mContext.getString(R.string.vehicle_parking_garage_label) + item.getParkingGarage());
        if (StoreDetailActivity.FROM_FAVORITE == mFromWhere) {
        	mFavoriteButton.setVisibility(View.GONE);
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
