package com.twolinessoftware.network;

import android.content.Context;

import com.twolinessoftware.Config;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 *
 */
@Module
public class NetworkModule {

    private final Context mContext;

    public NetworkModule(Context context) {
        mContext = context;
    }

    /**
     * API Services are defined here
     * @return
     */
    @Provides
    BaseApiService provideBaseApiService() {
        return new BaseRetrofitHelper().newBaseApiService(Config.URL_ENDPOINT);
    }

    @Provides
    Scheduler provideSubscribeScheduler() {
        return Schedulers.io();
    }

}
