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
import com.twolinessoftware.activities.ButtonsEnabledTextWatcher;
import com.twolinessoftware.utils.ThemeUtil;
import com.twolinessoftware.utils.ValidationUtil;
import com.twolinessoftware.utils.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import icepick.State;
import timber.log.Timber;

/**
 * Created by John on 2015-04-02.
 */
public class LoginFragment extends BaseFragment implements Validator.ValidationListener {

    @State
    @Email(messageResId = R.string.error_invalid_email)
    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @State
    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_password)
    EditText mEditPassword;

    @Bind(R.id.button_login)
    Button mButtonLogin;

    @Inject
    LoginPresenter mLoginPresenter;

    @Inject
    AccountManager mAccountManager;

    private LoginViewCallback mCallback;

    private Validator mValidator;


    private ButtonsEnabledTextWatcher mEnableTextWatcher = new ButtonsEnabledTextWatcher(this);


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
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Timber.v("Attaching login");
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
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        getToolbar().setTitle(getString(R.string.login_fragment_title));
        enableBack(true);
        setToolbarVisibility(true);

        prepopulateAccount();

        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));
        mEditEmail.setCompoundDrawables(new IconDrawable(getBaseActivity(), MaterialIcons.md_email).color(ThemeUtil.getPrimaryColor(getBaseActivity())).actionBarSize(), null, null, null);

        /**
         * If they click on the email address field after prepopulating it. Erase all of it.
         */
        mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if ( hasfocus ) {
                mEditEmail.setText("");
            }
            mEditEmail.setOnFocusChangeListener(null);
        });
        mEditEmail.addTextChangedListener(mEnableTextWatcher);

        mEditPassword.setCompoundDrawables(new IconDrawable(getBaseActivity(), MaterialIcons.md_lock_open).color(ThemeUtil.getPrimaryColor(getBaseActivity())).actionBarSize(), null, null, null);
        ViewUtils.requestFocusAndShowKeyboard(mEditPassword);

        mEditPassword.setOnEditorActionListener((v, actionId, event) -> {
            if ( actionId == EditorInfo.IME_ACTION_DONE ) {
                mValidator.validate();
                return true;
            }
            return false;
        });
        mEditPassword.addTextChangedListener(mEnableTextWatcher);

        setButtonsEnabled(false);

        mEnableTextWatcher.check();

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
        setButtonsEnabled(false);

        ViewUtils.clearFocusAndHideKeyboard(mEditPassword);

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
                break;
            } else {
                Timber.v("Unknown validation error:");
            }
        }
    }

    @Override
    public void setButtonsEnabled(boolean enabled) {
        mButtonLogin.setEnabled(enabled);
    }
}
