package com.twolinessoftware.notifications;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 *
 */
public abstract class GooglePlayService {

    private final Context mContext;

    public GooglePlayService(Context context){
        this.mContext = context;
    }

    protected boolean hasPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        return resultCode == ConnectionResult.SUCCESS;
    }


    public Context getContext() {
        return mContext;
    }
}



