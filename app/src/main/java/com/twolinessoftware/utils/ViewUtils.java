package com.twolinessoftware.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
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
}
