package com.twolinessoftware.comms;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twolinessoftware.BaseApplication;
import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.activities.WelcomeActivity;
import com.twolinessoftware.network.NetworkManager;
import com.twolinessoftware.network.messages.BaseServerMessage;
import com.twolinessoftware.network.messages.PingMessage;
import com.twolinessoftware.notifications.GCMService;
import com.twolinessoftware.storage.DataStore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Subscriber;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 *
 * Used to validate and verify the API calls. Runs on device through the network
 *
 */
@RunWith(AndroidJUnit4.class)
public class ApiCommunicationTest {

    private PingMessage pingMessage;

    @Rule
    public ActivityTestRule<WelcomeActivity> mActivityRule = new ActivityTestRule<>(
            WelcomeActivity.class, true, false);

    private BaseApplication mApplication;


    @Before
    public void setUp() {
       // mActivityRule.launchActivity(null);
        mApplication = (BaseApplication) getInstrumentation().getTargetContext().getApplicationContext();
    }


    @Test
    public void test_OnlinePingResponse(){

        DataStore.getInstance().setSignedin(false);

        final CountDownLatch latch = new CountDownLatch(1);

        NetworkManager networkManager = BaseApplication.get(mApplication).getComponent().networkManager();

        networkManager.getBaseApiService().postPingTest(new BaseServerMessage()).subscribe(new Subscriber<PingMessage>() {
            @Override
            public void onCompleted() {
                System.out.println("Finished");
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }

            @Override
            public void onNext(PingMessage pm) {
                pingMessage = pm;
                latch.countDown();
            }
        });

        try {
            latch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Timeout");
        }

        assertThat(pingMessage, is(notNullValue()));
        assertThat(pingMessage.getUserMsg(), containsString("Pong!"));

    }

    @Test
    public void test_OnlineAuthPingResponse(){

        DataStore.getInstance().setUserIdAndSecret(1, TestDataUtil.TEST_CLIENT_KEY);
        DataStore.getInstance().setSignedin(true);

        final CountDownLatch latch = new CountDownLatch(1);

        NetworkManager networkManager = BaseApplication.get(mApplication).getComponent().networkManager();

        networkManager.getBaseApiService().postAuthPingTest(new BaseServerMessage()).subscribe(new Subscriber<PingMessage>() {
            @Override
            public void onCompleted() {
                System.out.println("Finished");
            }

            @Override
            public void onError(Throwable e) {

                System.out.println("Error:"+e.getMessage());


                latch.countDown();
            }

            @Override
            public void onNext(PingMessage pm) {
                pingMessage = pm;
                latch.countDown();
            }
        });

        try {
            latch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Timeout");
        }

        assertThat(pingMessage, is(notNullValue()));
        assertThat(pingMessage.getUserMsg(), containsString("Auth Pong!"));

    }



    private boolean gcmPasses;

    final CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void test_ServerGcmRegistration(){

        EventBus.getDefault().register(this);

        DataStore.getInstance().setUserIdAndSecret(1, TestDataUtil.TEST_CLIENT_KEY);
        DataStore.getInstance().setSignedin(true);
        DataStore.getInstance().storeGcmRegistration("empty", 0);

        BaseApplication.get(mApplication).getComponent().googleServicesManager().getGCMService().register();

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
