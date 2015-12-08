package com.twolinessoftware.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.twolinessoftware.activities.MainActivity;
import com.twolinessoftware.activities.RatingActivity;
import com.twolinessoftware.services.BackgroundActionService;
import com.twolinessoftware.utils.NotificationUtil;

import timber.log.Timber;

/**
 *
 */
public class PushNotificationBroadcastReceiver extends BroadcastReceiver {
    public static final String PUSH_INTENT = "com.appreciado.PUSH_MESSAGE";

    public static final String EXTRA_MESSAGE_TYPE = "MESSAGE_TYPE";
    public static final String EXTRA_APPOINTMENT = "APPOINTMENT";
    public static final String EXTRA_USER = "USER";
    public static final String EXTRA_DOCTOR = "DOCTOR";
    // For fixed messages
    public static final String EXTRA_MESSAGE = "MESSAGE";
    public static final String EXTRA_TITLE = "TITLE";


    private NotificationManager mNotificationManager;


    public enum MessageType{
        upcomingAppointment,
        checkIn,
        ratingRequest,
        confirmAppointment,
        broadcast
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("Received Push Notification and Sending Android Notifications");

        if(!intent.getAction().equalsIgnoreCase(PUSH_INTENT)){
            Timber.e("Unknown action to broadcast receiver:aborting:"+intent.getAction());
            return;
        }

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        MessageType messageType = (MessageType) intent.getSerializableExtra(EXTRA_MESSAGE_TYPE);

        Appointment appointment = intent.getParcelableExtra(EXTRA_APPOINTMENT);

        Doctor doctor = intent.getParcelableExtra(EXTRA_DOCTOR);

        User user = intent.getParcelableExtra(EXTRA_USER);

        switch(messageType){
            case upcomingAppointment:
                showUpcomingAppointmentNotification(context, appointment, doctor, user);
                break;
            case checkIn:
                showCheckInNotification(context,appointment,doctor,user);
                break;
            case ratingRequest:
                showRatingNotification(context,appointment,doctor,user);
                break;
            case confirmAppointment:
                showNewAppointmentNotification(context,appointment,doctor,user);
                break;
            case broadcast:
                String title = intent.getStringExtra(EXTRA_TITLE);
                String message = intent.getStringExtra(EXTRA_MESSAGE);
                showBroadcastNotification(context,title,message);
                break;
        }
    }

    private void showNewAppointmentNotification(Context context, Appointment appointment, Doctor doctor, User user) {

        Timber.v("Showing New Appointment Notification for Appoint:" + appointment.getId());


        // Confirm Pending Intent
        Intent intent = new Intent(context.getApplicationContext(), BackgroundActionService.class);
        intent.putExtra(BackgroundActionService.EXTRA_ACTION,BackgroundActionService.Action.ApproveAppointment);
        intent.putExtra(BackgroundActionService.EXTRA_APPOINTMENT,appointment);
        intent.putExtra(BackgroundActionService.EXTRA_DOCTOR,doctor);
        intent.putExtra(BackgroundActionService.EXTRA_USER, user);
        PendingIntent confirmPendingIntent = PendingIntent.getService(context, (int) appointment.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)

                .setSmallIcon(com.twolinessoftware.R.drawable.logo)
                .setContentTitle(context.getString(com.twolinessoftware.R.string.notification_confirm_title))
                .setContentText(NotificationUtil.getAppointmentString(context, appointment, user, doctor))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(NotificationUtil.getApprovalAppointmentString(context, appointment, user, doctor)))
             //   .addAction(0, context.getString(R.string.notification_reschedule_button), rescheduleAppIntent)
                .addAction(0, context.getString(com.twolinessoftware.R.string.notification_approve_button), confirmPendingIntent);


