package com.twolinessoftware.services;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.twolinessoftware.network.NetworkManager;

import timber.log.Timber;

/**
 *
 */
public class GcmReceiverService extends GcmListenerService {

    public static String MESSAGE_TYPE_PROVIDER_MESSAGE = "message";
    public static String MESSAGE_TYPE_PROVIDER_NAME = "title";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Timber.v("Received GCM Message:" + from + " with Data:" + data.toString());



            OneoffTask toggleTask = new OneoffTask.Builder()
                    .setService(SyncNotificationsService.class)
                    .setTag(NetworkManager.ONEOFFTAG)
                    .setExecutionWindow(1, 30) // execute now +/- 5 s
                    .setPersisted(false)      // Persist Task Across reboots
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY) // Requires network (yes)
                    .build();
            GcmNetworkManager.getInstance(this).schedule(toggleTask);


    }
}
