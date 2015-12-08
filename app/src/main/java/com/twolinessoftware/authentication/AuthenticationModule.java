package com.twolinessoftware.authentication;

import android.accounts.AccountManager;
import android.content.Context;

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


}
