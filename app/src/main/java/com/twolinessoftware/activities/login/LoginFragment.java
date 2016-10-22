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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
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
import com.mobsandgeeks.saripaar.annotation.Order;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.ErrorException;
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
import timber.log.Timber;

public class LoginFragment extends BaseFragment implements Validator.ValidationListener {


    @Email(messageResId = R.string.error_invalid_email)
    @NotEmpty(messageResId = R.string.error_field_required)
    @Order(1)
    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @NotEmpty(messageResId = R.string.error_field_required)
    @Order(2)
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

    private ButtonsEnabledTextWatcher mButtonsEnabledTextWatcher;

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

        setRetainInstance(true);

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    public void onAttachToContext(Context context) {
        super.onAttachToContext(context);

        BaseApplication.get(context).getComponent().inject(this);

        if (context instanceof LoginViewCallback) {
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
        Timber.v("onDestroy");
        mLoginPresenter.detachView();
    }

    @Override
    public void onResume() {
        super.onResume();
        getToolbar().setTitle(getString(R.string.login_fragment_title));
        enableBack(true);
        setToolbarVisibility(true);

        setButtonsEnabled(false);
        mButtonsEnabledTextWatcher.check();
    }

    @Override
    public void onPause() {
        super.onPause();
        mEditEmail.removeTextChangedListener(mButtonsEnabledTextWatcher);
        mEditPassword.removeTextChangedListener(mButtonsEnabledTextWatcher);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {

        Timber.v("Initializing Views");

        ViewUtils.requestFocusAndShowKeyboard(mEditPassword);

        prepopulateAccount();

        mButtonsEnabledTextWatcher = new ButtonsEnabledTextWatcher(this);

        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));
        mEditEmail.setCompoundDrawables(new IconDrawable(getBaseActivity(), MaterialIcons.md_email).color(ThemeUtil.getPrimaryColor(getBaseActivity())).actionBarSize(), null, null, null);
        mEditEmail.addTextChangedListener(mButtonsEnabledTextWatcher);

        mEditPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mValidator.validate();
                return true;
            }
            return false;
        });
        mEditPassword.addTextChangedListener(mButtonsEnabledTextWatcher);

    }

    private void prepopulateAccount() {
        if (ActivityCompat.checkSelfPermission(getBaseActivity(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {

            if (TextUtils.isEmpty(mEditEmail.getText())) {

                Account[] accounts = mAccountManager.getAccounts();
                for (Account account : accounts) {
                    if (ValidationUtil.isValidEmail(account.name)) {
                        // prepopulate with the email account
                        mEditEmail.setText(account.name);
                        break;
                    }
                }

                /**
                 * If a user selects the email on the first time, erase the whole thing,
                 * our prepopulated guess is probably not partially wrong.!
                 */
                mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
                    if (hasfocus) {
                        mEditEmail.setText("");
                    }
                    mEditEmail.setOnFocusChangeListener(null);
                });
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
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());

            // Display error messages ;)
            if (view instanceof EditText) {
                EditText editText = ((EditText) view);
                editText.setError(message);
                break;
            } else {
                Timber.v("Unknown validation error:");
            }
        }
    }

    @Override
    public void handleError(ErrorException.Code code) {
        super.handleError(code);
        ViewUtils.requestFocusAndShowKeyboard(mEditPassword);
    }

    @Override
    public void setButtonsEnabled(boolean enabled) {
        mButtonLogin.setEnabled(enabled);
    }
}
