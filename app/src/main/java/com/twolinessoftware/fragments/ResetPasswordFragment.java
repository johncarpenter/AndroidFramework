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
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.service.AccountService;
import com.twolinessoftware.smarterlist.util.AccountUtils;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by John on 2015-04-02.
 */
public class ResetPasswordFragment extends  BaseFragment{

    @InjectView(R.id.edit_email)
    EditText m_editEmail;

    @Inject
    Bus m_eventBus;

    @Inject
    AccountManager m_accountManager;

    @Inject
    AccountService m_accountService;

    @Override
    protected int setContentView() {
        return R.layout.fragment_resetpassword;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getBaseActivity().setTitle(R.string.login_toolbar_title);
        Injector.inject(this);
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

        m_editEmail.setOnFocusChangeListener((v,hasfocus)->{
            if(hasfocus) {
                m_editEmail.setText("");
            }
            m_editEmail.setOnFocusChangeListener(null);
        });
    }

    private void prepopulateAccount() {

        Account[] accounts = m_accountManager.getAccounts();
        for (Account account : accounts)
        {
            if(AccountUtils.isValidEmail(account.name)){
                m_editEmail.setText(account.name);
                break;
            }
        }
    }



    @OnClick(R.id.button_reset)
    public void onClickLogin(View view){
        String email = m_editEmail.getText().toString().trim();

       if(!AccountUtils.isValidEmail(email)){
           m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_INVALID_EMAIL));
       }else{
           m_accountService.resetPassword(email);
        }
    }


}
