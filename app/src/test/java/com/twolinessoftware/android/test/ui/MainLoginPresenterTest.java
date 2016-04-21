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

import com.twolinessoftware.activities.login.LoginViewCallback;
import com.twolinessoftware.activities.login.MainLoginSplashPresenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@SmallTest
public class MainLoginPresenterTest {

    @Mock
    LoginViewCallback mLoginViewCallback;

    private MainLoginSplashPresenter mMainLoginPresenter;


    @Before
    public void before(){
        initMocks(this);
        mMainLoginPresenter = new MainLoginSplashPresenter();
        mMainLoginPresenter.attachView(mLoginViewCallback);
    }

    @Test
    public void mainLoginPresenter_ShouldRedirectToSignIn(){
        mMainLoginPresenter.navigateToSignIn();
        verify(mLoginViewCallback).onNavigateToLogin();
    }

    @Test
    public void mainLoginPresenter_ShouldRedirectToCreate(){
        mMainLoginPresenter.navigateToCreate();
        verify(mLoginViewCallback).onNavigateToRegister();
    }

}
