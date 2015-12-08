package com.twolinessoftware.ui;

import android.app.Instrumentation;
import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.activities.MainActivity;
import com.twolinessoftware.storage.DataStore;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertNotNull;

/**
 * Tests that are run when a user launches the app after completing the registrations
 */
@RunWith(AndroidJUnit4.class)
public class LaunchInstrumentationTest extends BaseTest {

    private Instrumentation.ActivityMonitor mMainMonitor;
    private boolean mCurrentValue;

    @Override
    public void launchActivity() {
        mMainMonitor = getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);

        mCurrentValue = DataStore.getInstance().getSignedIn();

        DataStore.getInstance().setSignedin(true);

        super.launchActivity();
    }

    @After
    public void teardown(){

        DataStore.getInstance().setSignedin(mCurrentValue);
        getInstrumentation().removeMonitor(mMainMonitor);
    }

    @Test
    public void test_ShouldPassThroughToMainIfSignedIn() {
        MainActivity mainActivity = (MainActivity) getInstrumentation().waitForMonitorWithTimeout(mMainMonitor, 5000);

        assertNotNull(mainActivity);
        matchToolbarTitle(getActivity().getString(com.twolinessoftware.R.string.main_fragment_title).toUpperCase(Locale.US));

    }




}
