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

package com.twolinessoftware.network;

import android.content.Intent;

import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.authentication.UserManager;
import com.twolinessoftware.model.User;

import org.joda.time.DateTime;

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
    public NetworkManager(UserManager userManager, PreferencesHelper preferencesHelper, AuthenticationManager authenticationManager) {
        mUserManager = userManager;
        mPreferenceHelper = preferencesHelper;
        mAuthenticationManager = authenticationManager;
    }

    public Observable<User> createUser(String uid, String email) {
        Timber.v("Creating new user profile");
        User user = new User(email);
        user.setCreated(DateTime.now());

        return mUserManager.createUser(uid, user);
    }

    public Observable<User> getMe() {
        Timber.v("Retrieving User Profile");
        return mUserManager.getMe();
    }

    public Observable<Token> authenticate(final String email, final String pass) {

        Timber.v("Authenticating " + email);

        return mUserManager.login(email, pass)
                .flatMap(token -> {
                    mPreferenceHelper.storeToken(token.getAccessToken());
                    Intent intent = mAuthenticationManager.generateAuthIntent(token, email, pass);
                    mAuthenticationManager.completeLogin(intent);
                    return Observable.just(token);
                });

    }

    public Observable<Token> register(String email, String pass) {

        Timber.v("Creating new account " + email);

        return mUserManager.register(email, pass)
                .flatMap(token -> {
                    mPreferenceHelper.storeToken(token.getAccessToken());
                    Intent intent = mAuthenticationManager.generateAuthIntent(token, email, pass);
                    mAuthenticationManager.completeLogin(intent);
                    return Observable.just(token);
                });
    }

    public Observable<Boolean> forgotPassword(final String email) {

        Timber.v("Sending Forgot Password Link");

        return mUserManager.forgotPassword(email);

    }


}
