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

package com.twolinessoftware.network;

import android.content.Context;

import com.google.gson.Gson;
import com.twolinessoftware.BuildConfig;
import com.twolinessoftware.utils.GsonUtil;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.CacheControl;
import okhttp3.logging.HttpLoggingInterceptor;
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
     *
     * @return
     */

    @Provides
    Scheduler provideSubscribeScheduler() {
        return Schedulers.io();
    }

    @Provides
    CacheControl provideCache() {
        return new CacheControl.Builder().maxAge(4, TimeUnit.HOURS).build();
    }

    @Provides
    HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }

    @Provides
    Gson providesGson() {
        return GsonUtil.buildGsonAdapter();
    }

}
