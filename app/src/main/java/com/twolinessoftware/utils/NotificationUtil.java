package com.twolinessoftware.utils;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;

import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseActivity;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 *
 */
public class NotificationUtil {

    public static void showErrorCrouton(Activity activity, @IdRes int container, String message) {

        Style style = new Style.Builder()
                .setHeightDimensionResId(R.dimen.crouton_height)
                .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                .setBackgroundColorValue(activity.getResources().getColor(R.color.pal_red))
                .build();

        Crouton.makeText(activity, message, style, container).show();
    }

    public static Crouton getInfoCrouton(Activity activity, @IdRes int container, String message) {

        Style style = new Style.Builder()
                .setHeightDimensionResId(R.dimen.crouton_height)
                .setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)
                .setBackgroundColorValue(activity.getResources().getColor(R.color.pal_blue))
                .build();

        return Crouton.makeText(activity, message, style, container);
    }


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
