/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.authentication;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.Injector;
import com.twolinessoftware.smarterlist.activity.LoginActivity;
import com.twolinessoftware.smarterlist.model.Token;
import com.twolinessoftware.smarterlist.util.Ln;

import javax.inject.Inject;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator in onBind()
 */
public class AccountAuthenticatorService extends Service {

    @Inject
    AccountManager m_accountManager;

    private static AccountAuthenticatorImpl sAccountAuthenticator = null;

    public AccountAuthenticatorService() {
        super();
        BaseApplication.get(this).getComponent().inject(this);
    }

    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT))
            ret = getAuthenticator().getIBinder();
        return ret;
    }

    private AccountAuthenticatorImpl getAuthenticator() {
        if (sAccountAuthenticator == null)
            sAccountAuthenticator = new AccountAuthenticatorImpl(this);
        return sAccountAuthenticator;
    }

    private  class AccountAuthenticatorImpl extends AbstractAccountAuthenticator {
        private Context mContext;


        public AccountAuthenticatorImpl(Context context) {
            super(context);
            mContext = context;
       }

        /*
         *  The user has requested to add a new account to the system.  We return an intent that will launch our login screen if the user has not logged in yet,
         *  otherwise our activity will just pass the user's credentials on to the account manager.
         */
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            Bundle reply = new Bundle();

            Intent i = new Intent(mContext, LoginActivity.class);
            i.putExtra(LoginActivity.EXTRA_IS_ADDING,true);
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            reply.putParcelable(AccountManager.KEY_INTENT, i);

            return reply;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

            // Extract the username and password from the Account Manager, and ask
           String authToken = m_accountManager.peekAuthToken(account, authTokenType);

            // Lets give another try to authenticate the user. We will resend the user/password
            if (TextUtils.isEmpty(authToken)) {
                Ln.v("Empty auth token refreshing from server");
                authToken = retrieveAuthTokenFromServer(account);
            }else{
                // Check if the token is expired
                long expiry = Long.valueOf(m_accountManager.getUserData(account, Constants.USER_DATA_AUTH_TOKEN_EXPIRY));
                if(System.currentTimeMillis() > expiry){
                    Ln.v("Expired auth token refreshing from server");
                    authToken = retrieveAuthTokenFromServer(account);
                }
            }


            // If we get an authToken - we return it
            if (!TextUtils.isEmpty(authToken)) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);

                return result;
            }


            // If we get here, then we couldn't access the user's password - so we
            // need to re-prompt them for their credentials. We do that by creating
            // an intent to display our AuthenticatorActivity.
            Bundle reply = new Bundle();

            Intent i = new Intent(mContext, LoginActivity.class);
            i.putExtra(LoginActivity.EXTRA_IS_ADDING,true);
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            reply.putParcelable(AccountManager.KEY_INTENT, i);

            return reply;

        }

        private String retrieveAuthTokenFromServer(Account account){

            String authToken = null;

            final String password = m_accountManager.getPassword(account);
            if (password != null) {

                Token token = m_accountService.getAccessToken(account.name,password);

                if(token != null){
                    authToken = token.accessToken;

                    long expiry = System.currentTimeMillis() + (token.expiresIn*1000);
                    m_accountManager.setUserData(account,Constants.USER_DATA_AUTH_TOKEN_EXPIRY, String.valueOf(expiry));
                }

                ContentResolver.setSyncAutomatically(account, Constants.SMARTERLIST_ACCOUNT_TYPE, true);

            }
            return authToken;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
            return null;
        }

    }
}