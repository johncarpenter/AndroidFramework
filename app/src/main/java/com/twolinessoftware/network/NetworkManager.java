package com.twolinessoftware.network;

import android.content.Context;
import android.util.Log;

import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.events.OnCommunicationStatusEvent;
import com.twolinessoftware.events.OnErrorEvent;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.services.SyncNotificationsService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Scheduler;
import timber.log.Timber;

/**
 *
 */
public class NetworkManager {

    // Used for the task management
    public static final String ONEOFFTAG = "NetworkManagerDashboardSync";

    private final Context mContext;
    private final DataManager mDataManager;
    protected BaseApiService mBaseApiService;
    protected Scheduler mScheduler;
    protected EventBus mEventBus;


    protected GoogleServicesManager mGoogleServicesManager;

    @Inject
    public NetworkManager(Context context, BaseApiService baseApiService, Scheduler scheduler, EventBus eventBus, GoogleServicesManager googleServicesManager,
                          DataManager dataManager) {
        mContext = context;
        mBaseApiService = baseApiService;
        mScheduler = scheduler;
        mEventBus = eventBus;
        mGoogleServicesManager = googleServicesManager;
        mDataManager = dataManager;
    }

    public BaseApiService getBaseApiService() {
        return mBaseApiService;
    }

    public Scheduler getScheduler() {
        return mScheduler;
    }

    /**
     * Helper API Methods
     */

    public void authenticate(String email, String pass) {

        Timber.v("Authenticating " + email);

        mEventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.busy));

       /* getBaseApiService()
                .login(new AuthenticateMessage(email, pass))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Completed Authentication");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to login:" + e.getMessage());

                        if(e instanceof CommException){
                            CommException commException = (CommException) e;
                            if(commException.getCode() == 401){
                                mEventBus.post(new UserAuthenticatedEvent());
                            }
                        }else{
                            handleDefaultErrors(e);
                        }

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                    }

                    @Override
                    public void onNext(User user) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        // Store userId and Secret
                        DataStore ds = DataStore.getInstance();
                        ds.setUserIdAndSecret(user.getUserId(), user.getSecretKey());
                        ds.setEmailPass(user.getEmail(), "");
                        ds.setFirstLastName(user.getFirstName(), user.getLastName());
                        ds.setSignedin(true);

                        mEventBus.post(new UserAuthenticatedEvent(user));

                    }
                });*/


    }

    public void register(String email, String pass) {

        Timber.v("Authenticating " + email);

        mEventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.busy));
      /*  getBaseApiService()
                .register(new AuthenticateMessage(email, pass))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Completed Authentication");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to login:" + e.getMessage());

                        if(e instanceof CommException){
                            CommException commException = (CommException) e;
                            if(commException.getCode() == 401){
                                mEventBus.post(new UserAuthenticatedEvent());
                            }
                        }else{
                            handleDefaultErrors(e);
                        }

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                    }

                    @Override
                    public void onNext(User user) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        // Store userId and Secret
                        DataStore ds = DataStore.getInstance();
                        ds.setUserIdAndSecret(user.getUserId(), user.getSecretKey());
                        ds.setEmailPass(user.getEmail(), "");
                        ds.setFirstLastName(user.getFirstName(), user.getLastName());
                        ds.setSignedin(true);

                        mEventBus.post(new UserAuthenticatedEvent(user));

                    }
                });*/


    }


    public void forgotPassword(final String email){

        Timber.v("Sending Forgot Password Link");

        mEventBus.post(new OnCommunicationStatusEvent(OnCommunicationStatusEvent.Status.busy));
      /*  getBaseApiService()
                .resetPassword(new EmailWrapper(email))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<BasicServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("ForgotPassword Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to send forgot password link. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(BasicServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        mEventBus.post(new ForgotPasswordConfirmationEvent(email));
                    }
                });

*/
    }





    public void syncNotificationInformation() {

        OneoffTask toggleTask = new OneoffTask.Builder()
                .setService(SyncNotificationsService.class)
                .setTag(ONEOFFTAG)
                .setExecutionWindow(1, 15) // execute now +/- 5 s
                .setPersisted(false)      // Persist Task Across reboots
                .setRequiredNetwork(Task.NETWORK_STATE_ANY) // Requires network (yes)
                .build();

        GcmNetworkManager.getInstance(mContext).schedule(toggleTask);

    }


    private boolean handleDefaultErrors(Throwable e) {

        CommException commException = CommException.UNKNOWN;

        if (e instanceof CommException) {
            commException = (CommException) e;
        } else {
            Timber.e("Unhandled exception:" + Log.getStackTraceString(e));
        }

        switch (commException.getCode()) {
            case 200:
                return false;
            default:
                 // Broadcast Communication related errors
                mEventBus.post(new OnErrorEvent(OnErrorEvent.Error.COMMUNICATION));
                return true;
        }

    }




}
