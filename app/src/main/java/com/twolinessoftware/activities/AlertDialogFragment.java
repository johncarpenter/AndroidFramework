/*
 * Copyright (c) 2016. Petrofeed Inc
 *
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Petrofeed Inc and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Petrofeed Inc
 *  and its suppliers and may be covered by U.S. and Foreign Patents,
 *  patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Petrofeed Inc.
 *
 */

package com.twolinessoftware.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.twolinessoftware.R;

public class AlertDialogFragment extends DialogFragment {


    private static final String EXTRA_TITLE = "EXTRA_TITLE";
    private static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    private String mTitle;
    private String mMessage;

    public static AlertDialogFragment newInstance(String title, String message) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);

        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mTitle = getArguments().getString(EXTRA_TITLE);
        mMessage = getArguments().getString(EXTRA_MESSAGE);

        return new AlertDialog.Builder(getContext())
                .setTitle(mTitle)
                .setMessage(mMessage)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }


}
