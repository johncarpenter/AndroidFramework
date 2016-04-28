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

package com.twolinessoftware.authentication;

import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.twolinessoftware.ErrorException;
import com.twolinessoftware.PreferencesHelper;
import com.twolinessoftware.data.FirebaseMonitor;
import com.twolinessoftware.model.User;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by johncarpenter on 2016-04-19.
 */
public class FirebaseUserManager implements UserManager {

    private PreferencesHelper mPreferencesHelper;

    private Firebase mFirebase;

    private FirebaseAuthListener mFirebaseAuthListener;

    private WeakReference<AuthChangedListener> mAuthChangedListener;

    @Inject
    public FirebaseUserManager(Firebase firebase, PreferencesHelper preferencesHelper) {
        this.mFirebase = firebase;
        this.mPreferencesHelper = preferencesHelper;
        monitorFirebaseAuthChanges();
    }

    public Observable<User> createUser(String uid, User user) {

        if ( uid == null ) {
            return Observable.error(new AccountNotLoggedInException());
        }

        Timber.v("Creating New User Profile:" + uid);

        return Observable.create(new Observable.OnSubscribe<User>() {

            @Override
            public void call(Subscriber<? super User> subscriber) {
                // Create User Profile since it doesn't exist
                User user = mPreferencesHelper.getUserProfile();
                Timber.v("Thawing User Profile:" + user.toString());
                getFirebaseForUser(uid).setValue(user);
                mPreferencesHelper.storeUserProfile(user);
                subscriber.onNext(user);
                subscriber.onCompleted();
            }
        });


    }

    @Override
    public Observable<User> getMe() {

        String userId = mPreferencesHelper.getUserUid();

        Timber.v("Getting User Profile:" + userId);

        if ( userId == null ) {
            return Observable.error(new AccountNotLoggedInException());
        }

        return new FirebaseMonitor<User>(User.class)
                .once(getFirebaseForUser(userId))
                .subscribeOn(Schedulers.io())
                .flatMap(userFirebaseChange -> {
                    if ( userFirebaseChange.getValue() != null ) {
                        Timber.v("Updated user information: " + userFirebaseChange.getValue().toString());
                        mPreferencesHelper.storeUserProfile(userFirebaseChange.getValue());
                        return Observable.just(userFirebaseChange.getValue());
                    } else {
                        Timber.e("No user profile available, creating new profile");
                        return createUser(userId, mPreferencesHelper.getUserProfile());
                    }
                });
    }

    @Override
    public BlockingObservable<Token> loginBlocking(String email, String password) {
        return login(email, password).toBlocking();
    }

    @Override
    public Observable<Token> login(final String email, final String password) {
        Timber.v("Logging in user:" + email);

        return Observable.create(new Observable.OnSubscribe<Token>() {
            @Override
            public void call(Subscriber<? super Token> subscriber) {
                mFirebase.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {

                        User user = new User(email);
                        user.setUid(authData.getUid());
                        user.setCreated(DateTime.now());
                        mPreferencesHelper.storeUserProfile(user);

                        final Token token = new Token(authData.getToken(), authData.getExpires());
                        subscriber.onNext(token);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {

                        switch (firebaseError.getCode()) {
                            case FirebaseError.INVALID_CREDENTIALS:
                            case FirebaseError.INVALID_EMAIL:
                            case FirebaseError.INVALID_PASSWORD:
                                subscriber.onError(new ErrorException(ErrorException.Code.INVALID_CREDENTIALS));
                                return;
                            default:
                                subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                                return;
                        }
                    }
                });
            }
        });
    }

    @Override
    public Observable<Token> register(String email, String password) {

        return createFirebaseUser(email, password)
                .flatMap(pass -> login(email, password));

    }

    private Observable<Boolean> createFirebaseUser(String email, String password) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                mFirebase.createUser(email, password, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Timber.v("New User Created");
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Timber.e("Error creating user: " + Log.getStackTraceString(firebaseError.toException()));
                        switch (firebaseError.getCode()) {
                            case FirebaseError.EMAIL_TAKEN:
                                subscriber.onError(new ErrorException(ErrorException.Code.EMAIL_TAKEN));
                                break;
                            default:
                                subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                        }
                    }
                });
            }
        });

    }

    @Override
    public Observable<Boolean> forgotPassword(String email) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                mFirebase.resetPassword(email, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Timber.e("Unable to reset password:" + firebaseError.getMessage());
                        subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                    }
                });
            }
        });

    }

    @Override
    public Observable<Boolean> logout() {
        disableFirebaseAuthStateChangeMonitor();
        return Observable.just(true);

    }

    @Override
    public void registerAuthListener(AuthChangedListener listener) {
        mAuthChangedListener = new WeakReference<>(listener);
        monitorFirebaseAuthChanges();
    }

    @Override
    public void unregisterAuthListener(AuthChangedListener listener) {
        mAuthChangedListener.clear();
        mAuthChangedListener = null;
        disableFirebaseAuthStateChangeMonitor();
    }


    public void monitorFirebaseAuthChanges() {
        Timber.v("Starting Firebase Auth Monitor");
        if ( mFirebaseAuthListener == null ) {
            mFirebaseAuthListener = new FirebaseAuthListener();
            mFirebase.addAuthStateListener(mFirebaseAuthListener);
        }
    }

    public void disableFirebaseAuthStateChangeMonitor() {
        if ( mFirebaseAuthListener != null ) {
            Timber.v("Stopping Firebase Auth Monitor");
            mFirebase.removeAuthStateListener(mFirebaseAuthListener);
            mFirebaseAuthListener = null;
        }
    }

    private Firebase getFirebaseForUser(String uid) {
        return mFirebase.child("users").child((uid == null) ? mPreferencesHelper.getUserProfile().getUid() : uid);
    }

    private class FirebaseAuthListener implements Firebase.AuthStateListener {
        @Override
        public void onAuthStateChanged(AuthData authData) {
            Timber.v("Firebase Auth Status Change:" + authData);
            if ( authData == null && mAuthChangedListener != null ) {
                mAuthChangedListener.get().onLoggedOut();
            }
        }
    }
}
