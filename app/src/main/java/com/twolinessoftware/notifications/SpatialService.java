package com.twolinessoftware.notifications;

import android.content.Context;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

/**
 * Placeholder for location services and geofencing.
 */
public class SpatialService extends GooglePlayService {

    private final ReactiveLocationProvider mLocationProvider;

    public SpatialService(Context context) {
        super(context);
        mLocationProvider = new ReactiveLocationProvider(context);
    }

    public void addGeofence(double lat, double lng, double radiusInMeters){

        //GeofencingRequest request = new GeofencingRequest();


        //mLocationProvider.addGeofences()

    }


}
