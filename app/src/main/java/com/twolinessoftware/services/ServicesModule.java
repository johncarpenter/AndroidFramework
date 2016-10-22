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

import com.twolinessoftware.PreferencesHelper;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

/**
 *
 */
@Module
public class ServicesModule {

    private final Context mContext;

    public ServicesModule(Context context) {
        mContext = context;
    }

    @Provides
    SpatialService providesSpatialService(PreferencesHelper preferencesHelper, EventBus eventBus) {
        return new SpatialService(mContext, preferencesHelper, eventBus);
    }

    @Provides
    AnalyticsService providesAnalyticsService() {
        return new AnalyticsService(mContext);
    }

}
