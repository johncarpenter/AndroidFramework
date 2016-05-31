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
import android.widget.EditText;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseFragment;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.utils.ValidationUtil;
import com.twolinessoftware.utils.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import timber.log.Timber;

/**
 * Created by John on 2015-04-02.
 */
public class ResetPasswordFragment extends BaseFragment implements Validator.ValidationListener {

    @Email(messageResId = R.string.error_invalid_email)
    @NotEmpty(messageResId = R.string.error_field_required)
    @Bind(R.id.edit_email)
    AutoCompleteTextView mEditEmail;

    @Inject
    EventBus mEventBus;

    @Inject
    AccountManager mAccountManager;

    @Inject
    NetworkManager mNetworkManager;

    @Inject
    ResetPasswordPresenter mResetPasswordPresenter;


    private Validator mValidator;


    public static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }

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

        mValidator = new Validator(this);
        mValidator.setValidationListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        BaseApplication.get(getBaseActivity()).getComponent().inject(this);

        if ( context instanceof LoginViewCallback ) {
            LoginViewCallback callback = (LoginViewCallback) context;
            mResetPasswordPresenter.attachView(callback);
        } else {
            Timber.e("Fragment called outside of Login Context");
            throw new IllegalArgumentException("Fragment Called Outside of LoginActivity Context");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mResetPasswordPresenter.detachView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepopulateAccount();

        mEditEmail.setAdapter(ViewUtils.getEmailAddressAdapter(getBaseActivity()));
        mEditEmail.setOnFocusChangeListener((v, hasfocus) -> {
            if ( hasfocus ) {
                mEditEmail.setText("");
            }
            mEditEmail.setOnFocusChangeListener(null);
        });

        mEditEmail.setOnEditorActionListener((v, actionId, event) -> {
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


    @OnClick(R.id.button_reset)
    public void onClickLogin(View view) {
        mValidator.validate();
    }


    @Override
    public void onValidationSucceeded() {
        String email = mEditEmail.getText().toString().trim();
        mResetPasswordPresenter.resetPassword(email);
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
