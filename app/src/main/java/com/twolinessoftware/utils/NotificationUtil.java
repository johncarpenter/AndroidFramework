package com.twolinessoftware.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseActivity;
import com.twolinessoftware.activities.RatingActivity;
import com.twolinessoftware.data.DataManager;

import net.danlew.android.joda.DateUtils;

import java.util.List;

import timber.log.Timber;

/**
 *
 */
public class NotificationUtil {



    private static DataManager getDataManager(BaseActivity activity) {
        return activity.getDataManager();
    }

    public static void showGenericOkDialog(final BaseActivity activity,String title, String message) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(com.twolinessoftware.R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }

    public static void showErrorDialog(final BaseActivity activity,String message) {

        new AlertDialog.Builder(this)
                .setTitle(com.twolinessoftware.R.string.dialog_error)
                .setMessage(message)
                .setPositiveButton(com.twolinessoftware.R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }


    public static void showRatingDialog(final BaseActivity activity, final Appointment appointment) {

        Timber.v("Showing Rating Dialog for Appoint:" + appointment.getId());



        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_rating_title)
                .setMessage(getRatingString(activity, appointment, getDataManager(activity).getUser(appointment.getPatientId()), getDataManager(activity).getDoctor(appointment.getDoctorId())))
                .setPositiveButton(R.string.notification_rating_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       // show rating page
                        Intent intent = new Intent(activity, RatingActivity.class);
                        intent.putExtra(RatingActivity.EXTRA_APPOINTMENT,appointment);
                        activity.startActivity(intent);

                    }
                })
                .setNegativeButton(R.string.notification_rating_button_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseApplication.get(activity).getComponent().networkManager().rateAppointment(appointment, -1, null, false, false);
                        dialog.dismiss();
                    }
                })

                .show();
    }


    public static void showCheckInAppointmentDialog(final BaseActivity activity, final Appointment appointment) {

        Timber.v("Showing Check-in Dialog for Appoint:" + appointment.getId());
        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_checkin_title)
                .setMessage(R.string.notification_checkin_message)
                .setPositiveButton(R.string.notification_checkin_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseApplication.get(activity).getComponent().networkManager().checkin(appointment);
                    }
                })
                .show();
    }


    public static void showAcceptAppointmentDialog(final BaseActivity activity, final Appointment appointment) {

        Timber.v("Showing Accept Appointment Dialog for Appoint:" + appointment.getId());

        User user = getDataManager(activity).getUser(appointment.getPatientId());
        List<User> users = getDataManager(activity).getUsers();
        List<Appointment> appts = getDataManager(activity).getAppointments();

        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_confirm_title)
                .setMessage(getApprovalAppointmentString(activity, appointment, getDataManager(activity).getUser(appointment.getPatientId()), getDataManager(activity).getDoctor(appointment.getDoctorId())))
                .setPositiveButton(R.string.notification_approve_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseApplication.get(activity).getComponent().networkManager().acceptAppointment(appointment);
                    }
                })
                .show();
    }

    public static void showConfirmAppointmentDialog(final BaseActivity activity, final Appointment appointment) {

        Timber.v("Showing Appointment Dialog for Appoint:" + appointment.getId());

        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_confirm_title)
                .setMessage(getAppointmentString(activity, appointment, getDataManager(activity).getUser(appointment.getPatientId()), getDataManager(activity).getDoctor(appointment.getDoctorId())))
                .setPositiveButton(R.string.notification_confirm_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseApplication.get(activity).getComponent().networkManager().confirmAppointment(appointment);
                    }
                })
                .setNegativeButton(R.string.notification_reschedule_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotificationUtil.showRescheduleAppointmentDialog(activity, appointment);
                    }
                })

                .show();
    }

    public static void showRescheduleAppointmentDialog(final BaseActivity activity, final Appointment appointment) {

        Timber.v("Showing Reschedule Appointment Dialog");

        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_reschedule_title)
                .setMessage(R.string.notification_reschedule_message)
                .setPositiveButton(R.string.notification_call_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeviceUtils.placeCall(getDataManager(activity).getDoctor(appointment.getDoctorId()).getPhone());
                    }
                })
                .setNegativeButton(R.string.notification_cancel_button, null)
                .show();
    }



    public static void showBroadcastDialog(Context context, String title, String message) {
        Timber.v("Showing Broadcast Dialog:"+message);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.notification_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public static String getAppointmentString(Context context, @NonNull Appointment appointment, @NonNull User user, @NonNull Doctor doctor) {
        return context.getString(R.string.notification_confirm_message, user.getFirstName(), doctor.getFullName(),
                DateUtils.formatDateTime(context, appointment.getDateTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR),
                DateUtils.formatDateTime(context, appointment.getDateTime(), DateUtils.FORMAT_SHOW_TIME));
    }

    public static String getApprovalAppointmentString(Context context, @NonNull Appointment appointment, @NonNull User user, @NonNull Doctor doctor) {
        return context.getString(R.string.notification_approve_message, user.getFirstName(), doctor.getFullName(),
                DateUtils.formatDateTime(context, appointment.getDateTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR),
                DateUtils.formatDateTime(context, appointment.getDateTime(), DateUtils.FORMAT_SHOW_TIME));
    }


    public static String getRatingString(Context context, @NonNull Appointment appointment, @NonNull User user, @NonNull Doctor doctor) {
        return context.getString(R.string.notification_rating_message, doctor.getFullName(),
                DateUtils.formatDateTime(context, appointment.getDateTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
    }


    public static void cancelNotificationsForAppointment(Context context, @NonNull Appointment appointment){
        Timber.v("Cancelling Notification:"+appointment.getId());

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel((int)appointment.getId());


    }

}
