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

package com.twolinessoftware.android.test.ui;

import android.test.suitebuilder.annotation.SmallTest;

import com.twolinessoftware.ErrorException;
import com.twolinessoftware.activities.login.LoginViewCallback;
import com.twolinessoftware.activities.login.ResetPasswordPresenter;
import com.twolinessoftware.network.NetworkManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SmallTest
public class ResetPasswordPresenterTest {

    @Mock
    LoginViewCallback mLoginViewCallback;

    @Mock
    NetworkManager mNetworkManager;

    private ResetPasswordPresenter mResetPasswordPresenter;

    @Before
    public void before() {
        initMocks(this);
        mResetPasswordPresenter = new ResetPasswordPresenter(mNetworkManager, Schedulers.immediate());
        mResetPasswordPresenter.attachView(mLoginViewCallback);
    }

    @Test
    public void resetPasswordPresenter_EnsureResetCalled() {


        when(mNetworkManager.forgotPassword(any())).thenReturn(Observable.just(true));

        mResetPasswordPresenter.resetPassword("email");

        verify(mLoginViewCallback).showProgress(true);
        verify(mNetworkManager).forgotPassword("email");

        verify(mLoginViewCallback).showProgress(false);
        verify(mLoginViewCallback).onPasswordReset();

    }

    @Test
    public void resetPasswordPresenter_ShowErrorOnResetError() {

        when(mNetworkManager.forgotPassword(any())).thenReturn(Observable.error(new ErrorException(ErrorException.Code.GENERIC_ERROR)));

        mResetPasswordPresenter.resetPassword("email");

        verify(mLoginViewCallback).showProgress(true);
        verify(mNetworkManager).forgotPassword("email");

        verify(mLoginViewCallback).showProgress(false);
        verify(mLoginViewCallback, never()).onPasswordReset();
        verify(mLoginViewCallback).onError(ErrorException.Code.GENERIC_ERROR);

    }

}
