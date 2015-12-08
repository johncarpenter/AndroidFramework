package com.twolinessoftware.ui;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.ScreenshotUtil;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.activities.MainActivity;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LoginInstrumentationTest extends BaseTest {

    private Instrumentation.ActivityMonitor mMainMonitor;

    @Override
    public void launchActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mMainMonitor = instrumentation.addMonitor(MainActivity.class.getName(), null, false);

        DataStore.getInstance().clear();
        super.launchActivity();
    }

    @After
    public void teardown(){

        getInstrumentation().removeMonitor(mMainMonitor);
        DataStore.getInstance().clear();
    }


    @Test
    public void test_ScenarioWelcomePages() {

        delay(500); // allow the fragment to render
        ScreenshotUtil.take(getActivity(), "welcome_page_1");
        delay(100); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.viewPager)).perform(swipeLeft());

        delay(500); // allow the fragment to render
        ScreenshotUtil.take(getActivity(), "welcome_page_2");
        delay(100); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.viewPager)).perform(swipeLeft());

        delay(500); // allow the fragment to render
        ScreenshotUtil.take(getActivity(), "welcome_page_3");
        delay(100); // allow the fragment to render

    }

    @Test
    public void test_ScenarioDeviceValidation() {

        delay(500); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in)).perform(click());

        delay(500); // allow the fragment to render

        ScreenshotUtil.take(getActivity(), "sign_in");

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.email)).perform(clearText(),typeText("email"));

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.password)).perform(clearText(),typeText(""));

        closeSoftKeyboard();
        delay(500);


        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in))).perform(click());

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.email)).check(matches(withValidationError(getActivity().getString(com.twolinessoftware.R.string.validation_email))));
        onView(ViewMatchers.withId(com.twolinessoftware.R.id.password)).check(matches(withValidationError(getActivity().getString(com.twolinessoftware.R.string.validation_password))));

    }

    @Test
    public void test_ScenarioInvalidEmailPassword() {

        delay(500); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in)).perform(click());

        delay(500); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.email)).perform(clearText(), typeText("email@email.com"));

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.password)).perform(clearText(), typeText("password"));

        closeSoftKeyboard();

        delay(500);

        // Sign in response
        mMockWebServer.enqueue(new MockResponse().setResponseCode(401).setBody("{}")); // errors in here?

        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in))).perform(click());

        ScreenshotUtil.take(getActivity(), "sign_in_error");

        onView(ViewMatchers.withText(com.twolinessoftware.R.string.validation_invalid_email_password)).check(matches(isDisplayed()));



    }



    @Test
    public void test_ScenarioLoginPath() {

        delay(500); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in)).perform(click());

        delay(500); // allow the fragment to render


        onView(ViewMatchers.withId(com.twolinessoftware.R.id.email)).perform(clearText(), typeText("email@email.com"));

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.password)).perform(clearText(),typeText("password"));

        closeSoftKeyboard();
        delay(500);

        // Sign in response
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.USER_REPLY));

        // Dashboard
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.DASHBOARD_REPLY));

        // Update GCM response
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.USER_REPLY));


        onView(allOf(isDisplayed(), ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in))).perform(click());

        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(mMainMonitor, 5000);

        assertNotNull(mainActivity);
        matchToolbarTitle(getActivity().getString(com.twolinessoftware.R.string.main_fragment_title).toUpperCase(Locale.US));

    }


    @Test
    public void test_ScenarioForgotPassword() {

        delay(500); // allow the fragment to render


        onView(ViewMatchers.withId(com.twolinessoftware.R.id.btn_sign_in)).perform(click());

        delay(500); // allow the fragment to render

        onView(ViewMatchers.withId(com.twolinessoftware.R.id.text_forgotPassword)).perform(click());

        delay(100); // allow the dialog to render
        ScreenshotUtil.take(getActivity(), "sign_in_forgot_password");

        onView(ViewMatchers.withHint(com.twolinessoftware.R.string.dialog_forgotpassword_email)).perform(clearText(), typeText("email@email.com"));



        closeSoftKeyboard();
        delay(500);

        // Sign in response
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}")); // errors in here?

        onView(allOf(isDisplayed(), ViewMatchers.withText(com.twolinessoftware.R.string.dialog_forgotpassword_reset))).perform(click());

        delay(500);

        ScreenshotUtil.take(getActivity(), "sign_in_forgot_password_2");

        onView(ViewMatchers.withText(com.twolinessoftware.R.string.dialog_forgotpassword_sent)).check(matches(isDisplayed()));

    }



}
