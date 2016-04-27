package com.twolinessoftware;

import android.content.Context;

import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.twolinessoftware.network.BaseApiService;
import com.twolinessoftware.network.BaseRetrofitHelper;

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
