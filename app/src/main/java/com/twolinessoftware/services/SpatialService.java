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
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
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

    public Observable<Location> getLastLocation() {

        return mLocationProvider.getLastKnownLocation();
    }

    public Observable<Location> startLocationUpdates() {
        LocationRequest request = getDefaultLocationRequest();

        return checkGoogleLocationServices(request)
                .flatMap(locationSettingsResult -> mLocationProvider.getUpdatedLocation(request))
                .doOnNext(location1 -> Timber.v("Raw Location:" + location1.toString() + " acc:" + location1.getAccuracy()))
                .filter(location -> location.hasAccuracy() && location.getAccuracy() < Config.GPS_MIN_ACCURACY_IN_M)
                .doOnError(throwable -> {
                    Timber.e("Location services error:" + Log.getStackTraceString(throwable));
                    mEventBus.post(new OnLocationServicesDisabledEvent(null));
                });

    }

    public Observable<Boolean> hasLocationServicesEnabled() {

        LocationRequest request = getDefaultLocationRequest();

        return mLocationProvider.checkLocationSettings(new LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true).build())
                .map(locationSettingsResult -> {
                    Status status = locationSettingsResult.getStatus();
                    if ( status.getStatusCode() != LocationSettingsStatusCodes.SUCCESS ) {
                        Timber.v("hasLocationServicesEnabled:false:" + status.getStatusMessage());
                        return false;
                    } else {
                        Timber.v("hasLocationServicesEnabled:true");
                        return true;
                    }
                });
    }

    private LocationRequest getDefaultLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setSmallestDisplacement(Config.GPS_SMALLEST_DISPLACEMENT_IN_M)
                .setInterval(Config.GPS_UPDATE_INTERVAL_IN_SEC);
    }


    private Observable<LocationSettingsResult> checkGoogleLocationServices(LocationRequest request) {

        return mLocationProvider.checkLocationSettings(new LocationSettingsRequest.Builder().addLocationRequest(request).setAlwaysShow(true).build())
                .doOnNext(locationSettingsResult -> {
                    Status status = locationSettingsResult.getStatus();
                    if ( status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED || status.getStatusCode() == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ) {
                        Timber.v("checkGoogleLocationServices:false:" + status.getStatusMessage());
                        mEventBus.post(new OnLocationServicesDisabledEvent(status));
                    }
                })
                .filter(locationSettingsResult1 -> locationSettingsResult1.getStatus().getStatusCode() != LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
    }

    public static class OnLocationServicesDisabledEvent {
        private final Status mStatus;

        public OnLocationServicesDisabledEvent(Status status) {
            this.mStatus = status;
        }

        public Status getStatus() {
            return mStatus;
        }
    }
}
