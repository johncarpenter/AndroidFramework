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
