package com.twolinessoftware.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 *
 */
public class AuthenticationManager {

    private EventBus mEventBus;

    private Context mContext;

    private AccountManager mAccountManager;

    @Inject
    public AuthenticationManager(Context context, AccountManager accountManager){
        mContext = context;
        mAccountManager = accountManager;
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        if (getAccount() == null) {
            mEventBus.post(new OnLogoutEvent());
        }
    }

    public interface TokenRefreshListener {
        void onTokenRefreshed();

        void onTokenError();
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public static final Intent generateAuthIntent(Token accessToken, String username, String password) {

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.SMARTERLIST_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, accessToken.accessToken);
        data.putString(LoginActivity.KEY_ACCOUNT_PASSWORD, password);
        data.putBoolean(LoginActivity.EXTRA_IS_ADDING, true);


        final Intent res = new Intent();
        res.putExtras(data);
        return res;
    }

    public String getEmailAddress() {
        if (!isLoggedIn()) {
            return null;
        }

        return getAccount().name;
    }

    protected String getPassword() {
        if (!isLoggedIn()) {
            return null;
        }
        return m_accountManager.getPassword(getAccount());
    }


    public void invalidateToken() {
        if (getAuthToken() != null) {
            Ln.e("Authentication Token Invalidated");
            m_accountManager.invalidateAuthToken(Constants.SMARTERLIST_ACCOUNT_TYPE, getAuthToken());
        }
    }

    public void refreshAuthToken(final TokenRefreshListener listener) {

        if (getAccount() != null) {

            final AccountManagerFuture<Bundle> future = m_accountManager.getAuthToken(getAccount(), Constants.SMARTERLIST_TOKEN_TYPE, null, false, null, null);

            Observable.create(new Observable.OnSubscribe<String>() {
                @Override
                public void call(Subscriber<? super String> subscriber) {
                    try {

                        Bundle bnd = future.getResult();

                        final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                        subscriber.onNext(authtoken);
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.newThread())
                    .subscribe(token -> {
                        if (token == null) {
                            Ln.e("Unable to retrieve token:null");
                            if (listener != null) {
                                listener.onTokenError();
                            }
                        }
                    }, error -> {
                        Ln.e("Unable to retrieve token");
                        if (listener != null) {
                            listener.onTokenError();
                        }
                    }, () -> {
                        if (listener != null) {
                            listener.onTokenRefreshed();
                        }
                    });

        }
    }

    public void refreshAuthToken() {

        refreshAuthToken(null);
    }

    public String getAuthToken() {
        return m_accountManager.peekAuthToken(getAccount(), Constants.SMARTERLIST_TOKEN_TYPE);
    }

    public boolean isLoggedIn() {
        return getAccount() != null;
    }

    public Account getAccount() {
        Account[] accounts = m_accountManager.getAccountsByType(Constants.SMARTERLIST_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }


    public void scheduleSmartlistSync() {

        GcmNetworkManager gcm = GcmNetworkManager.getInstance(m_context);

        OneoffTask syncTask = new OneoffTask.Builder()
                .setService(ManualSyncService.class)
                .setExecutionWindow(5, 30)
                .setTag("sync-smartlist")
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                .build();

        gcm.schedule(syncTask);
    }

    public void removeAccount() {

        Ln.v("Removing account");
        //  m_accountManager.removeOnAccountsUpdatedListener(this);

        Account account = getAccount();
        m_accountManager.invalidateAuthToken(Constants.SMARTERLIST_ACCOUNT_TYPE, getAuthToken());
        m_accountManager.clearPassword(account);

        m_accountManager.removeAccount(account, null, null);


    }

}
