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

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.karumi.dexter.Dexter;
import com.tsengvn.typekit.Typekit;
import com.twolinessoftware.authentication.AuthenticationModule;
import com.twolinessoftware.data.ApplicationDatabaseHelper;
import com.twolinessoftware.data.DataManagerModule;
import com.twolinessoftware.network.NetworkModule;
import com.twolinessoftware.services.ServicesModule;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import de.greenrobot.event.EventBus;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;
import timber.log.Timber;

public class BaseApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .networkModule(new NetworkModule(this))
                .dataManagerModule(new DataManagerModule(this))
                .authenticationModule(new AuthenticationModule(this))
                .servicesModule(new ServicesModule(this))
                .build();

        initializeDatabase();

        initializeLogging();

        initializeLibraries();

        initializeFonts();

        initializeIcons();
    }

    private void initializeIcons() {
        Iconify.with(new MaterialModule());
    }

    private void initializeLibraries() {
        JodaTimeAndroid.init(this);

        Dexter.initialize(this);
    }

    private void initializeDatabase() {

        // Forces Cupboard to use annotations globally
        CupboardFactory.setCupboard(new CupboardBuilder()
                .useAnnotations()
                .registerFieldConverter(DateTime.class, new ApplicationDatabaseHelper.JodaTimeConverter())
                //  .registerFieldConverter(ComplexClass.class, new GsonFieldConverter<>(GsonUtil.buildGsonAdapter(), ComplexClass.class))
                .build());

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private void initializeFonts() {
        // Add Custom Fonts into the assets directory
        Typekit.getInstance();
        //.addNormal(Typekit.createFromAsset(this, "AmsiPro-SemiBold.otf"))
        //.addBold(Typekit.createFromAsset(this, "AmsiPro-Bold.otf"))
        //.addItalic(Typekit.createFromAsset(this, "AmsiPro-Light.otf"))
        //.addBoldItalic(Typekit.createFromAsset(this, "AmsiPro-Bold.otf"));

    }

    private void initializeLogging() {

        if ( BuildConfig.DEBUG ) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ErrorReportingTree());
        }

        EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();

    }


    public static BaseApplication get(Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }

    /**
     * Extracts the Timber.e logs and forwards them to the analytics tool for tracking
     */
    private class ErrorReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            // Direct Production Logs If Necessary
        }
    }

}
