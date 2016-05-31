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

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.ErrorException;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.AlertDialogFragment;
import com.twolinessoftware.activities.BaseActivity;
import com.twolinessoftware.authentication.AuthenticationManager;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by John on 2015-04-02.
 */
public class LoginActivity extends BaseActivity implements LoginViewCallback {

    @Inject
    AuthenticationManager mAuthenticationManager;

    private AccountAuthenticatorResponse m_accountAuthenticatorResponse;

    private Bundle m_resultBundle;

    public static Intent getStartIntent(Context context, boolean clearPreviousActivities) {
        Intent intent = new Intent(context, LoginActivity.class);
        if ( clearPreviousActivities ) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseApplication.get(this).getComponent().inject(this);

        if ( mAuthenticationManager.isLoggedIn() ) {
            Timber.e("Account is already logged in. Finishing activity");
            finish();
        }

        if(savedInstanceState == null) {
            setFragment(MainLoginSplashFragment.newInstance(), false);
        }

        m_accountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if ( m_accountAuthenticatorResponse != null ) {
            m_accountAuthenticatorResponse.onRequestContinued();
        }

    }

    /*
    From AccountAuthenticatorActivity Class
    */
    public final void setAccountAuthenticatorResult(Bundle result) {
        m_resultBundle = result;
    }

    @Override
    public void finish() {
        if ( m_accountAuthenticatorResponse != null ) {
            // send the result bundle back if set, otherwise send an error.
            if ( m_resultBundle != null ) {
                m_accountAuthenticatorResponse.onResult(m_resultBundle);
            } else {
                m_accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "cancelled");
            }
            m_accountAuthenticatorResponse = null;
        }
        super.finish();
    }


    @Override
    public void onNavigateToForgotPassword() {
        setFragment(ResetPasswordFragment.newInstance(), true);
    }

    @Override
    public void onNavigateToRegister() {
        setFragment(RegisterFragment.newInstance(), true);
    }

    @Override
    public void onNavigateToLogin() {
        setFragment(LoginFragment.newInstance(), true);
    }

    @Override
    public void onFinishLogin(Intent intent) {
        getCurrentFragment().setButtonsEnabled(true);
        Timber.v("Finishing Login");
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPasswordReset() {
        makeSnackbar(getString(R.string.notification_email_sent), Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onError(ErrorException.Code code) {
        Timber.v("Handling error:" + code.name());

        getCurrentFragment().setButtonsEnabled(true);

        // Show Error to User
        switch (code) {
            case INVALID_CREDENTIALS:
                showDialog(AlertDialogFragment.newInstance(getString(R.string.error_dialog_title), getString(R.string.error_invalid_credentials)), "dialog");
                break;
            case EMAIL_TAKEN:
                showDialog(AlertDialogFragment.newInstance(getString(R.string.error_dialog_title), getString(R.string.error_register_email_taken)), "dialog");
                break;

            default:
                showDialog(AlertDialogFragment.newInstance(getString(R.string.error_dialog_title), getString(R.string.error_communication_generic)), "dialog");
                break;
        }
    }




}
