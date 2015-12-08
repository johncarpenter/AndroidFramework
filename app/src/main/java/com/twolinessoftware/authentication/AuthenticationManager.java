package com.twolinessoftware.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.twolinessoftware.Constants;
import com.twolinessoftware.activities.LoginActivity;
import com.twolinessoftware.events.OnLogoutEvent;
import com.twolinessoftware.services.SyncNotificationsService;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 *
 */
public class AuthenticationManager implements OnAccountsUpdateListener {

    private EventBus mEventBus;

    private Context mContext;

    private AccountManager mAccountManager;

    @Inject
    public AuthenticationManager(Context context, AccountManager accountManager){
        mContext = context;
        mAccountManager = accountManager;
        mAccountManager.addOnAccountsUpdatedListener(this, new Handler(), false);
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


    public static final Intent generateAuthIntent(Token accessToken, String username) {

        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.BASE_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, accessToken.accessToken);
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
        return mAccountManager.getPassword(getAccount());
    }


    public void invalidateToken() {
        if (getAuthToken() != null) {
            Timber.e("Authentication Token Invalidated");
            mAccountManager.invalidateAuthToken(Constants.BASE_ACCOUNT_TYPE, getAuthToken());
        }
    }

    public void refreshAuthToken(final TokenRefreshListener listener) {

        if (getAccount() != null) {

            final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(getAccount(), Constants.BASE_ACCOUNT_TYPE, null, false, null, null);

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
                            Timber.e("Unable to retrieve token:null");
                            if (listener != null) {
                                listener.onTokenError();
                            }
                        }
                    }, error -> {
                        Timber.e("Unable to retrieve token");
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
        return mAccountManager.peekAuthToken(getAccount(), Constants.BASE_ACCOUNT_TYPE);
    }

    public boolean isLoggedIn() {
        return getAccount() != null;
    }

    public Account getAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.BASE_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }


    public void scheduleSync() {

        GcmNetworkManager gcm = GcmNetworkManager.getInstance(mContext);

        OneoffTask syncTask = new OneoffTask.Builder()
                .setService(SyncNotificationsService.class)
                .setExecutionWindow(5, 30)
                .setTag("sync-app")
                .setUpdateCurrent(true)
                .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                .build();

        gcm.schedule(syncTask);
    }

    public void removeAccount() {

        Timber.v("Removing account");
        mAccountManager.removeOnAccountsUpdatedListener(this);

        Account account = getAccount();
        mAccountManager.invalidateAuthToken(Constants.BASE_ACCOUNT_TYPE, getAuthToken());
        mAccountManager.clearPassword(account);

        mAccountManager.removeAccount(account,null,null);


    }

}
