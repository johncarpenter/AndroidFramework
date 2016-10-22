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

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class FirebaseUserManager implements UserManager {


    private DatabaseReference mFirebaseDatabase;

    private PreferencesHelper mPreferencesHelper;

    private FirebaseAuth mFirebaseAuth;

    private FirebaseAuthListener mFirebaseAuthListener;

    private WeakReference<AuthChangedListener> mAuthChangedListener;

    @Inject
    public FirebaseUserManager(PreferencesHelper preferencesHelper) {
        this.mFirebaseAuth = FirebaseAuth.getInstance();
        this.mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
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
                mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            FirebaseUser fb = task.getResult().getUser();

                            User user = new User(email);
                            user.setUid(fb.getUid());
                            user.setCreated(DateTime.now());
                            mPreferencesHelper.storeUserProfile(user);

                            final Token token = new Token(String.valueOf(System.currentTimeMillis()), 0);
                            subscriber.onNext(token);
                            subscriber.onCompleted();
                        }else{

                            if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                // Invalid Email
                                subscriber.onError(new ErrorException(ErrorException.Code.INVALID_CREDENTIALS));
                            }else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                // Invalid password
                                subscriber.onError(new ErrorException(ErrorException.Code.INVALID_CREDENTIALS));
                            }else{
                                //?
                                subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                            }
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
                mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {
                            Timber.v("New User Created");
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }else{
                            if(task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                // thrown if the password is not strong enough
                                subscriber.onError(new ErrorException(ErrorException.Code.WEAK_PASSWORD));
                            }else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                //thrown if the email address is malformed
                                subscriber.onError(new ErrorException(ErrorException.Code.EMAIL_MALFORMED));
                            }else if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // thrown if account exists
                                subscriber.onError(new ErrorException(ErrorException.Code.EMAIL_TAKEN));
                            }else{
                                subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                            }
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
                mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }else{
                            Timber.e("Unable to reset password:" + task.getException().getMessage());
                            if(task.getException() instanceof  FirebaseAuthInvalidUserException){
                                subscriber.onError(new ErrorException(ErrorException.Code.NO_EMAIL));

                            }else {
                               subscriber.onError(new ErrorException(ErrorException.Code.GENERIC_ERROR));
                            }
                        }
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
        if(mAuthChangedListener != null) {
            mAuthChangedListener.clear();
            mAuthChangedListener = null;
        }
        disableFirebaseAuthStateChangeMonitor();
    }


    public void monitorFirebaseAuthChanges() {
        Timber.v("Starting Firebase Auth Monitor");
        if ( mFirebaseAuthListener == null ) {
            mFirebaseAuthListener = new FirebaseAuthListener();
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
        }
    }

    public void disableFirebaseAuthStateChangeMonitor() {
        if ( mFirebaseAuthListener != null ) {
            Timber.v("Stopping Firebase Auth Monitor");
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
            mFirebaseAuthListener = null;
        }
    }

    private DatabaseReference getFirebaseForUser(String uid) {
        return mFirebaseDatabase.child((uid == null) ? mPreferencesHelper.getUserProfile().getUid() : uid);
    }

    private class FirebaseAuthListener implements FirebaseAuth.AuthStateListener {

        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            Timber.v("Firebase Auth Status Change:" + firebaseAuth);
            if (mAuthChangedListener != null && firebaseAuth.getCurrentUser() == null) {
                mAuthChangedListener.get().onLoggedOut();
            }
        }
    }


}
