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
import android.location.Location;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.twolinessoftware.Config;
import com.twolinessoftware.PreferencesHelper;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import timber.log.Timber;

/**
 * Placeholder for location services and geofencing.
 */
public class SpatialService {

    private EventBus mEventBus;
    private PreferencesHelper mPreferencesHelper;
    private ReactiveLocationProvider mLocationProvider;
    private Context mContext;


    @Inject
    public SpatialService(Context context, PreferencesHelper preferencesHelper, EventBus eventBus) {
        this.mContext = context;
        this.mPreferencesHelper = preferencesHelper;
        this.mEventBus = eventBus;
        mLocationProvider = new ReactiveLocationProvider(context);

    }

    public Observable<Location> getLastLocationOrThrow() {
        return mLocationProvider.getLastKnownLocation().toSingle().toObservable();

    }


    public Observable<Location> getLastLocation() {
        return hasLocationServicesEnabled()
                .flatMap(enabled -> mLocationProvider.getLastKnownLocation());
    }

    private Observable<Boolean> hasLocationServicesEnabled() {

        LocationRequest request = getDefaultLocationRequest();

        return mLocationProvider.checkLocationSettings(new LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true).build())
                .flatMap(locationSettingsResult -> {
                    Status status = locationSettingsResult.getStatus();
                    if (status.getStatusCode() != LocationSettingsStatusCodes.SUCCESS) {
                        Timber.v("hasLocationServicesEnabled:false:" + status.getStatusMessage());
                        return Observable.error(new LocationServicesDisabledException(status));
                    } else {
                        Timber.v("hasLocationServicesEnabled:true");
                        return Observable.just(true);
                    }
                });
    }

    private LocationRequest getDefaultLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(Config.GPS_UPDATE_INTERVAL_IN_SEC);
    }

    public static class LocationServicesDisabledException extends Throwable {
        private final Status mStatus;

        public LocationServicesDisabledException(Status status) {
            this.mStatus = status;
        }

        public Status getStatus() {
            return mStatus;
        }
    }

    public static class ReverseGeocodeException extends Throwable {
    }

}
