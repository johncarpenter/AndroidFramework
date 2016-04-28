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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.BaseApiService;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.services.AnalyticsService;
import com.twolinessoftware.services.GCMService;
import com.twolinessoftware.services.SpatialService;
import com.twolinessoftware.storage.DataStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import rx.Scheduler;

/**
 *
 */
@Module
public class MockApplicationModule {

    protected final Application mApplication;

    public MockApplicationModule(Application application) {
        mApplication = application;


    }

    @Provides
    @Singleton
    NetworkManager provideNetworkManager(BaseApiService baseApiService, Scheduler scheduler, EventBus eventBus, GoogleServicesManager googleServicesManager, DataManager dataManager) {
        return new NetworkManager(mApplication, baseApiService, scheduler, eventBus, googleServicesManager, dataManager);
    }


    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    GoogleServicesManager provideGoogleServicesManager(GCMService gcmService, SpatialService spatialService, AnalyticsService analyticsService) {
        return new GoogleServicesManager(mApplication, gcmService, spatialService, analyticsService);
    }

    @Provides
    @Singleton
    DataManager provideDataManager(SQLiteDatabase database) {
        return new DataManager(mApplication, database);
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        // @todo inject the eventbus builder here if needed
        return EventBus.getDefault();
    }

    @Provides
    SharedPreferences provideSharedPrefs(Application app) {
        return app.getSharedPreferences(DataStore.PREFS_NAME, Context.MODE_PRIVATE);
    }


}
