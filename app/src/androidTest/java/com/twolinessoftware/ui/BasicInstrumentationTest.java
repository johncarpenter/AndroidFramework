package com.twolinessoftware.ui;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.twolinessoftware.BaseTest;
import com.twolinessoftware.network.messages.BaseServerMessage;
import com.twolinessoftware.network.messages.PingMessage;
import com.twolinessoftware.storage.DataStore;
import com.squareup.okhttp.mockwebserver.MockResponse;

import junit.framework.Assert;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class BasicInstrumentationTest extends BaseTest {


    @Override
    public void launchActivity() {
        DataStore.getInstance().setSignedin(false);
        super.launchActivity();
    }

    @Test
    public void test_ActivityHasStarted() {
        MatcherAssert.assertThat(mActivityRule.getActivity(), is(not(nullValue())));
    }

    private PingMessage pingMessage;

    @Test
    public void test_MockApiWorks(){

        mMockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{'userMsg':'wow'}"));

        final CountDownLatch latch = new CountDownLatch(1);

        getNetworkManager().getBaseApiService().postPingTest(new BaseServerMessage()).subscribe(new Subscriber<PingMessage>() {
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
            latch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Assert.fail("Timeout");
        }

        assertThat(pingMessage, Matchers.is(notNullValue()));
        assertThat(pingMessage.getUserMsg(), containsString("wow"));
    }


}
