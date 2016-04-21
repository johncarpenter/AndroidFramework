package com.twolinessoftware.network;

import android.content.Intent;

import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.authentication.UserManager;
import com.twolinessoftware.model.User;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

/**
 *
 */
public class NetworkManager {

    private AuthenticationManager mAuthenticationManager;
    private UserManager mUserManager;
    private PreferencesHelper mPreferenceHelper;


    @Inject
    public NetworkManager( UserManager userManager, PreferencesHelper preferencesHelper,AuthenticationManager authenticationManager) {
        mUserManager = userManager;
        mPreferenceHelper = preferencesHelper;
        mAuthenticationManager = authenticationManager;
    }

    public Observable<User> getMe(){

        Timber.v("Retrieving User Profile");

        return mUserManager.getMe();
    }

    public Observable<Token> authenticate(final String email, final String pass) {

        Timber.v("Authenticating " + email);

        return mUserManager.login(email,pass)
                .flatMap(token -> {
                    mPreferenceHelper.storeToken(token.getAccessToken());
                    Intent intent = mAuthenticationManager.generateAuthIntent(token, email, pass);
                    mAuthenticationManager.completeLogin(intent);
                    return Observable.just(token);
                });

    }

    public Observable<Token> register(String email, String pass) {

        Timber.v("Creating new account " + email);

        return mUserManager.register(email,pass)
                .flatMap(token -> {
                    mPreferenceHelper.storeToken(token.getAccessToken());
                    Intent intent = mAuthenticationManager.generateAuthIntent(token, email, pass);
                    mAuthenticationManager.completeLogin(intent);
                    return Observable.just(token);
                });
    }

    public Observable<Boolean> forgotPassword(final String email){

        Timber.v("Sending Forgot Password Link");

        return mUserManager.forgotPassword(email);

    }


}
