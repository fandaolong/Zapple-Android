/*
 * Copyright (C) 2012 Zapple, www.zapple.com.cn
 */

package com.zapple.evshare.data;

import android.net.Uri;

/**
 * The PilesSite contains data related to Piles Site operation.
 */
public final class PilesSite {
    private static final String TAG = "PilesSite";
    private static final boolean DEBUG = true;
    private static final String AUTHORITY = "com.zapple.rental.provider.piles_site";
    
    // Constructor
    public PilesSite() {
    	
    }

	public interface PilesSiteColumns {
	    /**
	     * The unique ID for a row/vehicle.
	     * <P>Type: INTEGER (long)</P>
	     */
	    public static final String REMOTE_ID = "remote_id";
	    
        /**
         * The brand of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * The model of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ADDRESS = "address";        
        
        /**
         * The rent of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String LONGITUDE = "longitude";
        
        /**
         * The remainder energy of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String LATITUDE = "latitude";
        
        /**
         * The parking garage of the vehicle.
         * <P>Type: TEXT</P>
         */
        public static final String ALTITUDE = "altitude";                    
	}

	public static final class PilesSiteTable implements BaseColumns, PilesSiteColumns {
		public static final String TABLE_NAME = "piles_site";
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