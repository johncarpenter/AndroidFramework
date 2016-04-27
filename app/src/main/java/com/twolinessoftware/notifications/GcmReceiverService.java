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

package com.twolinessoftware.notifications;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.twolinessoftware.services.SyncNotificationsService;

import timber.log.Timber;

/**
 *
 */
public class GcmReceiverService extends GcmListenerService {

    private static final String ONEOFFTAG = "Sync_Service_Once";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Timber.v("Received GCM Message:" + from + " with Data:" + data.toString());

        OneoffTask toggleTask = new OneoffTask.Builder()
                .setService(SyncNotificationsService.class)
                .setTag(GcmReceiverService.ONEOFFTAG)
                .setExecutionWindow(1, 30) // execute now +/- 30 s
                .setPersisted(false)      // Persist Task Across reboots
                .setRequiredNetwork(Task.NETWORK_STATE_ANY) // Requires network (yes)
                .setExtras(data)
                .build();
        GcmNetworkManager.getInstance(this).schedule(toggleTask);

    }
}
