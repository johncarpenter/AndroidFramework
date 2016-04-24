/*
 * Copyright (c) 2016. Petrofeed Inc
 *
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Petrofeed Inc and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Petrofeed Inc
 *  and its suppliers and may be covered by U.S. and Foreign Patents,
 *  patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Petrofeed Inc.
 *
 */

package com.twolinessoftware.authentication;

import com.twolinessoftware.model.User;

import rx.Observable;
import rx.observables.BlockingObservable;

/**
 * Created by johncarpenter on 2016-04-19.
 */
public interface UserManager {


    Observable<User> createUser(String uid, User user);

    Observable<User> getMe();

    // Use with caution. This is a blocking call (obviously)
    BlockingObservable<Token> loginBlocking(String email, String password);

    Observable<Token> login(String email, String password);

    Observable<Token> register(String email, String password);

    Observable<Boolean> forgotPassword(String email);

    Observable<Boolean> logout();

    void registerAuthListener(AuthChangedListener listener);

    void unregisterAuthListener(AuthChangedListener listener);



}
