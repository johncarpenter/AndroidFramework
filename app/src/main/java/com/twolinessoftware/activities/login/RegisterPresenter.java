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

package com.twolinessoftware.activities.login;

import android.support.annotation.Nullable;

import com.twolinessoftware.ErrorException;
import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.activities.BasePresenter;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.network.NetworkManager;

import javax.inject.Inject;

import rx.Scheduler;

public class RegisterPresenter implements BasePresenter<LoginViewCallback> {

    private PreferencesHelper mPreferencesHelper;
    private Scheduler mScheduler;
    private AuthenticationManager mAuthenticationManager;
    private NetworkManager mNetworkManager;

    private Token mToken;

    @Inject
    public RegisterPresenter(NetworkManager networkManager, AuthenticationManager authenticationManager, Scheduler scheduler, PreferencesHelper preferencesHelper) {
        mNetworkManager = networkManager;
        mAuthenticationManager = authenticationManager;
        mScheduler = scheduler;
        mPreferencesHelper = preferencesHelper;
    }

    @Nullable
    private LoginViewCallback mLoginViewCallback;

    @Override
    public void attachView(LoginViewCallback baseView) {
        mLoginViewCallback = baseView;
    }

    @Override
    public void detachView() {
        mLoginViewCallback = null;
    }

    public void register(final String email, final String password) {

        if ( mLoginViewCallback != null ) {
            mLoginViewCallback.showProgress(true);
        }

        mNetworkManager.register(email, password)
                .subscribeOn(mScheduler)
                .flatMap(token -> {
                    mToken = token;
                    String uid = mPreferencesHelper.getUserUid();

                    return mNetworkManager.createUser(uid, email);
                })
                .subscribe(user -> {
                    if ( mLoginViewCallback != null ) {
                        mLoginViewCallback.showProgress(false);
                        mLoginViewCallback.onFinishLogin(mAuthenticationManager.generateAuthIntent(mToken, email, password));
                    }
                }, error -> {

                    if ( mLoginViewCallback != null ) {
                        mLoginViewCallback.showProgress(false);
                    }

                    if ( error instanceof ErrorException ) {
                        ErrorException errorException = (ErrorException) error;
                        if ( mLoginViewCallback != null ) {
                            mLoginViewCallback.onError(errorException.getCode());
                        }
                    } else {
                        if ( mLoginViewCallback != null ) {
                            mLoginViewCallback.onError(ErrorException.Code.GENERIC_ERROR);
                        }
                    }
                });

    }


}
