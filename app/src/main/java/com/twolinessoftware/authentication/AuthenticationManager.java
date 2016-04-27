package com.twolinessoftware.authentication;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.Config;
import com.twolinessoftware.PreferencesHelper;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 *
 */
public class AuthenticationManager implements OnAccountsUpdateListener, AuthChangedListener {

    public static final String EXTRA_IS_ADDING = "EXTRA_IS_ADDING";
    public static final String KEY_ACCOUNT_PASSWORD = "KEY_PASSWORD";
    public static final String KEY_AUTH_TOKEN_EXPIRY = "KEY_AUTH_TOKEN_EXPIRY";

    private UserManager mUserManager;
    private PreferencesHelper mPreferencesHelper;
    private Context mContext;
    private AccountManager mAccountManager;

    @Inject
    public AuthenticationManager(Context context, AccountManager accountManager, PreferencesHelper preferencesHelper, UserManager userManager) {
        mContext = context;
        mAccountManager = accountManager;
        mPreferencesHelper = preferencesHelper;
        mUserManager = userManager;
    }


    /**
     * I wanted to make this static, but mocking the test is a nightmare.
     *
     * @param accessToken
     * @param username
     * @param password
     * @return
     */
    public Intent generateAuthIntent(Token accessToken, String username, String password) {
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Config.BASE_ACCOUNT_TYPE);
        data.putString(AccountManager.KEY_AUTHTOKEN, accessToken.getAccessToken());
        data.putLong(AuthenticationManager.KEY_AUTH_TOKEN_EXPIRY, accessToken.getExpiresIn());
        data.putBoolean(AuthenticationManager.EXTRA_IS_ADDING, true);
        data.putString(AuthenticationManager.KEY_ACCOUNT_PASSWORD, password);

        return new Intent().putExtras(data);
    }

    public void completeLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        final Account account = new Account(accountName, Config.BASE_ACCOUNT_TYPE);

        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        String authtokenType = Config.BASE_ACCOUNT_TYPE;

        ContentResolver.setSyncAutomatically(account, Config.BASE_ACCOUNT_TYPE, true);
        ContentResolver.setIsSyncable(account, Config.BASE_ACCOUNT_TYPE, 1);

        mAccountManager.addAccountExplicitly(account, null, null);

        mAccountManager.setAuthToken(account, authtokenType, authtoken);

        String password = intent.getStringExtra(AuthenticationManager.KEY_ACCOUNT_PASSWORD);
        mAccountManager.setPassword(account, password);

        long expirySec = intent.getLongExtra(AuthenticationManager.KEY_AUTH_TOKEN_EXPIRY, TimeUnit.DAYS.toSeconds(1));

        DateTime expiry = DateTime.now().plusSeconds((int) expirySec);

        mAccountManager.setUserData(account, AuthenticationManager.KEY_AUTH_TOKEN_EXPIRY, String.valueOf(expiry.getMillis()));

        mUserManager.registerAuthListener(this);

    }

    public void backgroundLogout() {
        Timber.v("Logging out");

        BaseApplication application = BaseApplication.get(mContext);

        removeAccount()
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(pass -> {
                    mPreferencesHelper.clear();
                    Timber.v("Data Cleaned");
                    mUserManager.unregisterAuthListener(this);
                    mUserManager.logout();

                }, error -> {
                    Timber.e("Unable to logout:Cause:" + Log.getStackTraceString(error));
                    restartMainActivity(application);
                }, () -> restartMainActivity(application));
    }

    public void logout(Activity activity) {
        Timber.v("Logging out");

        removeAccount()
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(pass -> {
                    mPreferencesHelper.clear();
                    Timber.v("Data Cleaned");
                    mUserManager.unregisterAuthListener(this);
                    mUserManager.logout();


                }, error -> {
                    Timber.e("Unable to logout:Cause:" + Log.getStackTraceString(error));
                    activity.finish();
                    restartMainActivity(activity);
                }, () -> {
                    activity.finish();
                    restartMainActivity(activity);
                });
    }

    private void restartMainActivity(Context activity) {
        Timber.v("Restarting Main Activity");

        Intent i = mContext.getPackageManager()
                .getLaunchIntentForPackage(mContext.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }

    public void refreshAuthToken() {

        mAccountManager.getAuthToken(getAccount(), Config.BASE_ACCOUNT_TYPE, null, false, future -> {
            try {
                Bundle accountDetails = future.getResult();
                if ( accountDetails.containsKey(AccountManager.KEY_INTENT) ) {
                    // Credentials failed
                    Timber.v("Unable to login to get token");
                    backgroundLogout();
                }
            } catch (Exception e) {
                Timber.e("Unable to validate token:Not sure what to do:" + Log.getStackTraceString(e));
            }
        }, new Handler());
    }

    public String getAuthToken() {
        Account account = getAccount();
        if ( account == null ) {
            throw new AccountNotFoundException();
        }

        return mAccountManager.peekAuthToken(getAccount(), Config.BASE_ACCOUNT_TYPE);
    }

    public boolean isLoggedIn() {
        return getAccount() != null;
    }

    public Account getAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(Config.BASE_ACCOUNT_TYPE);
        return accounts.length > 0 ? accounts[0] : null;
    }

    public Observable<Boolean> removeAccount() {
        Timber.v("Removing account");

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Account account = getAccount();
                mAccountManager.invalidateAuthToken(Config.BASE_ACCOUNT_TYPE, getAuthToken());
                mAccountManager.removeAccount(account, future -> {
                    try {
                        if ( future.getResult() ) {
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }, null);
            }
        });


    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
    }


    @Override
    public void onLoggedOut() {
        Timber.v("Received Logout from provider, attempting to log back in");
        // Retry
        refreshAuthToken();

    }
}
