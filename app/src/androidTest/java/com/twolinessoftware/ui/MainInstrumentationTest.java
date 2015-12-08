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

import de.greenrobot.event.EventBus;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class MainInstrumentationTest extends BaseTest {

    private Instrumentation.ActivityMonitor mMainMonitor;

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class, true, false);

    private CountDownLatch latch;

    private DashboardInfo dashboardInfo;


    @Override
    public void launchActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mMainMonitor = instrumentation.addMonitor(MainActivity.class.getName(), null, false);

        DataStore.getInstance().clear();
        TestDataUtil.loginDemoUser();
        DataStore.getInstance().storeGcmRegistration("blankid", GCMService.getAppVersion(instrumentation.getTargetContext()));

        super.launchActivity();


        // Dashboard
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY));

        // Notifications
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY));


        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(mMainMonitor, 5000);
        org.junit.Assert.assertNotNull(mainActivity);

        EventBus.getDefault().register(this);

        latch = new CountDownLatch(1);

        dashboardInfo = null;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        Assert.assertNotNull(dashboardInfo);
    }

    @After
    public void teardown(){

        getInstrumentation().removeMonitor(mMainMonitor);
        DataStore.getInstance().clear();

        EventBus.getDefault().unregister(this);
    }

    @Test
    public void test_UpcomingAppointmentsAreShown() {

        matchToolbarTitle(getActivity().getString(com.twolinessoftware.R.string.main_fragment_title).toUpperCase(Locale.US));

        delay(500);

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.pointsText)).check(matches(withText(containsString("105"))));

        onView(withRecyclerView(com.twolinessoftware.R.id.listView)).check(hasItemsCount(5));

        ScreenshotUtil.take(getActivity(), "main");

    }


    public void onEventMainThread(DashboardInfoUpdatedEvent event){

        dashboardInfo = event.info;

        latch.countDown();
    }

}
