/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.service.location;

import android.content.Context;

public class LocationTaskParas {
    public Context mContext;
    public long mPeriod;
    public boolean mAutoUpload;

    public LocationTaskParas(Context context, long period, boolean auto) {
        mContext = context;
        mPeriod = period;
        mAutoUpload = auto;
    }

    public LocationTaskParas() {

    }

    public boolean equals(LocationTaskParas paras) {
        if (mContext != paras.mContext || mPeriod != paras.mPeriod || mAutoUpload != paras.mAutoUpload) {
            return false;
        }
        return true;
    }

}
