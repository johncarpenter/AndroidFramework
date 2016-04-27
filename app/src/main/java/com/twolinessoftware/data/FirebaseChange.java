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

package com.twolinessoftware.data;

/**
 * Created by johncarpenter on 2016-01-26.
 */
public class FirebaseChange<T> {


    public enum State {
        Added, Changed, Removed, Empty, Data
    }

    private State mState;
    private T mValue;

    public FirebaseChange(State state, T value) {
        mState = state;
        mValue = value;
    }

    public State getState() {
        return mState;
    }

    public T getValue() {
        return mValue;
    }

}
