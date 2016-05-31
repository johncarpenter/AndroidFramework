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

package com.twolinessoftware.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import java.util.HashSet;

/**
 * Created by johncarpenter on 2015-12-08.
 */
public class ViewUtils {

    /**
     * Only pulls accounts that are setup by this app. If you want to pull all the accounts you will need the #PermissionUtil.requestPermission(Manifest.permission.READ_CONTACTS) call
     *
     * @param context
     * @return
     */
    public static ArrayAdapter<String> getEmailAddressAdapter(Context context) {
        Account[] accounts = AccountManager.get(context).getAccounts();

        HashSet<String> emailSet = new HashSet<>();
        for ( int i = 0; i < accounts.length; i++ ) {
            if ( ValidationUtil.isValidEmail(accounts[i].name) ) {
                emailSet.add(accounts[i].name);
            }
        }
        String[] emailArray = emailSet.toArray(new String[emailSet.size()]);
        return new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, emailArray);
    }


    public static void clearFocusAndHideKeyboard(View view) {
        view.clearFocus();

        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    public static void requestFocusAndShowKeyboard(View view) {
        view.requestFocus();

        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
