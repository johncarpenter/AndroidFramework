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

package com.twolinessoftware.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by johncarpenter on 2016-04-18.
 */
public class User {

    @SerializedName("email")
    private String mEmail;

    @SerializedName("mUid")
    private String mUid;

    public User(String email) {
        this.mEmail = email;
    }

    public String getUid() {
        return mUid;
    }

    @Override
    public String toString() {
        return "User{" +
                ", mEmail='" + mEmail + '\'' +
                ", mUid='" + mUid + '\'' +
                '}';
    }

    public void setUid(String uid) {
        mUid = uid;
    }
}
