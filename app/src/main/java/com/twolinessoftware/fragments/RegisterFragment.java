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
import android.widget.Button;
import android.widget.EditText;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.events.OnCommunicationStatusEvent;
import com.twolinessoftware.events.OnErrorEvent;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.utils.ValidationUtil;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by John on 2015-04-02.
 */
public class RegisterFragment extends  BaseFragment{
    @Override
    protected int setContentView() {
        return R.layout.fragment_register;
    }

    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @Bind(R.id.edit_password)
    EditText mEditPassword;

    @Bind(R.id.button_register)
    Button mButtonRegister;

    @Inject
    AccountManager mAccountManager;

    @Inject
    NetworkManager mNetworkManager;

    @Inject
    EventBus mEventBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getBaseActivity().setTitle(R.string.register_fragment_title);
        BaseApplication.get(getBaseActivity()).getComponent().inject(this);
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

        mEditPassword.requestFocus();
        mEditPassword.setTransformationMethod(null);

        mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if (hasfocus) {
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

    @OnClick(R.id.button_register)
    public void onClickLogin(View view){
        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();

        if (validateFields(email,password)) {
            mNetworkManager.register(mEditEmail.getText().toString(), mEditPassword.getText().toString());
        }
    }

    private boolean validateFields(String email, String password) {

        if(email == null || email.isEmpty() || password == null || password.isEmpty()){
            mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_EMPTY_FIELDS));
            return false;
        }

        if(password.length() < 6){
            mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_PASSWORD_LENGTH));
            return false;
        }

        if(!ValidationUtil.isValidEmail(email)){
            mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.VALIDATION_INVALID_EMAIL));
            return false;
        }
        return true;
    }

    public void onEventMainThread(OnCommunicationStatusEvent event) {
        switch (event.getStatus()) {
            case busy:
                mButtonRegister.setEnabled(false);
                break;
            default:
                mButtonRegister.setEnabled(true);
        }
    }
}
