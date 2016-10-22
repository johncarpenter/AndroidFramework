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

package com.twolinessoftware;

import android.app.Application;

import com.twolinessoftware.activities.login.LoginActivity;
import com.twolinessoftware.activities.login.LoginFragment;
import com.twolinessoftware.activities.login.MainLoginSplashFragment;
import com.twolinessoftware.activities.login.RegisterFragment;
import com.twolinessoftware.activities.login.ResetPasswordFragment;
import com.twolinessoftware.authentication.AccountAuthenticatorService;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.AuthenticationModule;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.data.DataManagerModule;
import com.twolinessoftware.network.NetworkModule;
import com.twolinessoftware.network.UserNetworkApi;
import com.twolinessoftware.services.ServicesModule;
import com.twolinessoftware.services.SyncNotificationsService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, DataManagerModule.class
        , AuthenticationModule.class, ServicesModule.class})
public interface ApplicationComponent {

    // Activities
    void inject(LoginActivity loginActivity);


    // Fragments
    void inject(LoginFragment loginFragment);

    void inject(RegisterFragment registerFragment);

    void inject(ResetPasswordFragment resetPasswordFragment);

    void inject(MainLoginSplashFragment mainLoginSplashFragment);

    // Adapters

    // Managers
    void inject(UserNetworkApi userNetworkApi);


    // Services
    void inject(SyncNotificationsService syncNotificationsService);

    void inject(AccountAuthenticatorService accountAuthenticatorService);


    Application application();

    UserNetworkApi networkManager();

    DataManager dataManager();

    AuthenticationManager authenticationManager();

}
