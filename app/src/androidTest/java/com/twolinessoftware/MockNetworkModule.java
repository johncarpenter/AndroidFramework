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

import android.content.Context;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.twolinessoftware.network.BaseApiService;

import java.io.IOException;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.fail;

/**
 *
 */
@Module
public class MockNetworkModule {

    private final Context mContext;
    private final MockWebServer mMockWebServer;

    public MockNetworkModule(Context context) {
        mContext = context;

        mMockWebServer = new MockWebServer();
        try {
            mMockWebServer.start();
        } catch (IOException e) {
            fail("Unable to start mock server:" + e.getMessage());
        }

    }

    @Provides
    BaseApiService provideAppreciadoApiService() {
        return new BaseRetrofitHelper().newAppreciadoApiService(mMockWebServer.url("/").toString());
    }


    @Provides
    Scheduler provideSubscribeScheduler() {
        return Schedulers.io();
    }

    @Provides
    MockWebServer providerMockWebServer() {
        return mMockWebServer;
    }


}
