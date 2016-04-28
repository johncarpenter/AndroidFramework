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


import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.UserManager;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.NetworkManager;

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
    DataManager provideDataManager(SQLiteDatabase database) {
        return new DataManager(mApplication, database);
    }

    @Provides
    @Singleton
    AuthenticationManager provideAuthenticationManager(AccountManager accountManager, PreferencesHelper preferencesHelper, UserManager userManager) {
        return new AuthenticationManager(mApplication, accountManager, preferencesHelper, userManager);
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }

    @Provides
    SharedPreferences provideSharedPreferences() {
        return mApplication.getSharedPreferences(Config.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    @Singleton
    @Provides
    PreferencesHelper providePreferencesHelper(SharedPreferences sharedPreferences) {
        return new PreferencesHelper(sharedPreferences);
    }

}
