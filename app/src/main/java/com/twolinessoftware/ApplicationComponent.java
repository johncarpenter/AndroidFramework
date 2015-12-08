package com.twolinessoftware;

import android.app.Application;

import com.twolinessoftware.activities.LoginActivity;
import com.twolinessoftware.authentication.AccountAuthenticatorService;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.authentication.AuthenticationModule;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.data.DataManagerModule;
import com.twolinessoftware.fragments.LoginFragment;
import com.twolinessoftware.fragments.RegisterFragment;
import com.twolinessoftware.fragments.ResetPasswordFragment;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.network.NetworkModule;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.notifications.GoogleServicesModule;
import com.twolinessoftware.services.SyncNotificationsService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class, NetworkModule.class, GoogleServicesModule.class, DataManagerModule.class
, AuthenticationModule.class})
public interface ApplicationComponent {

    // Activities
    void inject(LoginActivity loginActivity);


    // Fragments
    void inject(LoginFragment loginFragment);
    void inject(RegisterFragment registerFragment);
    void inject(ResetPasswordFragment resetPasswordFragment);

    // Adapters

    // Managers
    void inject(NetworkManager networkManager);
    void inject(GoogleServicesManager googleServicesManager);

    // Services
    void inject(SyncNotificationsService syncNotificationsService);
    void inject(AccountAuthenticatorService accountAuthenticatorService);

    Application application();
    NetworkManager networkManager();
    DataManager dataManager();
    GoogleServicesManager googleServicesManager();
    AuthenticationManager authenticationManager();
}
