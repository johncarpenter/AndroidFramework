package com.twolinessoftware.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 */
public class GsonUtil {

    public static Gson buildGsonAdapter() {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create();

        return gson;
    }

}
