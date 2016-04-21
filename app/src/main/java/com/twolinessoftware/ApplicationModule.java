package com.twolinessoftware;


import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.UserManager;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.notifications.AnalyticsService;
import com.twolinessoftware.notifications.GCMService;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.notifications.SpatialService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

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
    NetworkManager provideNetworkManager(UserManager userManager, PreferencesHelper preferencesHelper, AuthenticationManager authenticationManager) {
        return new NetworkManager(userManager, preferencesHelper, authenticationManager);
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
    AuthenticationManager provideAuthenticationManager( AccountManager accountManager, PreferencesHelper preferencesHelper, UserManager userManager) {
        return new AuthenticationManager(mApplication,accountManager,preferencesHelper, userManager);
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }

    @Provides
    SharedPreferences provideSharedPreferences(){
        return mApplication.getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    PreferencesHelper providePreferencesHelper(SharedPreferences sharedPreferences){
        return new PreferencesHelper(sharedPreferences);
    }

}
