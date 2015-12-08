package com.twolinessoftware.data;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class DataManagerModule {

    private final Application mApplication;

    public DataManagerModule(Application application) {
        mApplication = application;
    }

    @Provides
    ApplicationDatabaseHelper provideDatabaseHelper() {
        return new ApplicationDatabaseHelper(mApplication);
    }

    @Provides
     SQLiteDatabase provideDatabase(ApplicationDatabaseHelper applicationDatabaseHelper) {
        return applicationDatabaseHelper.getWritableDatabase();
    }

}
