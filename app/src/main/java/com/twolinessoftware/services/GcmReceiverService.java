package com.twolinessoftware.services;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.twolinessoftware.network.NetworkManager;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;

import timber.log.Timber;

/**
 *
 */
public class GcmReceiverService extends GcmListenerService {


    public enum MessageType {appointment, providerMessage};

    public static String MESSAGE_TYPE_APPOINTMENT_ID = "appointmentId";
    public static String MESSAGE_TYPE_EVENT_ID = "eventTypeId";
    public static String MESSAGE_TYPE_PROVIDER_ID = "providerId";
    public static String MESSAGE_TYPE_USER_ID = "userId";

    public static String MESSAGE_TYPE_PROVIDER_MESSAGE = "message";
    public static String MESSAGE_TYPE_PROVIDER_NAME = "title";


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Timber.v("Received GCM Message:" + from + " with Data:" + data.toString());

        boolean consumedMessage = false;

        if(data != null){

            MessageType messageType = MessageType.appointment;

            if(data.containsKey(MESSAGE_TYPE_PROVIDER_MESSAGE)){
                messageType = MessageType.providerMessage;
            }


            if(messageType == MessageType.providerMessage){

                try{

                    String providerName = data.getString(MESSAGE_TYPE_PROVIDER_NAME);
                    String message = data.getString(MESSAGE_TYPE_PROVIDER_MESSAGE);

                    if(message != null){
                        Timber.v("Sending Message Broadcast");
                        Intent intent = SyncNotificationsService.getBroadcastMessageIntent(providerName, message);
                        getApplication().sendOrderedBroadcast(intent,null);
                        consumedMessage = true;
                    }


                }catch(NumberFormatException ne){
                    Timber.e("Unable to process incoming message: NFE:"+ Log.getStackTraceString(ne));
                }
            }
        }

        if(!consumedMessage){
            // Default is to process the appointment messages regardless.
            Timber.v("Processing Appointment Push");

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
}
