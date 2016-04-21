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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseFragment extends Fragment {

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    protected abstract int setContentView();

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setContentView(),container,false);
        ButterKnife.bind(this, view);
        return view;
    }

    // Finishes the fragment and passes control to the activity
    protected void finish(boolean removeFromStack){
        if(removeFromStack) {
            getBaseActivity().onBackPressed();
        }
    }

   @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCompositeSubscription != null && !mCompositeSubscription.isUnsubscribed()){
            mCompositeSubscription.unsubscribe();
        }
    }

    public CompositeSubscription getCompositeSubscription() {
        return mCompositeSubscription;
    }

    private BaseActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }


    public BaseActivity getBaseActivity() {
        return activity;
    }

    public void setTitle(String title) {
        getBaseActivity().getToolbar().setTitle(title);
    }

    public void enableBack(boolean enable) {
        getBaseActivity().enableBack(enable);
    }
}
