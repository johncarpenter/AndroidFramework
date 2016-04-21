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

        if(!intent.getAction().equalsIgnoreCase(PUSH_INTENT)){
            Timber.e("Unknown action to broadcast receiver:aborting:"+intent.getAction());
            return;
        }

    }

}

