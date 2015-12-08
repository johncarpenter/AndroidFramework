/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.notifications;

import android.content.Context;

import javax.inject.Inject;

public class GoogleServicesManager {

    protected AnalyticsService mAnalyticsService;

    protected GCMService mGCMService;

    protected SpatialService mSpatialService;

    private Context mContext;

    @Inject
    public GoogleServicesManager(Context context, GCMService gcmService, SpatialService spatialService, AnalyticsService analyticsService) {

        this.mGCMService = gcmService;

        this.mSpatialService = spatialService;

        this.mContext = context;

        this.mAnalyticsService = analyticsService;

    }

    public GCMService getGCMService() {
        return mGCMService;
    }

    public SpatialService getSpatialService() {
        return mSpatialService;
    }


    public AnalyticsService getAnalyticsService() {
        return mAnalyticsService;
    }
}
