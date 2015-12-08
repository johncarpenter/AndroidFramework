package com.twolinessoftware.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.tsengvn.typekit.TypekitContextWrapper;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.notifications.GoogleServicesManager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BaseActivity extends AppCompatActivity {

	private NetworkManager mNetworkManager;

	private GoogleServicesManager mGoogleServicesManager;

	private DataManager mDataManager;

	@Bind(R.id.toolbar)
	Toolbar mToolbar;

	@Bind(R.id.progress_bar)
	ProgressBar mProgressBar;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNetworkManager = BaseApplication.get(this).getComponent().networkManager();
		mGoogleServicesManager = BaseApplication.get(this).getComponent().googleServicesManager();
		mDataManager = BaseApplication.get(this).getComponent().dataManager();

		setContentView(getContentView());
		ButterKnife.bind(this);

	}

	protected int getContentView() {
		return R.layout.activity_main;
	}


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_global, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id=item.getItemId();

		switch(id) {
            // Global Items
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		if(toolbar != null) {
			setSupportActionBar(toolbar);
			toolbar.setNavigationOnClickListener(v -> onBackPressed());
		}

	}

	protected void setFragment(Fragment fragment) { setFragment(fragment, true); }

	protected void setFragment(Fragment fragment, boolean addToBackstack) {
		if (fragment == null) return;
		FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
		if(addToBackstack) {
			ft.addToBackStack(null);
		}
		//ft.setCustomAnimations(com.twolinessoftware.R.anim.enter_from_right, com.twolinessoftware.R.anim.exit_to_left, com.twolinessoftware.R.anim.enter_from_left, com.twolinessoftware.R.anim.exit_to_right)
		ft.replace(R.id.fragment_container, fragment)
			.commit();
	}

	public void enableBack(boolean enable) {
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(enable);
		ab.setDisplayShowHomeEnabled(enable);
	}


	public NetworkManager getNetworkManager() {
		return mNetworkManager;
	}

	public GoogleServicesManager getGoogleServicesManager() {
		return mGoogleServicesManager;
	}

    public DataManager getDataManager() {
        return mDataManager;
    }

	@Override
	public void onBackPressed() {

		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack();
		} else {
			super.onBackPressed();
		}
	}


	public void clearBackStack() {
		int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
		for (int i = 0; i < backStackCount; i++) {
			int backStackId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
			getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	/**
	 * Default show progress. Override for specific implementations (i.e. dialog/loading bar/etc..)
	 * @param visible
	 */
	public void showProgress(boolean visible) {

		mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

}
