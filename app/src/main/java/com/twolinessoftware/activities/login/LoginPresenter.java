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

import com.twolinessoftware.ErrorException;
import com.twolinessoftware.activities.BasePresenter;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.network.UserNetworkApi;

import javax.inject.Inject;

import rx.Scheduler;
import timber.log.Timber;

/**
 * Created by johncarpenter on 2016-04-18.
 */
public class LoginPresenter implements BasePresenter<LoginViewCallback> {

    private Scheduler mScheduler;
    private AuthenticationManager mAuthenticationManager;
    private UserNetworkApi mUserNetworkApi;

    private Token mToken;

    @Inject
    public LoginPresenter(UserNetworkApi userNetworkApi, AuthenticationManager authenticationManager, Scheduler scheduler) {
        mUserNetworkApi = userNetworkApi;
        mAuthenticationManager = authenticationManager;
        mScheduler = scheduler;
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

    public void login(final String email, final String password) {
        if (mLoginViewCallback != null) {
            mLoginViewCallback.showProgress(true);
        }

        mUserNetworkApi.authenticate(email, password)
                .subscribeOn(mScheduler)
                .flatMap(token -> {
                    mToken = token;
                    return mUserNetworkApi.getMe();
                })
                .subscribe(user -> {
                    Timber.v("User Logged in:" + user.getUid());
                    if (mLoginViewCallback != null) {
                        mLoginViewCallback.showProgress(false);
                        mLoginViewCallback.onFinishLogin(mAuthenticationManager.generateAuthIntent(mToken, email, password));
                    }
                }, error -> {
                    if (mLoginViewCallback != null) {
                        mLoginViewCallback.showProgress(false);
                    }

                    if (error instanceof ErrorException) {
                        ErrorException errorException = (ErrorException) error;
                        if (mLoginViewCallback != null) {
                            mLoginViewCallback.onError(errorException.getCode());
                        }
                    } else {
                        if (mLoginViewCallback != null) {
                            mLoginViewCallback.onError(ErrorException.Code.GENERIC_ERROR);
                        }
                    }
                });


    }


}
