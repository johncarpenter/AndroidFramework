package com.twolinessoftware;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.firebase.client.Firebase;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.karumi.dexter.Dexter;
import com.tsengvn.typekit.Typekit;
import com.twolinessoftware.activities.UiModule;
import com.twolinessoftware.authentication.AuthenticationModule;
import com.twolinessoftware.data.DataManagerModule;
import com.twolinessoftware.network.NetworkModule;
import com.twolinessoftware.notifications.GoogleServicesModule;

import net.danlew.android.joda.JodaTimeAndroid;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

public class BaseApplication extends Application{

	private ApplicationComponent mApplicationComponent;

	@Override
	public void onCreate () {
		super.onCreate();

		mApplicationComponent = DaggerApplicationComponent.builder()
				.applicationModule(new ApplicationModule(this))
				.googleServicesModule(new GoogleServicesModule(this))
				.networkModule(new NetworkModule(this))
				.dataManagerModule(new DataManagerModule(this))
				.authenticationModule(new AuthenticationModule(this))
				.uiModule(new UiModule(this))
				.build();

		initializeDatabase();

		initializeLogging();

		initializeLibraries();

		initializeFonts();

		initializeIcons();
	}

	private void initializeIcons() {
		Iconify.with(new MaterialModule());
	}

	private void initializeLibraries() {
		JodaTimeAndroid.init(this);

		Dexter.initialize(this);
	}

	private void initializeDatabase(){
		Firebase.setAndroidContext(this);
	}

	private void initializeFonts() {
		// Add Custom Fonts into the assets directory
		Typekit.getInstance();
				//.addNormal(Typekit.createFromAsset(this, "AmsiPro-SemiBold.otf"))
				//.addBold(Typekit.createFromAsset(this, "AmsiPro-Bold.otf"))
				//.addItalic(Typekit.createFromAsset(this, "AmsiPro-Light.otf"))
				//.addBoldItalic(Typekit.createFromAsset(this, "AmsiPro-Bold.otf"));

	}

	private void initializeLogging(){

		if (BuildConfig.DEBUG){
			Timber.plant(new Timber.DebugTree());
		}else{
			Timber.plant(new ErrorReportingTree());
		}

		EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();

	}


    public static BaseApplication get(Context context) {
        return (BaseApplication) context.getApplicationContext();
    }

	public ApplicationComponent getComponent() {
		return mApplicationComponent;
	}

	// Needed to replace the component with a test specific one
	public void setComponent(ApplicationComponent applicationComponent) {
		mApplicationComponent = applicationComponent;
	}

	/**
	 * Extracts the Timber.e logs and forwards them to the google analytics for tracking
	 */
	private class ErrorReportingTree extends Timber.Tree {
		@Override protected void log(int priority, String tag, String message, Throwable t) {
			if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
				return;
			}
			String concatMessage = "["+t+"] "+message;
			mApplicationComponent.googleServicesManager().getAnalyticsService().trackError(concatMessage,false);
		}
	}

}
