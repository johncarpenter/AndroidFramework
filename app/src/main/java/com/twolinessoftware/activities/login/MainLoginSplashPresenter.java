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

import com.twolinessoftware.activities.BasePresenter;

import javax.inject.Inject;

/**
 * Created by johncarpenter on 2016-04-20.
 */
public class MainLoginSplashPresenter implements BasePresenter<LoginViewCallback> {
    @Inject
    public MainLoginSplashPresenter() {
    }

    private LoginViewCallback mLoginViewCallback;

    @Override
    public void attachView(LoginViewCallback baseView) {
        mLoginViewCallback = baseView;
    }

    @Override
    public void detachView() {
        mLoginViewCallback = null;
    }

    public void navigateToSignIn() {
        mLoginViewCallback.onNavigateToLogin();
    }

    public void navigateToCreate() {
        mLoginViewCallback.onNavigateToRegister();
    }
}
