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
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.network.NetworkModule;
import com.twolinessoftware.services.RefreshIDListenerService;
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
    void inject(NetworkManager networkManager);


    // Services
    void inject(SyncNotificationsService syncNotificationsService);

    void inject(AccountAuthenticatorService accountAuthenticatorService);

    void inject(RefreshIDListenerService refreshIDListenerService);

    Application application();

    NetworkManager networkManager();

    DataManager dataManager();

    AuthenticationManager authenticationManager();

}
