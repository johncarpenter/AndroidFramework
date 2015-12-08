package com.twolinessoftware;


import android.accounts.AccountManager;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.BaseApiService;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.notifications.AnalyticsService;
import com.twolinessoftware.notifications.GCMService;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.notifications.SpatialService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import rx.Scheduler;

@Module
public class ApplicationModule {

    protected final Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    NetworkManager provideNetworkManager( BaseApiService baseApiService, Scheduler scheduler, EventBus eventBus, GoogleServicesManager googleServicesManager, DataManager dataManager) {
        return new NetworkManager( mApplication, baseApiService,  scheduler,  eventBus, googleServicesManager, dataManager);
    }

    @Provides
    @Singleton
    GoogleServicesManager provideGoogleServicesManager( GCMService gcmService, SpatialService spatialService, AnalyticsService analyticsService) {
        return new GoogleServicesManager(mApplication, gcmService, spatialService, analyticsService);
    }

    @Provides
    @Singleton
    DataManager provideDataManager( SQLiteDatabase database) {
        return new DataManager(mApplication, database);
    }

    @Provides
    @Singleton
    AuthenticationManager provideAuthneticationManager( AccountManager accountManager) {
        return new AuthenticationManager(mApplication,accountManager);
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        // @todo inject the eventbus builder here if needed
        return EventBus.getDefault();
    }

}
