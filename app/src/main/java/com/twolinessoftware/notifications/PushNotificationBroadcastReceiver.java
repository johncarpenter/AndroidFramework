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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

/**
 *
 */
public class PushNotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String PUSH_INTENT = "com.twolinessoftware.PUSH_MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("Received Push Notification and Sending Android Notifications");

        if ( !intent.getAction().equalsIgnoreCase(PUSH_INTENT) ) {
            Timber.e("Unknown action to broadcast receiver:aborting:" + intent.getAction());
            return;
        }

    }

}

