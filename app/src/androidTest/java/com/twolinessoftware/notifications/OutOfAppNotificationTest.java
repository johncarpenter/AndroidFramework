package com.twolinessoftware.notifications;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.activities.RatingActivity;
import com.twolinessoftware.services.SyncNotificationsService;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 *
 * Used to simulated and testing GCM registrations
 *
 */
@RunWith(AndroidJUnit4.class)
public class OutOfAppNotificationTest extends BaseTest {

    private CountDownLatch latch = new CountDownLatch(1);

    private DashboardInfo dashBoardInfo;

    private Instrumentation.ActivityMonitor mMainMonitor;

    @Override
    public void launchActivity() {
        mMainMonitor = getInstrumentation().addMonitor(RatingActivity.class.getName(), null, false);

        TestDataUtil.loginDemoUser();
    }

    @Test
    public void test_displayRatingNotification() {

        latch = new CountDownLatch(1);

        EventBus.getDefault().register(this);

        // MainActivity launches this
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        getNetworkManager().dashboardInfo();

        dashBoardInfo = null;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertNotNull(dashBoardInfo);

        DateTime confirmDateTime = DateTime.now().minusMinutes(120);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1, true, true, true, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());


        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

       /*

            Currently no way to evaluate whether a notification is shown or not. This portion has to be tested manually.

        */

    }

    @Test
    public void test_displayConfirmAppointmentNotification() {

        latch = new CountDownLatch(1);

        EventBus.getDefault().register(this);

        // MainActivity launches this
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        getNetworkManager().dashboardInfo();

        dashBoardInfo = null;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertNotNull(dashBoardInfo);

        DateTime confirmDateTime = DateTime.now().plusHours(47);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1,true, false, false, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());


        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

       /*

            Currently no way to evaluate whether a notification is shown or not. This portion has to be tested manually.

        */

    }

    @Test
    public void test_displayCheckinNotification() {

        latch = new CountDownLatch(1);

        EventBus.getDefault().register(this);

        // MainActivity launches this
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        getNetworkManager().dashboardInfo();

        dashBoardInfo = null;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertNotNull(dashBoardInfo);

        DateTime confirmDateTime = DateTime.now().plusMinutes(14);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1,true, true, false, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());


        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

       /*

            Currently no way to evaluate whether a notification is shown or not. This portion has to be tested manually.

        */

    }



    public void onEventMainThread(DashboardInfoUpdatedEvent event){

        dashBoardInfo = event.info;

        latch.countDown();
    }

    public void onEventMainThread(CommunicationErrorEvent event){
        System.out.println("Comms Error:"+event.getStatusCode());
        latch.countDown();
    }


}
