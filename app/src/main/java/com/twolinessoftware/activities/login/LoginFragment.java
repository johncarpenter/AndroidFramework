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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
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
public class LoginFragment extends BaseFragment implements Validator.ValidationListener {

    @Email(messageResId = R.string.error_invalid_email)
    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_password)
    EditText mEditPassword;

    @Inject
    LoginPresenter mLoginPresenter;

    @Inject
    AccountManager mAccountManager;

    private LoginViewCallback mCallback;

    private Validator mValidator;


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    protected int setContentView() {
        return R.layout.fragment_login;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getBaseActivity().setTitle(R.string.login_fragment_title);

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

        if ( context instanceof LoginViewCallback ) {
            mCallback = (LoginViewCallback) context;
            mLoginPresenter.attachView(mCallback);
        } else {
            Timber.e("Fragment called outside of Login Context");
            throw new IllegalArgumentException("Fragment Called Outside of LoginActivity Context");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoginPresenter.detachView();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    @OnClick(R.id.button_login)
    public void onClickLogin(View view) {
        mValidator.validate();
    }

    @OnClick(R.id.text_forgot_password)
    public void onClickForgotPassword(View view) {
        mCallback.onNavigateToForgotPassword();
    }

    @Override
    public void onValidationSucceeded() {
        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
        mLoginPresenter.login(email, password);
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

}
