package com.twolinessoftware.comms;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.activities.WelcomeActivity;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.storage.DataStore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;


/**
 * The Base Test injects the Test Modules in addition to the mockwebserver
 */
@RunWith(AndroidJUnit4.class)
public abstract class OnlineBaseTest {

    private Instrumentation mInstrumentation;
    private BaseApplication mApplication;

    public Instrumentation getInstrumentation() {
        return mInstrumentation;
    }

    @Rule
    public ActivityTestRule<WelcomeActivity> mActivityRule = new ActivityTestRule<>(
            WelcomeActivity.class, true, false);

    @Before
    public void setUp() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        mApplication = (BaseApplication) getInstrumentation().getTargetContext().getApplicationContext();
        launchActivity();
    }

    @After
    public void tearDown(){
        DataStore.getInstance().clear();
        getDataManager().clear();
    }

    /**
     * Override for specific intent launches
     */
    public void launchActivity() {
        mActivityRule.launchActivity(null);
    }

    public Activity getActivity(){
            return mActivityRule.getActivity();
    }

    public static ViewInteraction matchToolbarTitle(CharSequence title) {
        return onView(isAssignableFrom(Toolbar.class))
                .check(matches(withToolbarTitle(is(title))));
    }

    public GoogleServicesManager getGoogleServicesManager(){
        return mApplication.getComponent().googleServicesManager();
    }

    public NetworkManager getNetworkManager(){
        return mApplication.getComponent().networkManager();
    }

    public DataManager getDataManager(){
        return mApplication.getComponent().dataManager();
    }

    private static Matcher<Object> withToolbarTitle(final Matcher<CharSequence> textMatcher) {
        return new BoundedMatcher<Object, Toolbar>(Toolbar.class) {
            @Override public boolean matchesSafely(Toolbar toolbar) {
                return textMatcher.matches(toolbar.getTitle());
            }
            @Override public void describeTo(Description description) {
                description.appendText("with toolbar title: ");
                textMatcher.describeTo(description);
            }
        };
    }

    public static Matcher<View> withValidationError(final String expected) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof EditText)) {
                    return false;
                }
                EditText editText = (EditText) view;
                return editText.getError().toString().equals(expected);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }


    public void delay(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            fail("Interrupted exception");
        }
    }

}
