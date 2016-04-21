package com.twolinessoftware.authentication;

import android.accounts.AccountManager;
import android.content.Context;

import com.firebase.client.Firebase;
import com.twolinessoftware.Config;
import com.twolinessoftware.PreferencesHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class AuthenticationModule {

    private final Context mContext;

    public AuthenticationModule(Context context) {
        mContext = context;
    }

    @Provides
    AccountManager provideAccountManager(){
        return (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Provides
    @Singleton
    UserManager provideUserManager(Firebase firebase, PreferencesHelper preferencesHelper){
        return new FirebaseUserManager(firebase, preferencesHelper);
    }

    @Provides
    Firebase provideFirebase() {
        return new Firebase(Config.FIREBASE_URL);
    }


}
