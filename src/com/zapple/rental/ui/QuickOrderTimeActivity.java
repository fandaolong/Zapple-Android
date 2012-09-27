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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zapple.rental.R;
import com.zapple.rental.data.SubmitOrder;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class QuickOrderTimeActivity extends Activity {
    private static final String TAG = "QuickOrderTimeActivity";
    private static final boolean DEBUG = true;
    private final static long MINUTES_MAX_30 = 30*60*1000;
    
	private Button mTakeVehicleDateButton;
	private Button mTakeVehicleTimeButton;
	private Button mReturnVehicleDateButton;
	private Button mReturnVehicleTimeButton;
	private Button mNextButton;
	
	private SubmitOrder mSubmitOrder;
	private Context mContext;
    // date and time
    private int mTakeYear;
    private int mTakeMonth;
    private int mTakeDay;
    private int mTakeHour;
    private int mTakeMinute;
    private int mReturnYear;
    private int mReturnMonth;
    private int mReturnDay;
    private int mReturnHour;
    private int mReturnMinute;	
	
    private DatePickerDialog.OnDateSetListener mTakeDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mTakeYear = year;
                    mTakeMonth = monthOfYear + 1;
                    mTakeDay = dayOfMonth;
                    mTakeVehicleDateButton.setText(mTakeYear + "-" + mTakeMonth + "-" + mTakeDay);
                }
            };

    private TimePickerDialog.OnTimeSetListener mTakeTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mTakeHour = hourOfDay;
                    mTakeMinute = minute;
                    mTakeVehicleTimeButton.setText(mTakeHour + ":" + mTakeMinute);
                }
            };	
	
    private DatePickerDialog.OnDateSetListener mReturnDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear,
                        int dayOfMonth) {
                    mReturnYear = year;
                    mReturnMonth = monthOfYear + 1;
                    mReturnDay = dayOfMonth;
                    mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
                }
            };

    private TimePickerDialog.OnTimeSetListener mReturnTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mReturnHour = hourOfDay;
                    mReturnMinute = minute;
                    mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
                }
            };  	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_quick_order_time);
        
        mContext = this;
        // find view section
		mTakeVehicleDateButton = (Button) findViewById(R.id.take_vehicle_date_button);
		mTakeVehicleTimeButton = (Button) findViewById(R.id.take_vehicle_time_button);		
		mReturnVehicleDateButton = (Button) findViewById(R.id.return_vehicle_date_button);
		mReturnVehicleTimeButton = (Button) findViewById(R.id.return_vehicle_time_button);

        mNextButton = (Button) findViewById(R.id.next_button);
        
		mTakeVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(
						mContext,
                        mTakeDateSetListener,
                        mTakeYear, mTakeMonth, mTakeDay);
				dpd.show();
			}
		});
		
		mTakeVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(
						mContext,
                        mTakeTimeSetListener, mTakeHour, mTakeMinute, false);
				tpd.show();
			}
		});	
		
		mReturnVehicleDateButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleDateButton");
				DatePickerDialog dpd = new DatePickerDialog(
						mContext,
                        mReturnDateSetListener,
                        mReturnYear, mReturnMonth, mReturnDay);
				dpd.show();			
			}
		});
		
		mReturnVehicleTimeButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "mReturnVehicleTimeButton");
				TimePickerDialog tpd = new TimePickerDialog(
						mContext,
						mReturnTimeSetListener, mReturnHour, mReturnMinute, false);
				tpd.show();				
			}
		});	        
        
        mNextButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				doActionEnterNext();
			}
		});
        
		Intent intent = getIntent();
		if (intent != null) {
			mSubmitOrder = intent.getParcelableExtra(OrderCheckActivity.SUBMIT_ORDER_EXTRA);
		}
		
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        mReturnYear = c.get(Calendar.YEAR);
        mReturnMonth = c.get(Calendar.MONTH) + 1;
        mReturnDay = c.get(Calendar.DAY_OF_MONTH) + 1;
        mReturnHour = c.get(Calendar.HOUR_OF_DAY);
        mReturnMinute = c.get(Calendar.MINUTE);		
        mReturnVehicleDateButton.setText(mReturnYear + "-" + mReturnMonth + "-" + mReturnDay);
        mReturnVehicleTimeButton.setText(mReturnHour + ":" + mReturnMinute);
        mTakeYear = c.get(Calendar.YEAR);
        mTakeMonth = c.get(Calendar.MONTH) + 1;
        mTakeDay = c.get(Calendar.DAY_OF_MONTH);
        mTakeHour = c.get(Calendar.HOUR_OF_DAY);
        mTakeMinute = c.get(Calendar.MINUTE);	        
        mTakeVehicleDateButton.setText(mTakeYear + "-" + mTakeMonth + "-" + mTakeDay);
        mTakeVehicleTimeButton.setText(mTakeHour + ":" + mTakeMinute);    		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_quick_order_time, menu);
        return true;
    }   
    
    // private method do action section
    private void doActionEnterNext() {
      Intent intent = new Intent(QuickOrderTimeActivity.this, OrderChooseServiceActivity.class);
      if (mSubmitOrder != null) {
//    	  Date date = new Date();
    	  final Calendar c = Calendar.getInstance();
    	  c.set(mTakeYear, mTakeMonth-1, mTakeDay, mTakeHour, mTakeMinute);
    	  long takeVehicleDate = c.getTimeInMillis();
//    	  long takeVehicleDate = date.UTC(mTakeYear, mTakeMonth, mTakeDay, mTakeHour, mTakeMinute, 0);
    	  long currentTimeMillis = System.currentTimeMillis();
    	  if ((takeVehicleDate - currentTimeMillis) > MINUTES_MAX_30 || (takeVehicleDate - currentTimeMillis) < 0) {
    		  Toast.makeText(mContext, R.string.threshold_30_minutes, Toast.LENGTH_SHORT).show();
    		  return;
    	  }
    	  c.set(mReturnYear, mReturnMonth-1, mReturnDay, mReturnHour, mReturnMinute);
    	  long returnVehicleDate = c.getTimeInMillis();
          mSubmitOrder.mTakeVehicleDate = String.valueOf(takeVehicleDate);
          mSubmitOrder.mReturnVehicleDate = String.valueOf(returnVehicleDate);    	  
      }

      intent.putExtra(OrderCheckActivity.SUBMIT_ORDER_EXTRA, mSubmitOrder);
      try {
          startActivity(intent);
      } catch (ActivityNotFoundException e) {
          Log.e(TAG, "doActionEnterRegisterAccount->", e);
      }    	
    }
}