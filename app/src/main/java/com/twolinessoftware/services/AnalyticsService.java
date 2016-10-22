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

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.twolinessoftware.BuildConfig;
import com.twolinessoftware.R;

import javax.inject.Inject;

/**
 * Placeholder for Analytics
 */
public class AnalyticsService {


    private Context mContext;

    @Inject
    public AnalyticsService(Context context) {
        this.mContext = context;
        getDefaultTracker();
    }

    private Tracker m_tracker;

    synchronized public Tracker getDefaultTracker() {
        if (m_tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);

            if (BuildConfig.DEBUG) {
                analytics.setLocalDispatchPeriod(15);
            }

            m_tracker = analytics.newTracker(R.xml.analytics);

        }
        return m_tracker;
    }


    public void trackActivity(String screenName) {
        Tracker tracker = getDefaultTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void trackEvent(String category, String action) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    public void trackError(String error, boolean fatal) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(error)
                .setFatal(fatal)
                .build());
    }

    public void trackDefaultEvent(String category, String action, long value) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setValue(value)
                .build());
    }


    public void trackDefaultEvent(String category, String action, String label) {
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

}
