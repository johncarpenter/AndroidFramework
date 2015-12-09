package com.twolinessoftware.notifications;

import android.content.Context;

import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.network.BaseApiService;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class GoogleServicesModule {

    private final Context mContext;

    public GoogleServicesModule(Context context) {
        mContext = context;
    }

    @Provides
    GCMService provideGCMService(BaseApiService apiService, PreferencesHelper preferencesHelper) {
        return new GCMService(mContext,apiService, preferencesHelper);
    }

    @Provides
    SpatialService provideSpatialService() {
        return new SpatialService(mContext);
    }

    @Provides
    AnalyticsService provideAnalyticsService() { return  new AnalyticsService(mContext);}

}
