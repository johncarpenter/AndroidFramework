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

package com.twolinessoftware;

/**
 * Used for handling internal error exceptions
 */
public class ErrorException extends Exception {

    public enum Code{
        INVALID_CREDENTIALS,
        EMAIL_TAKEN, NO_DATA_AVAILABLE, FIREBASE_ERROR_GENERIC, GENERIC_ERROR
    }

    private final Code mCode;

    public ErrorException(Code code) {
        this.mCode = code;
    }

    public Code getCode() {
        return mCode;
    }
}
