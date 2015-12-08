package com.twolinessoftware.ui;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.ScreenshotUtil;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.activities.MainActivity;
import com.twolinessoftware.activities.ReferActivity;
import com.twolinessoftware.notifications.GCMService;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockResponse;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ReferralInstrumentationTest extends BaseTest {

    private Instrumentation.ActivityMonitor mMainMonitor;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class, true, false);
    private CountDownLatch latch = new CountDownLatch(1);

    private DashboardInfo dashboardInfo;
    private Instrumentation.ActivityMonitor mReferMonitor;


    @Override
    public void launchActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mMainMonitor = instrumentation.addMonitor(MainActivity.class.getName(), null, false);
        mReferMonitor = instrumentation.addMonitor(ReferActivity.class.getName(), null, false);

        DataStore.getInstance().clear();
        TestDataUtil.loginDemoUser();
        DataStore.getInstance().storeGcmRegistration("blankid", GCMService.getAppVersion(instrumentation.getTargetContext()));

        super.launchActivity();


        // Dashboard
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY));

        // Notifications
        //mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY));


        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(mMainMonitor, 5000);
        assertNotNull(mainActivity);
    }

    @After
    public void teardown(){

        getInstrumentation().removeMonitor(mMainMonitor);
        getInstrumentation().removeMonitor(mReferMonitor);
        DataStore.getInstance().clear();
    }

    @Test
    public void test_ReferralValidation() {

        EventBus.getDefault().register(this);

        matchToolbarTitle(getActivity().getString(com.twolinessoftware.R.string.main_fragment_title).toUpperCase(Locale.US));
        latch = new CountDownLatch(1);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        Assert.assertNotNull(dashboardInfo);

        onView(allOf(ViewMatchers.withId(com.twolinessoftware.R.id.friendBtn),isDisplayed())).perform(click());

        ReferActivity referActivity = (ReferActivity) getInstrumentation().waitForMonitorWithTimeout(mReferMonitor, 5000);
        assertNotNull(referActivity);

        ScreenshotUtil.take(getActivity(), "referral");

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.full_name)).perform(clearText(), typeText("full_name"));

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.email)).perform(clearText(), typeText("email@email.com"));

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.phone)).perform(clearText(), typeText("4031234567"));

        closeSoftKeyboard();

        delay(500);

        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.btn_continue))).perform(click());

        delay(500); // Animate to the refer doctor screen

        matchToolbarTitle(getActivity().getString(com.twolinessoftware.R.string.refer_doctor_fragment_title));

        ScreenshotUtil.take(getActivity(), "refer_doctor");

        // 3 Referral Doctors
        onData(is(instanceOf(Doctor.class)))
                .inAdapterView(allOf(ViewMatchers.withId(com.twolinessoftware.R.id.listView), isDisplayed()))
                .atPosition(2)
                .check(matches(isDisplayed()));

        // Test Popup for the error
        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.next))).perform(click());

        delay(500); // wait for error dialog

        onView(ViewMatchers.withText(com.twolinessoftware.R.string.error_select_doctor_to_refer)).check(matches(isDisplayed()));

        onView(allOf(isDisplayed(), ViewMatchers.withText(com.twolinessoftware.R.string.dialog_ok))).perform(click());

        onData(is(instanceOf(Doctor.class)))
                .inAdapterView(allOf(ViewMatchers.withId(com.twolinessoftware.R.id.listView), isDisplayed()))
                .atPosition(2)
                .perform(click());

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.edit_notes)).perform(clearText(), typeText("notes"));

        closeSoftKeyboard();

        delay(500);

        // Submit the request
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.REFERRAL_REPLY));
        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.next))).perform(click());

        waitForCreateReferralConfirmedEvent();

        String text = getActivity().getString(com.twolinessoftware.R.string.notification_thanks_toast,48);
        onView(withText(text)).inRoot(withDecorView(not(getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));

        delay(3500); // clear the toast message
    }


    public void onEventMainThread(DashboardInfoUpdatedEvent event){

        dashboardInfo = event.info;

        latch.countDown();
    }


    public void onEventMainThread(CommunicationErrorEvent event){
        System.out.println("Comms Error:"+event.getStatusCode());
        fail("Comms error:" + event.getStatusCode());
    }


    public void waitForCreateReferralConfirmedEvent(){

        final CountDownLatch latch = new CountDownLatch(1);

        final Object busLock = new Object(){
            public void onEventMainThread(CreateReferralConfirmedEvent event){
                latch.countDown();
            }
        };
        EventBus.getDefault().register(busLock);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            junit.framework.Assert.fail("Interrupted");
        }

        EventBus.getDefault().unregister(busLock);
    }



}
