package com.twolinessoftware.unit;

import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Test;

import timber.log.Timber;

import static junit.framework.Assert.assertEquals;


@SmallTest
public class CipherUtilUnitTest {

    @Test
    public void unitTest_TestReferralScheme() {

        String testBody = "{\"fullName\":\"Simon Pegg\",\"email\":\"simon@pegg.com\",\"telephone\":\"4035551212\",\"notes\":\"Cool guy.\"}";
        //String key = "898be9dc5004ed0fa6e117c9a3099d31";
// 0012ab8c82ac96ec08212fa2639d730f8dadd7f44079f6ad67ea95ca04bedfa2

       CryptUtils.AesWrapper result = CryptUtils.calculateRsa(testBody);

        Timber.v("IV:" + result.iv);
        Timber.v("Cipher:" + result.message);
        Timber.v("Key:"+result.key);

        String test = CryptUtils.decodeRsa(result.message,result.iv,result.key);

        assertEquals(test,testBody);

    }


}
