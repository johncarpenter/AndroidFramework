package com.twolinessoftware;

import com.twolinessoftware.data.DataManagerModule;

import javax.inject.Singleton;

import dagger.Component;


@Singleton
@Component(modules = {MockApplicationModule.class, MockNetworkModule.class, GoogleServicesModule.class, DataManagerModule.class})
public interface MockComponent extends ApplicationComponent {
    void inject(BaseTest basetTest);
}
