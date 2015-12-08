package com.twolinessoftware.services;

import com.twolinessoftware.BaseApplication;
import com.google.android.gms.iid.InstanceIDListenerService;

import timber.log.Timber;

/**
 *
 */
public class RefreshIDListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        Timber.v("GCM Token Invalidated:Refreshing");
        BaseApplication.get(this).getComponent().googleServicesManager().getGCMService().register();

    }
}
