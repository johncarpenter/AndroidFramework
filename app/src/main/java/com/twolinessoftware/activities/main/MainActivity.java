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

package com.twolinessoftware.activities.main;

import android.os.Bundle;

import com.twolinessoftware.ErrorException;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.BaseNavigationActivity;

/**
 * Created by johncarpenter on 2016-04-20.
 */
public class MainActivity extends BaseNavigationActivity implements MainActivityCallback{

    @Override
    public int getDrawerMenuId() {
        return R.id.menu_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for login
        requiresLogin();
    }


    @Override
    public void onError(ErrorException.Code code) {

    }


}
