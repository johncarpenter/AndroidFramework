package com.twolinessoftware.notifications;

import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 * Used to simulated and testing GCM registrations
 *
 */
@RunWith(AndroidJUnit4.class)
public class GCMRegistrationTest extends BaseTest {


    final CountDownLatch latch = new CountDownLatch(1);
    private boolean gcmPasses;

    @Override
    public void launchActivity() {
        // Don't launch activities
    }

    @Test
    public void test_ReceivesRegistration(){

        EventBus.getDefault().register(this);


        // Update GCM response
        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(TestDataUtil.USER_REPLY));


        DataStore.getInstance().setSignedin(true);
        DataStore.getInstance().storeGcmRegistration("empty",0);
        getGoogleServicesManager().getGCMService().register();

        gcmPasses = false;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertTrue(gcmPasses);

        EventBus.getDefault().unregister(this);

    }



    public void onEventMainThread(GCMService.GcmUpdatedEvent event){
        System.out.println("GCM Ret:"+event.success);
        gcmPasses = event.success;
        latch.countDown();
    }


}
