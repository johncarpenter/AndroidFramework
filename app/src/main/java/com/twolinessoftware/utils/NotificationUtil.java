package com.twolinessoftware.utils;

import android.support.v7.app.AlertDialog;

import com.twolinessoftware.activities.BaseActivity;

/**
 *
 */
public class NotificationUtil {


    public static void showGenericOkDialog(final BaseActivity activity,String title, String message) {

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(com.twolinessoftware.R.string.dialog_ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }

    public static void showErrorDialog(final BaseActivity activity,String message) {

        new AlertDialog.Builder(activity)
                .setTitle(com.twolinessoftware.R.string.dialog_error)
                .setMessage(message)
                .setPositiveButton(com.twolinessoftware.R.string.dialog_ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }




}
