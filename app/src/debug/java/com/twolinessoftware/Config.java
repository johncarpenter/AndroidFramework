/*
 * Copyright 2016 2LinesSoftware Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
