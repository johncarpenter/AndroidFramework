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

package com.twolinessoftware.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.twolinessoftware.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

@SuppressLint("Registered")
public abstract class BaseNavigationActivity extends BaseActivity {

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.navigation_drawer)
    NavigationView mNavigationView;


    @Bind(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;

    private View mHeader;

    public abstract int getDrawerMenuId();

    @Override
    public int getContentView() {
        return R.layout.activity_base_drawer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavigationDrawer();

    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                getToolbar(),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                updateHeaderInfo();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(menuItem -> {
            int menuItemId = menuItem.getItemId();
            Timber.v("MenuID: " + menuItemId + ", currentActivityMenuID: " + getDrawerMenuId());

            boolean differentMenuSelection = menuItemId != getDrawerMenuId();

            switch (menuItemId) {
                case R.id.menu_about:
                    if ( differentMenuSelection ) {
                       //
                    }
                    mDrawerLayout.closeDrawers();
                    expandToolbar();
                    return true;
                case R.id.menu_settings:
                    if ( differentMenuSelection ) {
                         //
                    }
                    mDrawerLayout.closeDrawers();
                    return true;
                case R.id.menu_logout:
                    if ( differentMenuSelection ) {
                        //
                    }
                    mDrawerLayout.closeDrawers();
                    return true;
            }

            return false;

        });

        setupMenuIcons();
        setupHeader();
    }

    protected void updateHeaderInfo(){};

    private void setupHeader() {
        // Set the header
        mHeader = mNavigationView.inflateHeaderView(R.layout.item_nav_drawer_header);
        updateHeaderInfo();
    }

    private void setupMenuIcons() {
        Menu menu = mNavigationView.getMenu();
        int mActiveMenuId = getDrawerMenuId();
        setMenuItemIcon(menu, R.id.menu_about, MaterialIcons.md_info, mActiveMenuId == R.id.menu_about);
        setMenuItemIcon(menu, R.id.menu_logout, MaterialIcons.md_cancel, mActiveMenuId == R.id.menu_logout);
        setMenuItemIcon(menu, R.id.menu_settings, MaterialIcons.md_settings, mActiveMenuId == R.id.menu_settings);
    }

    private void setMenuItemIcon(Menu menu, @IdRes int menuResourceId, Icon iconName, boolean checked) {

        MenuItem item = menu.findItem(menuResourceId);
        Timber.v("setup menu item icon: " + item + ", checked: " + checked);
        item.setChecked(checked);
        item.setIcon(new IconDrawable(BaseNavigationActivity.this, iconName).colorRes(checked ? R.color.pal_grey_4 : R.color.pal_white));
    }

    public void collapseToolbar() {
        mAppBarLayout.setExpanded(false, false);
    }

    public void expandToolbar() {
        mAppBarLayout.setExpanded(true, false);
    }

}
