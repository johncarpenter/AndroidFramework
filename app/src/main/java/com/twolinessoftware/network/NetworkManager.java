package com.twolinessoftware.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.messages.AuthenticateMessage;
import com.twolinessoftware.network.messages.BaseServerMessage;
import com.twolinessoftware.network.messages.BasicServerResponse;
import com.twolinessoftware.network.messages.PointsServerResponse;
import com.twolinessoftware.network.messages.ReferralServerResponse;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.services.SyncNotificationsService;
import com.twolinessoftware.storage.DataStore;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postAuthenticate(new AuthenticateMessage(email, pass))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<User>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Completed Authentication");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to login:" + e.getMessage());

                        if(e instanceof ApiException){
                            ApiException apiException = (ApiException) e;
                            if(apiException.getCode() == 401){
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
                });


    }


    public void forgotPassword(final String email){

        Timber.v("Sending Forgot Password Link");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postForgotPassword(new EmailWrapper(email))
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


    }


    public void checkin(final Appointment appointment){

        Timber.v("Sending Checkin Confirmation");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postCheckIn(new AppointmentWrapper(appointment.getId()))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<BasicServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Checkin Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to checkin to appointment. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(BasicServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        mEventBus.post(new CheckInConfirmationEvent(appointment));

                        showAppreciadoToast(49); // Where do we get the points from?

                        // @todo Sync without using cache
                        dashboardInfo();
                    }
                });


    }


    public void dashboardInfo() {

        Timber.v("Syncing Dashboard Information ");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postDashboardInfo(new BaseServerMessage())
                .subscribeOn(Schedulers.io())
                .map(new Func1<DashboardInfo, DashboardInfo>() {
                    @Override
                    public DashboardInfo call(DashboardInfo dashboardInfo) {

                        mDataManager.clear();
                        mDataManager.getDatabase().beginTransaction();

                        mDataManager.setUsers(dashboardInfo.getUsers());
                        mDataManager.setAppointments(dashboardInfo.getAppointments());
                        mDataManager.setClinics(dashboardInfo.getClinics());
                        mDataManager.setDoctors(dashboardInfo.getDoctors());

                        mDataManager.getDatabase().setTransactionSuccessful();
                        mDataManager.getDatabase().endTransaction();

                        DataStore.getInstance().setPoints(dashboardInfo.getPoints());

                        mEventBus.post(new DashboardInfoUpdatedEvent(dashboardInfo));

                        return dashboardInfo;
                    }
                })
                .subscribe(new Subscriber<DashboardInfo>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Finished updating Dashboard Information");
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to Update Dashboard Information:" + Log.getStackTraceString(e));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle dashboard specific errors
                        }
                    }

                    @Override
                    public void onNext(DashboardInfo appointmentModel) {

                    }
                });

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


    public void rateAppointment(final Appointment appointment,final int rating,final String comments,final boolean contactRequested, final boolean anonymous) {

        Timber.v("Sending Rate Appointment");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postRateAppointment(new RatingWrapper(appointment.getId(), rating, comments, contactRequested, anonymous))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<PointsServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Appointment Rating Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to rate appointment. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(PointsServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        if (rating != -1) {
                            mEventBus.post(new AppointmentRatedEvent(appointment));

                            if (response.getPointsChange() > 0) {
                                showAppreciadoToast(response.getPointsChange());
                            }
                        }

                        // @todo Sync without using cache
                        dashboardInfo();
                    }
                });
    }

    public void confirmAppointment(final Appointment appointment) {

        Timber.v("Sending Appointment Confirmation");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postConfirmAppointment(new AppointmentWrapper(appointment.getId()))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<BasicServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Appointment Confirmation Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to confirm appointment. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(BasicServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        mEventBus.post(new AppointmentConfirmedEvent(appointment));

                        showAppreciadoToast(50); // Where do we get the points from?

                        // @todo Sync without using cache
                        dashboardInfo();
                    }
                });
    }


    public void acceptAppointment(final Appointment appointment) {

        Timber.v("Sending Appointment Approval");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postAcceptAppointment(new AppointmentWrapper(appointment.getId()))
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<BasicServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Appointment Approval Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to Approve appointment. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(BasicServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        mEventBus.post(new AppointmentApprovedEvent(appointment));

                        showAppreciadoToast(47); // Where do we get the points from?

                        dashboardInfo();
                    }
                });
    }




    public void createReferral(Doctor doctor, Referral referral){

        Timber.v("Sending Referral");

        final long clinicId = doctor.getClinicId();
        final long practionerId = doctor.getId();

        final String email = referral.getEmail();
        final String telephone = referral.getTelephone();
        final boolean sendReferral = true;

        Observable<CryptUtils.AesWrapper> aesWrapperObservable = CryptUtils.calculateRsaObservable(GsonUtil.buildGsonAdapter().toJson(referral));

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        aesWrapperObservable
                .flatMap(new Func1<CryptUtils.AesWrapper, Observable<ReferralServerResponse>>() {
                    @Override
                    public Observable<ReferralServerResponse> call(CryptUtils.AesWrapper aesWrapper) {
                        return getBaseApiService()
                                .postCreateReferral(new ReferralWrapper(clinicId, practionerId, telephone, sendReferral, aesWrapper));
                    }
                })
                .subscribeOn(getScheduler())
                .subscribe(new Subscriber<ReferralServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Referral Confirmation Finished");
                    }

                    @Override
                    public void onError(Throwable e) {

                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                        if (!handleDefaultErrors(e)) {
                            // @todo handle timeouts and errors. Do we reschedule this request?
                            Timber.e("Unable to Create Referral. Error:" + Log.getStackTraceString(e));
                        }
                    }

                    @Override
                    public void onNext(ReferralServerResponse response) {
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));

                        mEventBus.post(new CreateReferralConfirmedEvent(response.getReferralId()));

                        showAppreciadoToast(48); // Where do we get the points from?
                    }
                });

    }


    public void showAppreciadoToast(final int points) {

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(mContext, mContext.getString(com.twolinessoftware.R.string.notification_thanks_toast, points), Toast.LENGTH_LONG);
                toast.show();

            }
        });
    }


    private boolean handleDefaultErrors(Throwable e) {

        ApiException apiException = ApiException.UNKNOWN;

        if (e instanceof ApiException) {
            apiException = (ApiException) e;
        } else {
            Timber.e("Unhandled exception:" + Log.getStackTraceString(e));
        }

        switch (apiException.getCode()) {
            case 200:
                return false;
            default:
                 // Broadcast Communication related errors
                mEventBus.post(new CommunicationErrorEvent(apiException.getCode(), apiException.getErrors()));
                return true;
        }

    }


    public static class EmailWrapper {
        private String email;

        public EmailWrapper(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class PayloadWrapper {
        private String payload;

        public PayloadWrapper(String payload) {
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }
    }


    public static class AppointmentWrapper {
        private long appointmentId;

        public AppointmentWrapper(long id) {
            this.appointmentId = id;
        }

        public long getAppointmentId() {
            return appointmentId;
        }
    }

    public static class RatingWrapper {
        private long appointmentId;
        private int rating;
        private String comments;
        private boolean contactRequested;
        private boolean anonymous;

        public RatingWrapper(long appointmentId, int rating, String comments, boolean contactRequested, boolean anonymous) {
            this.appointmentId = appointmentId;
            this.rating = rating;
            this.comments = comments;
            this.contactRequested = contactRequested;
            this.anonymous = anonymous;
        }
    }

    public static class ReferralWrapper{

        private long clinicId;
        private long practionerId;
        private String email;
        private String telephone;
        private boolean sendReferral;
        private String key;
        private String iv;
        private String body;

        public ReferralWrapper(long clinicId, long practionerId, String telephone, boolean sendReferral, CryptUtils.AesWrapper aesWrapper) {
            this.clinicId = clinicId;
            this.practionerId = practionerId;
            //this.email = email;
          this.telephone = telephone;
            this.sendReferral = sendReferral;
            this.key = aesWrapper.key;
            this.iv = aesWrapper.iv;
            this.body = aesWrapper.message;
        }
    }


    /**
     * Testing code.
     * @param json
     */
    public void testSendPushNotification(String json) {

        Timber.v("Sending Test Push Notification");

        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.busy));

        getBaseApiService()
                .postTestPushMessage(new PayloadWrapper(json))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<BasicServerResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Finished Calling Test Push");
                        mEventBus.post(new CommunicationStatusEvent(CommunicationStatusEvent.Status.idle));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Unable to Send Test Push:" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(BasicServerResponse basicServerResponse) {

                    }
                });

    }

}
