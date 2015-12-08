package com.twolinessoftware.notifications;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.R;
import com.twolinessoftware.ScreenshotUtil;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.activities.MainActivity;
import com.twolinessoftware.activities.RatingActivity;
import com.twolinessoftware.services.SyncNotificationsService;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.not;

/**
 *
 * Used to simulated and testing GCM registrations
 *
 */
@RunWith(AndroidJUnit4.class)
public class InApplicationNotificationTest extends BaseTest {

    private CountDownLatch latch = new CountDownLatch(1);

    private DashboardInfo dashBoardInfo;

    private Instrumentation.ActivityMonitor mMainMonitor;
    private Instrumentation.ActivityMonitor mRatingMonitor;

    private MainActivity mainActivity;

    @Override
    public void launchActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mMainMonitor = instrumentation.addMonitor(MainActivity.class.getName(), null, false);
        mRatingMonitor = instrumentation.addMonitor(RatingActivity.class.getName(), null, false);

        DataStore.getInstance().clear();
        TestDataUtil.loginDemoUser();
        DataStore.getInstance().storeGcmRegistration("blankid", GCMService.getAppVersion(instrumentation.getTargetContext()));

        super.launchActivity();

        // Dashboard
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        // Notifications
       // mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));


        mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(mMainMonitor, 5000);
        Assert.assertNotNull(mainActivity);

        EventBus.getDefault().register(this);

        latch = new CountDownLatch(1);

        dashBoardInfo = null;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertNotNull(dashBoardInfo);

    }

    @After
    public void teardown(){

        getInstrumentation().removeMonitor(mMainMonitor);
        getInstrumentation().removeMonitor(mRatingMonitor);
        DataStore.getInstance().clear();
        getDataManager().clear();
        EventBus.getDefault().unregister(this);
    }

    @Test
    public void test_ConfirmAppointmentNotificationShown() {

        DateTime confirmDateTime = DateTime.now().plusHours(47);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1,true,  false, false, false);


        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());

        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

        waitForTestingEvent(TestingStatusEvent.DIALOG_DISPLAYED);


        onView(withText(R.string.notification_confirm_title)).check(matches(isDisplayed()));

        ScreenshotUtil.take(getActivity(), "notification_confirm");

        // Run through the confirm message
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.CONFIRM_REPLY));
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        onView(withText(R.string.notification_confirm_button)).perform(click());

        delay(500);

        String text = getActivity().getString(R.string.notification_thanks_toast,50);

        onView(withText(text)).inRoot(withDecorView(not(getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));


        delay(Toast.LENGTH_LONG); // Added to wait for the toast to clear for the next test to pass

    }

    @Test
    public void test_ConfirmAppointmentNotificationAlreadyShown() {

        DateTime confirmDateTime = DateTime.now().plusHours(47);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1,true,  true, false, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());

        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

        onView(withText(R.string.notification_confirm_title)).check(doesNotExist());

    }


    @Test
    public void test_CheckInNotificationShown() {

        DateTime confirmDateTime = DateTime.now().plusMinutes(14);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1, true, true, false, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());

        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

        waitForTestingEvent(TestingStatusEvent.DIALOG_DISPLAYED);

        onView(withText(R.string.notification_checkin_title)).check(matches(isDisplayed()));
        ScreenshotUtil.take(getActivity(), "notification_checkin");
        //
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.CONFIRM_REPLY));
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        onView(withText(R.string.notification_checkin_button)).perform(click());

        delay(1000);

        String text = getActivity().getString(R.string.notification_thanks_toast,49);

        onView(withText(text)).inRoot(withDecorView(not(getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));

        delay(Toast.LENGTH_LONG); // Added to wait for the toast to clear for the next test to pass

    }

    @Test
    public void test_RatingNotificationShown() {


        DateTime confirmDateTime = DateTime.now().minusMinutes(120);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1,true,  true, true, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());

        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

        waitForTestingEvent(TestingStatusEvent.DIALOG_DISPLAYED);

        onView(withText(R.string.notification_rating_title)).check(matches(isDisplayed()));
        ScreenshotUtil.take(getActivity(), "notification_rating");

        // @todo Run through the negative confirm message

        // Positive Reply
        onView(withText(R.string.notification_rating_button_yes)).perform(click());

        RatingActivity mainActivity = (RatingActivity) getInstrumentation().waitForMonitorWithTimeout(mRatingMonitor, 5000);
        Assert.assertNotNull(mainActivity);

        matchToolbarTitle(getActivity().getString(R.string.rating_screen_title).toUpperCase(Locale.US));

        onView(withId(R.id.ratingBar)).perform(new SetRating());

        onView(withId(R.id.edit_notes)).perform(clearText(), typeText("notes"));
        closeSoftKeyboard();
        delay(500);

        onView(withId(R.id.checkAnonymous)).perform(click());

        onView(withId(R.id.checkContact)).perform(click());

        ScreenshotUtil.take(getActivity(), "rating");

        // TODO test the rating submission?
    }

    @Test
    public void test_ApproveNotificationShown() {

        DateTime confirmDateTime = DateTime.now().plusDays(14);
        Appointment testAppointment = new Appointment(1,3, confirmDateTime, 1, 1, false, false, false, false);

        Doctor doctor = getDataManager().getDoctor(testAppointment.getDoctorId());

        User user = getDataManager().getUser(testAppointment.getPatientId());

        SyncNotificationsService.scheduleNotificationForAppointment(getInstrumentation().getTargetContext(), testAppointment, user, doctor);

        waitForTestingEvent(TestingStatusEvent.DIALOG_DISPLAYED);

        onView(withText(R.string.notification_confirm_title)).check(matches(isDisplayed()));

        ScreenshotUtil.take(getActivity(), "notification_approval");
        delay(500);

        //
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.APPROVAL_REPLY));
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY_NO_APPOINTMENTS));

        onView(withText(R.string.notification_approve_button)).check(matches(isDisplayed())).perform(click());

        String text = getActivity().getString(R.string.notification_thanks_toast,47);

        onView(withText(text)).inRoot(withDecorView(not(getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));

        delay(3500); // Added to wait for the toast to clear for the next test to pass
    }



    public void onEventMainThread(DashboardInfoUpdatedEvent event){

        dashBoardInfo = event.info;

        latch.countDown();
    }

    public void onEventMainThread(CommunicationErrorEvent event){
        System.out.println("Comms Error:"+event.getStatusCode());
        fail("Comms:"+event.getStatusCode());
    }


    public final class SetRating implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            Matcher <View> isRatingBarConstraint = isAssignableFrom(RatingBar.class);
            return isRatingBarConstraint;
        }

        @Override
        public String getDescription() {
            return "Custom view action to set rating.";
        }

        @Override
        public void perform(UiController uiController, View view) {
            RatingBar ratingBar = (RatingBar) view;
            ratingBar.setRating(3);
        }
    }


}
