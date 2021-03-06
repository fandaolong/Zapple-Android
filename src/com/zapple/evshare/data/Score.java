/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

import android.net.Uri;

/**
 * The Score contains data related to Score operation.
 */
public final class Score {
    private static final String TAG = "Score";
    private static final boolean DEBUG = true;
    private static final String AUTHORITY = "com.zapple.rental.provider.score";
    
    // Constructor
    public Score() {
    	
    }

    public String mDescription;
    public long mScoreValue;
    public long mDate;
    public long mExpiryDate;
    
	public interface ScoreColumns {
	    /**
	     * The unique ID for a row/vehicle.
	     * <P>Type: INTEGER (long)</P>
	     */
	    public static final String REMOTE_ID = "remote_id";
	    
        /**
         * The brand of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String DESCRIPTION = "description";

        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String SCORE_VALUE = "score_value";        
        
        /**
         * The rent of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String DATE = "date";
        
        /**
         * The remainder energy of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String EXPIRY_DATE = "expiry_date";               
	}

	public static final class ScoreTable implements BaseColumns, ScoreColumns {
		public static final String TABLE_NAME = "score";
		/**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);	
        
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "remote_id DESC";  
        
        
	}    
}