        showNotification(MessageType.upcomingAppointment,(int)appointment.getId(),mBuilder.build());
    }


    private void showBroadcastNotification(Context context, String title, String message) {

        Timber.v("Showing Broadcast Notification "+title+":"+message);

        // Launch App Intent
        Intent launchIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        PendingIntent launchAppIntent = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(com.twolinessoftware.R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(launchAppIntent);


        showNotification(MessageType.broadcast,(int)System.currentTimeMillis(),mBuilder.build());
    }

    private void showUpcomingAppointmentNotification(Context context, Appointment appointment, Doctor doctor, User user) {

        Timber.v("Showing Appointment Notification for Appoint:" + appointment.getId());

        // Launch App Intent, Show reschedule
        Intent rescheduleIntent = new Intent(context.getApplicationContext(), MainActivity.class);
        rescheduleIntent.putExtra(MainActivity.EXTRA_APPOINTMENT, appointment);
        rescheduleIntent.putExtra(MainActivity.EXTRA_LAUNCH_ACTION, MainActivity.LaunchAction.RESCHEDULE);
        PendingIntent rescheduleAppIntent = PendingIntent.getActivity(context,(int)appointment.getId(),rescheduleIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        // Confirm Pending Intent
        Intent intent = new Intent(context.getApplicationContext(), BackgroundActionService.class);
        intent.putExtra(BackgroundActionService.EXTRA_ACTION,BackgroundActionService.Action.ConfirmAppointment);
        intent.putExtra(BackgroundActionService.EXTRA_APPOINTMENT,appointment);
        intent.putExtra(BackgroundActionService.EXTRA_DOCTOR,doctor);
        intent.putExtra(BackgroundActionService.EXTRA_USER, user);
        PendingIntent confirmPendingIntent = PendingIntent.getService(context, (int) appointment.getId(),intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)

                        .setSmallIcon(com.twolinessoftware.R.drawable.logo)
                        .setContentTitle(context.getString(com.twolinessoftware.R.string.notification_confirm_title))
                        .setContentText(NotificationUtil.getAppointmentString(context, appointment, user, doctor))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(NotificationUtil.getAppointmentString(context, appointment, user, doctor)))
                        .addAction(0, context.getString(com.twolinessoftware.R.string.notification_reschedule_button), rescheduleAppIntent)
                        .addAction(0, context.getString(com.twolinessoftware.R.string.notification_confirm_button), confirmPendingIntent);


        showNotification(MessageType.upcomingAppointment,(int)appointment.getId(),mBuilder.build());
    }


    private void showCheckInNotification(Context context, Appointment appointment, Doctor doctor, User user) {

        Timber.v("Showing CheckIn Notification for Appoint:"+appointment.getId());

        // CheckIn Pending Intent
        Intent intent = new Intent(context.getApplicationContext(), BackgroundActionService.class);
        intent.putExtra(BackgroundActionService.EXTRA_ACTION,BackgroundActionService.Action.CheckIn);
        intent.putExtra(BackgroundActionService.EXTRA_APPOINTMENT,appointment);
        intent.putExtra(BackgroundActionService.EXTRA_DOCTOR,doctor);
        intent.putExtra(BackgroundActionService.EXTRA_USER, user);
        PendingIntent checkInPendingIntent = PendingIntent.getService(context, (int) appointment.getId(),intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(com.twolinessoftware.R.drawable.logo)
                .setContentTitle(context.getString(com.twolinessoftware.R.string.notification_checkin_title))
                .setContentText(context.getString(com.twolinessoftware.R.string.notification_checkin_message))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(NotificationUtil.getAppointmentString(context, appointment, user, doctor)))
                .addAction(android.R.drawable.ic_menu_mylocation, context.getString(com.twolinessoftware.R.string.notification_checkin_button), checkInPendingIntent);


        showNotification(MessageType.checkIn,(int)appointment.getId(),mBuilder.build());
    }

    private void showRatingNotification(Context context, Appointment appointment, Doctor doctor, User user) {

        Timber.v("Showing Rating Notification for Appoint:" + appointment.getId());

        // Launch App Intent
        Intent rateIntent = new Intent(context.getApplicationContext(), RatingActivity.class);
        rateIntent.putExtra(RatingActivity.EXTRA_APPOINTMENT, appointment);

        PendingIntent ratingIntent = PendingIntent.getActivity(context,(int)appointment.getId(),rateIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Cancel Pending Intent
        Intent intent = new Intent(context.getApplicationContext(), BackgroundActionService.class);
        intent.putExtra(BackgroundActionService.EXTRA_ACTION,BackgroundActionService.Action.CancelRating);
        intent.putExtra(BackgroundActionService.EXTRA_APPOINTMENT,appointment);
        intent.putExtra(BackgroundActionService.EXTRA_DOCTOR,doctor);
        intent.putExtra(BackgroundActionService.EXTRA_USER,user);
        PendingIntent cancelPendingIntent = PendingIntent.getService(context, (int) appointment.getId(),intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(com.twolinessoftware.R.drawable.logo)
                .setContentTitle(context.getString(com.twolinessoftware.R.string.notification_rating_title))
                .setContentText(NotificationUtil.getRatingString(context, appointment, user, doctor))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(NotificationUtil.getRatingString(context, appointment, user, doctor)))
                .addAction(0, context.getString(com.twolinessoftware.R.string.notification_rating_button_no), cancelPendingIntent)
                .addAction(0, context.getString(com.twolinessoftware.R.string.notification_rating_button_yes), ratingIntent);


        showNotification(MessageType.ratingRequest, (int) appointment.getId(),mBuilder.build());
    }


    private void showNotification(MessageType messageType, int id,  Notification notification) {
        mNotificationManager.notify(id,notification);
    }




}

