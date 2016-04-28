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
