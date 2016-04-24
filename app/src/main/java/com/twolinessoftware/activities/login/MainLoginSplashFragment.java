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

    public static MainLoginSplashFragment newInstance(){
        return new MainLoginSplashFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

        if(context instanceof LoginViewCallback){
            mCallback = (LoginViewCallback) context;
            mMainLoginSplashPresenter.attachView(mCallback);
        }else{
            Timber.e("Fragment called outside of Login Context");
            throw new IllegalArgumentException("Fragment Called Outside of LoginActivity Context");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMainLoginSplashPresenter.detachView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_main_login;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick(R.id.button_register)
    public void onButtonRegister(){
        mMainLoginSplashPresenter.navigateToCreate();
    }

    @OnClick(R.id.button_login)
    public void onButtonLogin(){
        mMainLoginSplashPresenter.navigateToSignIn();
    }

}
