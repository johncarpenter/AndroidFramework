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

package com.twolinessoftware.services;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Inject;

/**
 * Placeholder for Analytics
 */
public class AnalyticsService {

    private FirebaseAnalytics mTracker;
    private Context mContext;

    @Inject
    public AnalyticsService(Context context) {
        this.mContext = context;
        mTracker = FirebaseAnalytics.getInstance(context);
    }

    public void trackSample(String name) {

        Bundle bundle = new Bundle();
        bundle.putString("EXTRA", name);
        mTracker.logEvent("SAMPLE", bundle);

    }

}
