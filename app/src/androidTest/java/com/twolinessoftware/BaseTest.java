package com.twolinessoftware;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.twolinessoftware.activities.WelcomeActivity;
import com.twolinessoftware.data.DataManager;
import com.twolinessoftware.data.DataManagerModule;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.notifications.GoogleServicesManager;
import com.twolinessoftware.notifications.GoogleServicesModule;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import junit.framework.Assert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;


/**
 * The Base Test injects the Test Modules in addition to the mockwebserver
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseTest {

    @Inject
    public MockWebServer mMockWebServer;

    private Instrumentation mInstrumentation;
    private MockComponent mComponent;
    private Activity currentActivity;

    public Instrumentation getInstrumentation() {
        return mInstrumentation;
    }

    @Rule
    public ActivityTestRule<WelcomeActivity> mActivityRule = new ActivityTestRule<>(
            WelcomeActivity.class, true, false);

    @Before
    public void setUp() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
        BaseApplication app
                = (BaseApplication) getInstrumentation().getTargetContext().getApplicationContext();

       mComponent = DaggerMockComponent.builder()
                .mockApplicationModule(new MockApplicationModule(app))
                .mockNetworkModule(new MockNetworkModule(app))
                .googleServicesModule(new GoogleServicesModule(app))
               .dataManagerModule(new DataManagerModule(app))
                .build();
        app.setComponent(mComponent);
        mComponent.inject(this);
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
    public void launchActivity(Intent intent) {
        mActivityRule.launchActivity(intent);
    }

    public void launchActivity(){
        launchActivity(null);
    }

    public Activity getActivity(){
            return mActivityRule.getActivity();
    }

    public static ViewInteraction matchToolbarTitle(CharSequence title) {
        return onView(isAssignableFrom(Toolbar.class))
                .check(matches(withToolbarTitle(is(title))));
    }

    public GoogleServicesManager getGoogleServicesManager(){
        return mComponent.googleServicesManager();
    }

    public NetworkManager getNetworkManager(){
        return mComponent.networkManager();
    }

    public DataManager getDataManager(){
        return mComponent.dataManager();
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

    @SuppressWarnings("unchecked")
    public static Matcher<View> withRecyclerView(@IdRes int viewId)
    {
        return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
    }

    @SuppressWarnings("unchecked")
    public static ViewInteraction onRecyclerItemView(@IdRes int recyclerView,@IdRes int identifyingView, Matcher<View> identifyingMatcher, Matcher<View> childMatcher)
    {
        Matcher<View> itemView = allOf(withParent(withRecyclerView(recyclerView)),
                withChild(allOf(withId(identifyingView), identifyingMatcher)));
        return Espresso.onView(allOf(isDescendantOfA(itemView), childMatcher));
    }

    public static ViewAssertion hasItemsCount(final int count) {
        return new ViewAssertion() {
            @Override public void check(View view, NoMatchingViewException e) {
                if (!(view instanceof RecyclerView)) {
                    throw e;
                }
                RecyclerView rv = (RecyclerView) view;
                assertThat(rv.getAdapter().getItemCount(), equalTo(count));
            }
        };
    }


    public Activity getCurrentActivity(){

        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    currentActivity = (Activity)resumedActivities.iterator().next();
                }
            }
        });

        return currentActivity;
    }




}
