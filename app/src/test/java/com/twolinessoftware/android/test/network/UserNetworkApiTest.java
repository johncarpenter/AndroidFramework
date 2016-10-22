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

package com.twolinessoftware.android.test.network;

import android.content.Intent;
import android.test.suitebuilder.annotation.SmallTest;

import com.twolinessoftware.ErrorException;
import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.Token;
import com.twolinessoftware.authentication.UserManager;
import com.twolinessoftware.network.UserNetworkApi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SmallTest
public class UserNetworkApiTest {

    @Mock
    UserManager mUserManager;

    @Mock
    PreferencesHelper mPreferencesHelper;

    @Mock
    AuthenticationManager mAuthenticationManager;

    private UserNetworkApi mUserNetworkApi;

    @Before
    public void before() {
        initMocks(this);

        mUserNetworkApi = new UserNetworkApi(mUserManager, mPreferencesHelper, mAuthenticationManager);
    }

    @Test
    public void network_authenticateShouldStoreUserOnLogin() {

        when(mUserManager.login(any(), any())).thenReturn(Observable.just(new Token("test", 10)));

        when(mAuthenticationManager.generateAuthIntent(any(), any(), any())).thenReturn(new Intent("test"));

        TestSubscriber<Token> testSubscriber = new TestSubscriber();

        mUserNetworkApi.authenticate("email", "pass")
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();


        verify(mPreferencesHelper).storeToken(any());
        verify(mAuthenticationManager).completeLogin(any());

    }

    @Test
    public void network_authenticateShouldForwardErrorsOnLogin() {

        ErrorException errorException = new ErrorException(ErrorException.Code.INVALID_CREDENTIALS);

        when(mUserManager.login(any(), any())).thenReturn(Observable.error(errorException));


        TestSubscriber<Token> testSubscriber = new TestSubscriber();

        mUserNetworkApi.authenticate("email", "pass")
                .subscribe(testSubscriber);

        testSubscriber.assertError(errorException);

        verify(mPreferencesHelper, never()).storeToken(any());
        verify(mAuthenticationManager, never()).completeLogin(any());

    }


    @Test
    public void network_authenticateShouldStoreUserOnRegister() {

        when(mUserManager.register(any(), any())).thenReturn(Observable.just(new Token("test", 10)));

        when(mAuthenticationManager.generateAuthIntent(any(), any(), any())).thenReturn(new Intent());

        TestSubscriber<Token> testSubscriber = new TestSubscriber();

        mUserNetworkApi.register("email", "pass")
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();


        verify(mPreferencesHelper).storeToken(any());
        verify(mAuthenticationManager).completeLogin(any());

    }

    @Test
    public void network_authenticateShouldForwardErrorsOnRegister() {

        ErrorException errorException = new ErrorException(ErrorException.Code.EMAIL_TAKEN);

        when(mUserManager.login(any(), any())).thenReturn(Observable.error(errorException));


        TestSubscriber<Token> testSubscriber = new TestSubscriber();

        mUserNetworkApi.authenticate("email", "pass")
                .subscribe(testSubscriber);

        testSubscriber.assertError(errorException);

        verify(mPreferencesHelper, never()).storeToken(any());
        verify(mAuthenticationManager, never()).completeLogin(any());

    }


}
