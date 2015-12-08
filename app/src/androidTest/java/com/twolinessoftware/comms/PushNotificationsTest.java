package com.twolinessoftware.comms;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.twolinessoftware.TestDataUtil;
import com.twolinessoftware.notifications.GCMService;
import com.twolinessoftware.storage.DataStore;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;
import timber.log.Timber;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;

/**
 *
 * Used to validate and verify the API calls. Runs on device through the network
 *
 */
@RunWith(AndroidJUnit4.class)
public class PushNotificationsTest extends OnlineBaseTest{


    private boolean gcmPasses;

    final CountDownLatch latch = new CountDownLatch(1);

    @Test
    public void test_PushAppointment(){

        EventBus.getDefault().register(this);

        DataStore.getInstance().setUserIdAndSecret(1, TestDataUtil.TEST_CLIENT_KEY);
        DataStore.getInstance().setSignedin(true);
        DataStore.getInstance().storeGcmRegistration("empty", 0);

        getGoogleServicesManager().getGCMService().register();

        gcmPasses = false;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertTrue(gcmPasses);

        // send test push message

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("appointmentId",1);
            jsonObject.put("eventTypeId",1);
            jsonObject.put("userId",1);
            jsonObject.put("providerId",1);
        }catch(JSONException jse){
            Timber.e("WTF:Json:" + Log.getStackTraceString(jse));
        }
        String testJson = jsonObject.toString();

        getNetworkManager().testSendPushNotification(testJson);

        // Manually check to see if the notifications were sent. This is pretty tough to test


        EventBus.getDefault().unregister(this);

    }

    @Test
    public void test_PushMessage(){

        EventBus.getDefault().register(this);

        DataStore.getInstance().setUserIdAndSecret(1, TestDataUtil.TEST_CLIENT_KEY);
        DataStore.getInstance().setSignedin(true);
        DataStore.getInstance().storeGcmRegistration("empty", 0);

        getGoogleServicesManager().getGCMService().register();

        gcmPasses = false;

        try {
            latch.await();
        } catch (InterruptedException e) {
            fail("Interrupted");
        }

        assertTrue(gcmPasses);

        // send test push message

        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("title","title");
            jsonObject.put("message","message");
        }catch(JSONException jse){
            Timber.e("WTF:Json:" + Log.getStackTraceString(jse));
        }
        String testJson = jsonObject.toString();

        getNetworkManager().testSendPushNotification(testJson);

        // Manually check to see if the notifications were sent. This is pretty tough to test


        EventBus.getDefault().unregister(this);

    }



    public void onEventMainThread(GCMService.GcmUpdatedEvent event){
        System.out.println("GCM Ret:"+event.success);
        gcmPasses = event.success;
        latch.countDown();
    }

}
