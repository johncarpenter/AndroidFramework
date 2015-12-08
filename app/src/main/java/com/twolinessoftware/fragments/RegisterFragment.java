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
import android.widget.Button;
import android.widget.EditText;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.R;
import com.twolinessoftware.smarterlist.event.OnCommunicationStatusEvent;
import com.twolinessoftware.smarterlist.event.OnErrorEvent;
import com.twolinessoftware.smarterlist.service.AccountService;
import com.twolinessoftware.smarterlist.util.AccountUtils;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by John on 2015-04-02.
 */
public class RegisterFragment extends  BaseFragment{
    @Override
    protected int setContentView() {
        return R.layout.fragment_register;
    }

    @InjectView(R.id.edit_email)
    EditText m_editEmail;

    @InjectView(R.id.edit_password)
    EditText m_editPassword;

    @InjectView(R.id.button_register)
    Button m_buttonRegister;

    @Inject
    AccountManager m_accountManager;

    @Inject
    Bus m_eventBus;

    @Inject
    AccountService m_accountService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getBaseActivity().setTitle(R.string.register_toolbar_title);
        Injector.inject(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.register_menu, menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prepopulateAccount();

        m_editPassword.requestFocus();
        m_editPassword.setTransformationMethod(null);

        m_editEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if (hasfocus) {
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

    @OnClick(R.id.button_register)
    public void onClickLogin(View view){

        String email = m_editEmail.getText().toString().trim();
        String password = m_editPassword.getText().toString().trim();

        if (validateFields(email,password)) {
            m_accountService.registerUser(m_editEmail.getText().toString(), m_editPassword.getText().toString());
        }
    }

    private boolean validateFields(String email, String password) {

        if(email == null || email.isEmpty() || password == null || password.isEmpty()){
            m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_EMPTY_FIELDS));
            return false;
        }

        if(password.length() < 6){
            m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_PASSWORD_LENGTH));
            return false;
        }

        if(!AccountUtils.isValidEmail(email)){
            m_eventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_INVALID_EMAIL));
            return false;
        }
        return true;
    }

    @Subscribe
    public void onCommunicationEvent(OnCommunicationStatusEvent event) {
        switch (event.getStatus()) {
            case PROGRESS:
                m_buttonRegister.setEnabled(false);
                break;
            default:
                m_buttonRegister.setEnabled(true);
        }
    }
}
