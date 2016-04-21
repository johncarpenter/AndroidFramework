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
import com.twolinessoftware.activities.login.RegisterPresenter;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.model.User;
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
public class RegisterPresenterTest {

    @Mock
    LoginViewCallback mLoginViewCallback;

    @Mock
    NetworkManager mNetworkManager;

    @Mock
    AuthenticationManager mAuthenticationManager;

    private RegisterPresenter mRegisterPresenter;

    @Before
    public void before(){
        initMocks(this);
        mRegisterPresenter = new RegisterPresenter(mNetworkManager,mAuthenticationManager, Schedulers.immediate());
        mRegisterPresenter.attachView(mLoginViewCallback);
    }

    @Test
    public void registerPresenter_EnsureLoginIsCalled(){


        when(mNetworkManager.register(any(),any())).thenReturn(Observable.just(new Token("test",10)));

        when(mNetworkManager.getMe()).thenReturn(Observable.just(new User("email")));

        mRegisterPresenter.register("email","password");


        verify(mLoginViewCallback).showProgress(true);
        verify(mNetworkManager).register("email","password");
        verify(mNetworkManager).getMe();

        verify(mLoginViewCallback).showProgress(false);
        verify(mLoginViewCallback).onFinishLogin(any());

    }

    @Test
    public void registerPresenter_ShowErrorOnInvalidPassword(){

        when(mNetworkManager.register(any(),any())).thenReturn(Observable.error(new ErrorException(ErrorException.Code.EMAIL_TAKEN)));

        mRegisterPresenter.register("email","password");
        verify(mLoginViewCallback).showProgress(true);
        verify(mNetworkManager).register("email","password");
        verify(mNetworkManager,never()).getMe();

        verify(mLoginViewCallback).showProgress(false);
        verify(mLoginViewCallback).onError(ErrorException.Code.EMAIL_TAKEN);

    }

}
