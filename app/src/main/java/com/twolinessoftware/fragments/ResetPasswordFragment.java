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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.events.OnErrorEvent;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.utils.ValidationUtil;
import com.twolinessoftware.utils.ViewUtils;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by John on 2015-04-02.
 */
public class ResetPasswordFragment extends  BaseFragment{

    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @Inject
    EventBus mEventBus;

    @Inject
    AccountManager mAccountManager;

    @Inject
    NetworkManager mNetworkManager;

    @Override
    protected int setContentView() {
        return R.layout.fragment_resetpassword;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getBaseActivity().setTitle(R.string.forgot_fragment_title);
        BaseApplication.get(getBaseActivity()).getComponent().inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.login_menu, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepopulateAccount();

        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));
        mEditEmail.setOnFocusChangeListener((v,hasfocus)->{
            if(hasfocus) {
                mEditEmail.setText("");
            }
            mEditEmail.setOnFocusChangeListener(null);
        });
    }

    private void prepopulateAccount() {

        Account[] accounts = mAccountManager.getAccounts();
        for (Account account : accounts)
        {
            if(ValidationUtil.isValidEmail(account.name)){
                mEditEmail.setText(account.name);
                break;
            }
        }
    }



    @OnClick(R.id.button_reset)
    public void onClickLogin(View view){
        String email = mEditEmail.getText().toString().trim();

       if(!ValidationUtil.isValidEmail(email)){
           mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_INVALID_EMAIL));
       }else{
           mNetworkManager.forgotPassword(email);
        }
    }


}
