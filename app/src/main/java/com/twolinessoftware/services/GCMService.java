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
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.twolinessoftware.Config;
import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.utils.AndroidUtils;

import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 *
 */
public class GCMService {

    private EventBus mEventBus;
    private PreferencesHelper mPreferencesHelper;

    private Context mContext;

    private GoogleCloudMessaging mGoogleCloudMessaging;

    private String mRegistrationId;


    @Inject
    public GCMService(Context context, PreferencesHelper preferencesHelper, EventBus eventBus) {
        this.mContext = context;
        mPreferencesHelper = preferencesHelper;
        mEventBus = eventBus;
    }


    public void register() {
        // Don't bother registering GCM unless an account is active

        mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(mContext);

        mRegistrationId = getRegistrationId();

        if ( TextUtils.isEmpty(mRegistrationId) ) {
            registerGcm();
        }

    }

    private void registerGcm() {

        if ( mGoogleCloudMessaging == null ) {
            Timber.v("Google Play Services are not enabled for this device");
            return;
        }

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {

                    InstanceID instanceID = InstanceID.getInstance(mContext);
                    mRegistrationId = instanceID.getToken(Config.GCM_SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(mRegistrationId);
                subscriber.onCompleted();
            }
        }).map(s -> {
            Timber.v("Storing Registration Id");
            storeRegistrationId(mRegistrationId);
            return true;
        }).subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to update GCM: Cause:" + e.getMessage());
                        mEventBus.post(new OnGcmUpdatedEvent(false).setError(e));
                    }

                    @Override
                    public void onNext(Boolean r) {
                        Timber.v("GCM Updated");
                        mEventBus.post(new OnGcmUpdatedEvent(true));
                    }
                });


    }

    private void storeRegistrationId(String regId) {

        int appVersion = AndroidUtils.getAppVersion(mContext);

        mPreferencesHelper.storeGcmRegistration(regId, appVersion);

    }

    private String getRegistrationId() {

        String registrationId = mPreferencesHelper.getGcmRegistration();

        if ( TextUtils.isEmpty(registrationId) ) {
            Timber.v("Registration not found.");
            return null;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = mPreferencesHelper.getGcmRegistrationVersion();

        int currentVersion = AndroidUtils.getAppVersion(mContext);

        if ( registeredVersion != currentVersion ) {
            Timber.v("App version changed. Requesting new GCM Update");
            return null;
        }
        return registrationId;
    }

    public static class OnGcmUpdatedEvent {
        public final boolean success;
        private Throwable mError;

        public OnGcmUpdatedEvent(boolean success) {
            this.success = success;
        }

        public Throwable getError() {
            return mError;
        }

        public OnGcmUpdatedEvent setError(Throwable e) {
            this.mError = e;
            return this;
        }
    }

}
