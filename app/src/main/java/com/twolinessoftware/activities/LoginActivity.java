package com.twolinessoftware.activities;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.twolinessoftware.fragments.LoginFragment;
import com.twolinessoftware.fragments.RegisterFragment;

import javax.inject.Inject;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.LifecycleCallback;

/**
 * Created by John on 2015-04-02.
 */
public class LoginActivity extends BaseActivity {

    public static final String EXTRA_IS_ADDING = "EXTRA_IS_ADDING";
    public static final String KEY_ACCOUNT_PASSWORD = "KEY_PASSWORD";

    private RegisterFragment m_registerFragment;

    private LoginFragment m_loginFragment;

    @Inject
    AccountManager m_accountManager;

    private AccountAuthenticatorResponse m_accountAuthenticatorResponse;

    private Bundle m_resultBundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (m_accountUtils.isLoggedIn()) {
            // @todo logout page?
            Ln.e("Account is already logged in");
            finish();
        }

        m_registerFragment = new RegisterFragment();
        m_loginFragment = new LoginFragment();

        showFragment(m_registerFragment,false,false);

        showEntryDialog();

        m_accountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (m_accountAuthenticatorResponse != null) {
            m_accountAuthenticatorResponse.onRequestContinued();
        }

    }

    private void showEntryDialog() {
        Dialog dialog = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getString(R.string.register_why_signup))
                .setMessage(getString(R.string.register_why_signup_text))
                .setPositiveButton(R.string.register_why_signup_button, (dialog1, which) -> {

                })
                .setCancelable(true)
                .create();

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_register:
                showFragment(m_registerFragment, false, true);
                return true;
            case R.id.menu_login:
                showFragment(m_loginFragment, false, true);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onCommunicationEvent(OnCommunicationStatusEvent event) {
        Ln.v("Progress:" + event.getStatus().toString());
        switch (event.getStatus()) {
            case PROGRESS:
                showProgress(true);
                break;
            default:
                showProgress(false);
        }
    }

    @Subscribe
    public void onErrorEvent(OnErrorEvent event) {
        Ln.e("Error event:"+event.getError().getDisplayError());

        showErrorCrouton(getString(event.getError().getDisplayError()));
    }

    @Subscribe
    public void onAccountLoginEvent(OnAccountLoggedInEvent event) {
        Intent intent = event.getIntent();

        finishLogin(event.getIntent());

        m_googleService.registerGcm();


        if(event.isNewAccount()){
            createInitialList();
        }

        m_accountUtils.scheduleSmartlistSync();

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();// @todo show upsell page?

    }

    private void createInitialList() {
        SmartList smartList = new SmartList(getString(R.string.default_smartlist_name),
                getString(R.string.default_smartlist_desc),
                Constants.DEFAULT_MASTERLIST_NAME,"http://storage.googleapis.com/smarterlist_icons/ic_grocery_list.png");
        smartList.setOwner(m_accountUtils.getEmailAddress());

        // Write the data, sync later
        smartList.setItemId(0);
        int localId = m_smartListDAO.save(smartList);
        smartList = m_smartListDAO.findById(localId);

        // Try a background sync now
       m_smartListService.createSmartList(smartList);
    }


    @Subscribe
    public void onAccountResetSent(OnAccountPasswordResetEvent event) {
        Crouton c = getInfoCrouton(getString(R.string.reset_link_sent));
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
        String accountPassword = intent.getStringExtra(KEY_ACCOUNT_PASSWORD);
        final Account account = new Account(accountName, Constants.SMARTERLIST_ACCOUNT_TYPE);

        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        String authtokenType = Constants.SMARTERLIST_TOKEN_TYPE;

        ContentResolver.setSyncAutomatically(account, Constants.SMARTERLIST_ACCOUNT_TYPE, true);
        ContentResolver.setIsSyncable(account, Constants.SMARTERLIST_ACCOUNT_TYPE, 1);

        m_accountManager.addAccountExplicitly(account, accountPassword, null);
        m_accountManager.setAuthToken(account, authtokenType, authtoken);

        m_accountUtils.forceAuthToken(authtoken);


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
