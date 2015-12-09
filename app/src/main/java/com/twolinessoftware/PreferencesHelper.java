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

    public void storeGcmRegistration(String regId, int appVersion) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEYS.PREFERENCE_REG_ID, regId);
        editor.putInt(KEYS.PREFERENCE_APP_VERSION, appVersion);
        editor.apply();
    }

    public String getGcmRegistration() {

        return mSharedPreferences.getString(KEYS.PREFERENCE_REG_ID, "");

    }

    public int getGcmRegistrationVersion() {
        return mSharedPreferences.getInt(KEYS.PREFERENCE_APP_VERSION, Integer.MIN_VALUE);
    }


    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    private static class KEYS {
        public static final String PREFERENCE_REG_ID = "gcm_registration";
        public static final String PREFERENCE_APP_VERSION = "gcm_version";
    }

}
