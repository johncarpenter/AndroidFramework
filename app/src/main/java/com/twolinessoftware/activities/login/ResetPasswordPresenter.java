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
import com.twolinessoftware.network.UserNetworkApi;

import javax.inject.Inject;

import rx.Scheduler;
import timber.log.Timber;

/**
 * Created by johncarpenter on 2016-04-18.
 */
public class ResetPasswordPresenter implements BasePresenter<LoginViewCallback> {

    private Scheduler mScheduler;
    private UserNetworkApi mUserNetworkApi;

    @Inject
    public ResetPasswordPresenter(UserNetworkApi userNetworkApi, Scheduler scheduler) {
        Timber.v("Creating Presenter");
        mUserNetworkApi = userNetworkApi;
        mScheduler = scheduler;
    }

    private LoginViewCallback mLoginViewCallback;

    @Override
    public void attachView(LoginViewCallback baseView) {
        Timber.v("Attaching Callback");
        mLoginViewCallback = baseView;
    }

    @Override
    public void detachView() {
        Timber.v("Detaching View");
        mLoginViewCallback = null;
    }

    public void resetPassword(final String email) {
        mLoginViewCallback.showProgress(true);

        mUserNetworkApi.forgotPassword(email)
                .subscribeOn(mScheduler)
                .subscribe(reset -> {
                    mLoginViewCallback.showProgress(false);
                    mLoginViewCallback.onPasswordReset();
                }, error -> {
                    mLoginViewCallback.showProgress(false);
                    if (error instanceof ErrorException) {
                        ErrorException errorException = (ErrorException) error;
                        mLoginViewCallback.onError(errorException.getCode());
                    } else {
                        mLoginViewCallback.onError(ErrorException.Code.GENERIC_ERROR);
                    }
                });


    }


}
