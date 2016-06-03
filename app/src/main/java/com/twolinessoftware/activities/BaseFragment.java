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

package com.twolinessoftware.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import icepick.Icepick;
import rx.subscriptions.CompositeSubscription;

public abstract class BaseFragment extends Fragment implements UICallback{

    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    protected abstract int setContentView();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this,savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(setContentView(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    // Finishes the fragment and passes control to the activity
    protected void finish(boolean removeFromStack) {
        if ( removeFromStack ) {
            getBaseActivity().onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeSubscription.clear();
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

    public Toolbar getToolbar(){ return activity.getToolbar();}

    public void enableBack(boolean back){
        activity.enableBack(back);
    }

    public void setToolbarVisibility(boolean visible){
        if(getBaseActivity().getSupportActionBar() != null){
            if(visible){
                getBaseActivity().getSupportActionBar().show();
            }else{
                getBaseActivity().getSupportActionBar().hide();
            }
        }
    }

    /**
     *
     * Called from #OnBackPressed in BaseActivity
     *
     * @return true if the action is consumed.
     */
    public boolean onBackPressed(){ return false;}

}
