package com.twolinessoftware.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tsengvn.typekit.TypekitContextWrapper;
import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.R;
import com.twolinessoftware.activities.login.LoginActivity;
import com.twolinessoftware.authentication.AuthenticationManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class BaseActivity extends AppCompatActivity {


    private static final int REQUEST_LOGIN = 12;
    @Bind(R.id.toolbar)
	Toolbar mToolbar;

	@Bind(R.id.progress_bar)
	ProgressBar mProgressBar;

	@Bind(R.id.coordinator_layout)
	CoordinatorLayout mCoordinatorLayout;

	private AuthenticationManager mAuthenticationManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getContentView());

		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);
		mToolbar.setNavigationOnClickListener(v -> onBackPressed());

		mAuthenticationManager = BaseApplication.get(this).getComponent().authenticationManager();

	}

	public int getContentView() {
		return R.layout.activity_base;
	}


	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
	}


	public Toolbar getToolbar() {
		return mToolbar;
	}

	protected void setFragment(Fragment fragment) {
		setFragment(fragment, true);
	}

	public void setFragmentAtTop(Fragment fragment) {
		clearBackStack();
		setFragment(fragment, false);
	}


	public Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
	}

	public void setFragment(Fragment fragment, boolean addToBackstack) {
		if ( fragment == null ) return;

		fragment.setRetainInstance(true);


		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if ( addToBackstack ) {
			ft.addToBackStack(null);
		}
		ft.replace(R.id.fragment_container, fragment)
				.commitAllowingStateLoss();

		getSupportFragmentManager().executePendingTransactions();
	}

	public void enableBack(boolean enable) {
		ActionBar actionBar = getSupportActionBar();
		if ( actionBar != null ) {
			actionBar.setDisplayHomeAsUpEnabled(enable);
			actionBar.setDisplayShowHomeEnabled(enable);
		}
	}

	@Override
	public void onBackPressed() {

		FragmentManager fm = getSupportFragmentManager();
		if ( fm.getBackStackEntryCount() > 0 ) {
			fm.popBackStack();
		} else {
			super.onBackPressed();
		}
	}


	public void clearBackStack() {
		int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
		for ( int i = 0; i < backStackCount; i++ ) {
			int backStackId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
			getSupportFragmentManager().popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}

	/**
	 * Default show progress. Override for specific implementations (i.e. dialog/loading bar/etc..)
	 *
	 * @param visible
	 */
	public void showProgress(boolean visible) {
		mProgressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}


	public void showDialog(BaseDialogFragment baseDialogFragment, String tag) {
		if ( baseDialogFragment == null ) return;

		baseDialogFragment.setRetainInstance(true);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(tag);
		baseDialogFragment.show(ft, tag);
		getSupportFragmentManager().executePendingTransactions();
	}


	protected void startActivityAtTop(Class<? extends BaseActivity> activityClass) {
		Intent intent = new Intent(BaseActivity.this, activityClass);

		TaskStackBuilder.create(this).addParentStack(activityClass)
				.addNextIntentWithParentStack(intent)
				.startActivities();
		finish();
	}

	@NonNull
	public Snackbar makeSnackbar( @NonNull CharSequence text, int duration) {
		Snackbar snackBarView = Snackbar.make(mCoordinatorLayout, text, duration);
		snackBarView.setActionTextColor(ContextCompat.getColor(BaseActivity.this, R.color.pal_blue));

		TextView tv = (TextView) snackBarView.getView().findViewById(android.support.design.R.id.snackbar_text);
		tv.setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.pal_white));
		return snackBarView;
	}

	public void requiresLogin(){
		if(!mAuthenticationManager.isLoggedIn()){
			Timber.e("This activity requires a login context");
			startActivityForResult(new Intent(BaseActivity.this,LoginActivity.class),REQUEST_LOGIN);
		}
	}

    public void hideToolbar() {
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQUEST_LOGIN ) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    finish();
                    break;
                case RESULT_OK:
                    Timber.v("Returning from login");
                    break;
            }
        }
    }


}
