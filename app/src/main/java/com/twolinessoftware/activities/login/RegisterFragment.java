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
import android.widget.Button;
import android.widget.EditText;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseFragment;
import com.twolinessoftware.utils.ValidationUtil;

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

    public static RegisterFragment newInstance(){
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

        if(context instanceof LoginViewCallback){
            mCallback = (LoginViewCallback) context;
            mRegisterPresenter.attachView(mCallback);
        }else{
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
        mValidator.validate();
    }




    @Override
    public void onValidationSucceeded() {

        String email = mEditEmail.getText().toString().trim();
        String password = mEditPassword.getText().toString().trim();
        mCallback.showProgress(true);
        mRegisterPresenter.register(email,password);
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
