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

package com.twolinessoftware.activities;

import android.app.Application;

import com.twolinessoftware.activities.login.LoginPresenter;
import com.twolinessoftware.activities.login.RegisterPresenter;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.network.NetworkManager;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;

/**
 * Created by johncarpenter on 2016-04-19.
 */
@Module
public class UiModule {

    private final Application mApplication;

    public UiModule(Application application) {
        mApplication = application;
    }


    @Provides
    LoginPresenter providesLoginPresenter(NetworkManager networkManager, AuthenticationManager authenticationManager){
        return new LoginPresenter(networkManager,authenticationManager, Schedulers.io());
    }

    @Provides
    RegisterPresenter providesRegisterPresenters(NetworkManager networkManager, AuthenticationManager authenticationManager){
        return new RegisterPresenter(networkManager,authenticationManager,Schedulers.io());
    }

}

