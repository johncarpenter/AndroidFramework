package com.twolinessoftware;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.twolinessoftware.model.User;
import com.twolinessoftware.utils.GsonUtil;

import javax.inject.Inject;

import timber.log.Timber;

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

    public void storeToken(String accessToken) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEYS.PREFERENCE_AUTH_TOKEN, accessToken);
        editor.apply();
    }

    public String getAuthToken() {
        return mSharedPreferences.getString(KEYS.PREFERENCE_AUTH_TOKEN, null);
    }

    public void storeUserProfile(User user) {
        Timber.v("Storing User Profile:"+user.toString());
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(KEYS.PREFERENCE_USER, GsonUtil.buildGsonAdapter().toJson(user));
        editor.commit();
    }

    public User getUserProfile() {
        if ( !mSharedPreferences.contains(KEYS.PREFERENCE_USER) ) {
            return null;
        }
        return GsonUtil.buildGsonAdapter().fromJson(mSharedPreferences.getString(KEYS.PREFERENCE_USER, null), User.class);
    }

    @Nullable
    public String getUserUid(){
        User user = getUserProfile();
        if( user != null ){
            return user.getUid();
        }
        return null;
    }


    private static class KEYS {
        public static final String PREFERENCE_AUTH_TOKEN = "auth_token";
        public static final String PREFERENCE_USER = "user_json";
        public static final String PREFERENCE_REG_ID = "gcm_registration";
        public static final String PREFERENCE_APP_VERSION = "gcm_version";
    }

}
