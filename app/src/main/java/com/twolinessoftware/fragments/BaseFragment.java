/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twolinessoftware.activities.BaseActivity;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {


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

    public BaseActivity getBaseActivity(){
        return ((BaseActivity)getActivity());
    }

}
