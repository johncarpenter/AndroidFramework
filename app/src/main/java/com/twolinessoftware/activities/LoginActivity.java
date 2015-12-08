package com.twolinessoftware.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.Constants;
import com.twolinessoftware.authentication.AuthenticationManager;
import com.twolinessoftware.events.OnAccountLoggedInEvent;
import com.twolinessoftware.events.OnAccountPasswordResetEvent;
import com.twolinessoftware.events.OnCommunicationStatusEvent;
import com.twolinessoftware.events.OnErrorEvent;
import com.twolinessoftware.fragments.LoginFragment;
import com.twolinessoftware.fragments.RegisterFragment;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.utils.NotificationUtil;

import javax.inject.Inject;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;
import timber.log.Timber;

/**
 * Created by John on 2015-04-02.
 */
public class LoginActivity extends BaseActivity {

    public static final String EXTRA_IS_ADDING = "EXTRA_IS_ADDING";
    public static final String KEY_ACCOUNT_PASSWORD = "KEY_PASSWORD";

    private RegisterFragment m_registerFragment;

    private LoginFragment m_loginFragment;


    @Inject
    AccountManager mAccountManager;

    @Inject
    AuthenticationManager mAuthenticationManager;

    @Inject
    GoogleServicesManager mGoogleServicesManager;



    private AccountAuthenticatorResponse m_accountAuthenticatorResponse;

    private Bundle m_resultBundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaseApplication.get(this).getComponent().inject(this);



        if (mAuthenticationManager.isLoggedIn()) {
            Timber.e("Account is already logged in. Finishing activty");
            finish();
        }

        m_registerFragment = new RegisterFragment();
        m_loginFragment = new LoginFragment();

        setFragment(m_registerFragment,false);

        m_accountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (m_accountAuthenticatorResponse != null) {
            m_accountAuthenticatorResponse.onRequestContinued();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_register:
                setFragment(m_registerFragment, true);
                return true;
            case R.id.menu_login:
                setFragment(m_loginFragment, true);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void onEventMainThread(OnCommunicationStatusEvent event) {
         Timber.v("Communication Progress:" + event.getStatus().toString());
        switch (event.getStatus()) {
            case busy:
                showProgress(true);
                break;
            default:
                showProgress(false);
        }
    }

    public void onEventMainThread(OnErrorEvent event) {
        Timber.e("Error event:" + event.getError().getDisplayError());

        NotificationUtil.showErrorCrouton(this, R.id.fragment_container, getString(event.getError().getDisplayError()));
    }

    public void onEventMainThread(OnAccountLoggedInEvent event) {
        Intent intent = event.getIntent();

        finishLogin(event.getIntent());

        // @todo send the GCM registration to the server
        mGoogleServicesManager.getGCMService().register();

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }



    public void onEventMainThread(OnAccountPasswordResetEvent event) {
        Crouton c = NotificationUtil.getInfoCrouton(this,R.id.fragment_container,getString(R.string.reset_link_sent));
        c.setLifecycleCallback(new LifecycleCallback() {
            @Override
            public void onDisplayed() {
            }

            @Override
            public void onRemoved() {
                finish();
            }
        });
        c.show();
    }

    private void finishLogin(Intent intent) {

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        final Account account = new Account(accountName, Constants.BASE_ACCOUNT_TYPE);

        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        String authtokenType = Constants.BASE_ACCOUNT_TYPE;

        ContentResolver.setSyncAutomatically(account, Constants.BASE_ACCOUNT_TYPE, true);
        ContentResolver.setIsSyncable(account, Constants.BASE_ACCOUNT_TYPE, 1);

        mAccountManager.addAccountExplicitly(account, null, null);
        mAccountManager.setAuthToken(account, authtokenType, authtoken);


    }


    /*
    From AccountAuthenticatorActivity Class
    */
    public final void setAccountAuthenticatorResult(Bundle result) {
        m_resultBundle = result;
    }

    @Override
    public void finish() {
        if (m_accountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (m_resultBundle != null) {
                m_accountAuthenticatorResponse.onResult(m_resultBundle);
            } else {
                m_accountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "cancelled");
            }
            m_accountAuthenticatorResponse = null;
        }
        super.finish();
    }

}
