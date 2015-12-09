package com.twolinessoftware.services;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.twolinessoftware.BaseApplication;

import timber.log.Timber;

/**
 * This service pulls the latest dashboard information from the server,
 * and iterates through the appointments to ensure that the alarmmanager
 * has scheduled the notifications correctly.
 *
 * Sample execution is shown below
 *
 * OneoffTask toggleTask = new OneoffTask.Builder()
 *   .setService(SyncNotificationsService.class)
 *   .setExecutionWindow(0, 5) // execute now +/- 5 min
 *   .setExtras(bundle)        // Bundle fron NotificationUtils
 *   .setPersisted(false)      // Persist Task Across reboots
 *   .setRequiredNetwork(Task.NETWORK_STATE_ANY) // Requires network (yes)
 *   .setRequiresCharging(false)  // Requires charging (false)
 *   .build();
 *
 *
 */
public class SyncNotificationsService extends GcmTaskService {

    private static final String PERIODIC_UPDATE_TASK = "NetworkManagerDashboardSyncPeriodic";

    public SyncNotificationsService() {

    }

    public static void schedulePeriodicNotifications(Context context){

        long periodSecs = 60 * 60 * 24;     // Auto-update should be performed no more than once per 24 hours

        PeriodicTask dailyUpdate = new PeriodicTask.Builder()
                .setService(SyncNotificationsService.class)
                .setPeriod(periodSecs)
                .setTag(PERIODIC_UPDATE_TASK)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .setUpdateCurrent(true)
                .build();

        GcmNetworkManager.getInstance(context).schedule(dailyUpdate);
        Timber.d("Scheduled auto-update");
    }

    @Override
    public void onInitializeTasks() {
        schedulePeriodicNotifications(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        BaseApplication.get(getApplicationContext()).getComponent().inject(this);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        /**
         * Sync Data with the server
         */
        boolean isComplete = true;
        boolean isReschedule = false;


        if(isComplete && !isReschedule){
            return GcmNetworkManager.RESULT_SUCCESS;
        }else if(isComplete){
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }else{
            return GcmNetworkManager.RESULT_FAILURE;
        }

    }






}
