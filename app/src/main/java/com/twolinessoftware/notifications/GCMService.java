package com.twolinessoftware.notifications;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.twolinessoftware.network.BaseApiService;
import com.twolinessoftware.storage.DataStore;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 *
 */
public class GCMService extends GooglePlayService {

    private final BaseApiService mApi;

    private GoogleCloudMessaging mGoogleCloudMessaging;

    private String mRegistrationId;

    private static final String GCM_SENDER_ID = "30842492115"; // Production Account Value
   // private static final String GCM_SENDER_ID = "305305541396"; // Test Account Value

    public GCMService(Context context, BaseApiService apiService) {
        super(context);
        mApi = apiService;
    }

    public void register() {
        // Don't bother registering GCM unless an account is active
        if (DataStore.getInstance().getSignedIn() && hasPlayServices()) {
            mGoogleCloudMessaging = GoogleCloudMessaging.getInstance(getContext());

            mRegistrationId = getRegistrationId();

            if (TextUtils.isEmpty(mRegistrationId)) {
                registerGcm();
            }
        }
    }

    private void registerGcm() {

        if (mGoogleCloudMessaging == null) {
            Timber.v("Google Play Services are not enabled for this device");
            return;
        }

        Observable<String> registerGCM = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {

                    InstanceID instanceID = InstanceID.getInstance(getContext());
                    mRegistrationId = instanceID.getToken(GCM_SENDER_ID,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                } catch (IOException e) {
                    subscriber.onError(e);
                }
                subscriber.onNext(mRegistrationId);
            }
        });


        registerGCM
                .flatMap(new Func1<String, Observable<User>>() {
                    @Override
                    public Observable<User> call(String s) {
                        return mApi.postUpdateGCM(new GcmUserWrapper(mRegistrationId));

                      }
                })
                .map(new Func1<User, Boolean>() {
                    @Override
                    public Boolean call(User o) {
                        Timber.v("Storing Registration Id");
                        storeRegistrationId(mRegistrationId);
                        return true;

                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to update GCM: Cause:" + e.getMessage());
                        EventBus.getDefault().post(new GcmUpdatedEvent(false));
                    }

                    @Override
                    public void onNext(Boolean r) {
                        Timber.v("GCM Updated");
                        EventBus.getDefault().post(new GcmUpdatedEvent(true));
                    }
                });


    }

    private void storeRegistrationId(String regId) {

        int appVersion = getAppVersion(getContext());

        mDataStore.storeGcmRegistration(regId, appVersion);

    }


    private String getRegistrationId() {

        String registrationId = mDataStore.getGcmRegistration();

        if (TextUtils.isEmpty(registrationId)) {
            Timber.v("Registration not found.");
            return null;
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = mDataStore.getGcmRegistrationVersion();

        int currentVersion = getAppVersion(getContext());

        if (registeredVersion != currentVersion) {
            Timber.v("App version changed. Requesting new GCM Update");
            return null;
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static class GcmUpdatedEvent{
        public final boolean success;

        public GcmUpdatedEvent(boolean success) {
            this.success = success;
        }

    }

    public static class GcmUserWrapper{

        private GcmWrapper user;


        public GcmUserWrapper(String pushToken) {
            this.user = new GcmWrapper();
            this.user.userId = DataStore.getInstance().getUserId();
            this.user.pushToken = pushToken;
            this.user.pushEnabled = true;
            this.user. pushType = "ANDROID";
        }


    }

    private static class GcmWrapper{
        long userId;
        boolean pushEnabled;
        String pushType;
        String pushToken;
    }

}
