package com.twolinessoftware;

import android.content.SharedPreferences;

import javax.inject.Inject;

/**
 * Created by johncarpenter on 2015-12-08.
 */
public class PreferencesHelper {

    private SharedPreferences mSharedPreferences;

    @Inject
    public PreferencesHelper(SharedPreferences sharedPreferences){
        this.mSharedPreferences = sharedPreferences;
    }




}
