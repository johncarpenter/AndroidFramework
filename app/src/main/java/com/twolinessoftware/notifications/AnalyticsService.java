package com.twolinessoftware.notifications;

import android.content.Context;

import com.twolinessoftware.BuildConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Placeholder for Analytics
 */
public class AnalyticsService extends GooglePlayService {

    public static final String CATEGORY_NOTIFICATION = "Notification";

    public AnalyticsService(Context context) {
        super(context);
        getDefaultTracker();
    }

    private Tracker m_tracker;

    synchronized public Tracker getDefaultTracker() {
        if (m_tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(getContext());

            if(BuildConfig.DEBUG){
                analytics.setLocalDispatchPeriod(15);
            }

            m_tracker = analytics.newTracker(com.twolinessoftware.R.xml.analytics);

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

}
