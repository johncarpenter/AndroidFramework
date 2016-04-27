package com.twolinessoftware;

import retrofit.RestAdapter;

/**
 *
 */
public class Config {

    /**
     * REST API calls
     */
    public static final String URL_ENDPOINT = "http://dev";
    public static final RestAdapter.LogLevel RETROFIT_LOGLEVEL = RestAdapter.LogLevel.FULL;

    // Shared Preferences FIle
    public static final String SHARED_PREFERENCES_FILE = "com.twolinessoftware.SHARED_PREFERENCES";


    /**
     * Database details
     */
    public static final String FIREBASE_URL = "https://radiant-torch-1344.firebaseio.com/";


    /**
     * Push notifications
     */
    public static final String GCM_SENDER_ID = "";

    /**
     * GPS Location Settings
     */
    public static final float GPS_SMALLEST_DISPLACEMENT_IN_M = 15;
    public static final long GPS_UPDATE_INTERVAL_IN_SEC = 2;
    public static final float GPS_MIN_ACCURACY_IN_M = 150;


    /**
     * Account Names
     */
    public static final String BASE_ACCOUNT_TYPE = "com.twolinessoftware";
    public static final String BASE_TOKEN_TYPE = "api";

}
