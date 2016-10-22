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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseFragment;

import javax.inject.Inject;

import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by johncarpenter on 2016-04-20.
 */
public class MainLoginSplashFragment extends BaseFragment {

    @Inject
    MainLoginSplashPresenter mMainLoginSplashPresenter;

    private LoginViewCallback mCallback;

    public static MainLoginSplashFragment newInstance() {
        return new MainLoginSplashFragment();
    }

    @Override
    public void onAttachToContext(Context context) {
        super.onAttachToContext(context);
        Timber.v("Attaching main login");
        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

        if ( context instanceof LoginViewCallback ) {
            mCallback = (LoginViewCallback) context;

            mMainLoginSplashPresenter.attachView(mCallback);
        } else {
            Timber.e("Fragment called outside of Login Context");
            throw new IllegalArgumentException("Fragment Called Outside of LoginActivity Context");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.v("Detaching Presenter");
        mMainLoginSplashPresenter.detachView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.v("Restoring view state");

        setToolbarVisibility(false);
        enableBack(false);
    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_main_login;
    }


    @OnClick(R.id.button_register)
    public void onButtonRegister() {
        mMainLoginSplashPresenter.navigateToCreate();
    }

    @OnClick(R.id.button_login)
    public void onButtonLogin() {
        mMainLoginSplashPresenter.navigateToSignIn();
    }


    @Override
    public void setButtonsEnabled(boolean busy) {

    }
}
