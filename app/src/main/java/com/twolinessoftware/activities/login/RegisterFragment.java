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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseFragment;
import com.twolinessoftware.utils.ThemeUtil;
import com.twolinessoftware.utils.ValidationUtil;
import com.twolinessoftware.utils.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;


/**
 * Created by John on 2015-04-02.
 */
public class RegisterFragment extends BaseFragment implements Validator.ValidationListener {

    private Validator mValidator;


    @Email(messageResId = R.string.error_invalid_email)
    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_password)
    EditText mEditPassword;

    @Bind(R.id.button_register)
    Button mButtonRegister;

    @Inject
    RegisterPresenter mRegisterPresenter;

    @Inject
    AccountManager mAccountManager;

    private LoginViewCallback mCallback;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBaseActivity().setTitle(R.string.register_fragment_title);


        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

        if ( context instanceof LoginViewCallback ) {
            mCallback = (LoginViewCallback) context;
            mRegisterPresenter.attachView(mCallback);
        } else {
            Timber.e("Fragment called outside of Login Context");
            throw new IllegalArgumentException("Fragment Called Outside of LoginActivity Context");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRegisterPresenter.detachView();
    }

    @Override
    protected int setContentView() {
        return R.layout.fragment_register;
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        getToolbar().setTitle(getString(R.string.register_fragment_title));
        enableBack(true);
        setToolbarVisibility(true);


        prepopulateAccount();

        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));
        mEditEmail.setCompoundDrawables(new IconDrawable(getBaseActivity(), MaterialIcons.md_email).color(ThemeUtil.getPrimaryColor(getBaseActivity())).actionBarSize(), null, null, null);
        mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if ( hasfocus ) {
                mEditEmail.setText("");
            }
            mEditEmail.setOnFocusChangeListener(null);
        });


        mEditPassword.setCompoundDrawables(new IconDrawable(getBaseActivity(), MaterialIcons.md_lock_open).color(ThemeUtil.getPrimaryColor(getBaseActivity())).actionBarSize(), null, null, null);
        mEditPassword.requestFocus();

        mEditPassword.setOnEditorActionListener((v, actionId, event) -> {
            if ( actionId == EditorInfo.IME_ACTION_DONE ) {
                mValidator.validate();
                return true;
            }
            return false;
        });
    }

    private void prepopulateAccount() {

        Account[] accounts = mAccountManager.getAccounts();
        for ( Account account : accounts ) {
            if ( ValidationUtil.isValidEmail(account.name) ) {
                mEditEmail.setText(account.name);
                break;
            }
        }
    }

    @OnClick(R.id.button_register)
    public void onClickLogin(View view) {
        mValidator.validate();
    }

    @Override
    public void onValidationSucceeded() {

        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
        mCallback.showProgress(true);
        mRegisterPresenter.register(email, password);
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for ( ValidationError error : errors ) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());

            // Display error messages ;)
            if ( view instanceof EditText ) {
                EditText editText = ((EditText) view);
                editText.setError(message);
                editText.requestFocus();
                break;
            } else {
                Timber.v("Unknown validation error:");
            }
        }
    }

    @Override
    public void setButtonsEnabled(boolean busy) {

    }
}
