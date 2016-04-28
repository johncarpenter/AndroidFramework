/*
 * Copyright 2016 2LinesSoftware Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twolinessoftware.network;

import com.squareup.okhttp.OkHttpClient;
import com.twolinessoftware.Config;
import com.twolinessoftware.utils.GsonUtil;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedByteArray;
import timber.log.Timber;

/**
 *
 */
public class BaseRetrofitHelper {

    public static final int CONNECT_TIMEOUT = 20; // seconds

    public BaseApiService newBaseApiService(String endpoint) {

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setClient(new OkClient(okHttpClient))
                .setRequestInterceptor(apiRequestInterceptor)
                .setLogLevel(Config.RETROFIT_LOGLEVEL)
                .setErrorHandler(apiErrorConverter)
                .setConverter(new GsonConverter(GsonUtil.buildGsonAdapter()))
                .build();
        return restAdapter.create(BaseApiService.class);
    }


    final RequestInterceptor apiRequestInterceptor = request -> {
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Language", "en");
    };

    private final ErrorHandler apiErrorConverter = cause -> {

        Timber.e("Comms Error:" + cause.getMessage());
        String json = new String(((TypedByteArray) cause.getResponse()
                .getBody()).getBytes());
        Timber.v("Error:Cause:" + json);

        if ( cause.getKind() == RetrofitError.Kind.NETWORK ) {
            if ( cause.getCause() instanceof SocketTimeoutException ) {
                return CommException.TIMEOUT;
            } else {
                return CommException.NO_CONNECTION;
            }
        }

        if ( cause.getResponse() == null ) {
            return CommException.UNKNOWN;
        }

        // @todo parse the errors response and append to the throwable error

        CommException commException = CommException.UNKNOWN;
        commException.setCode(cause.getResponse().getStatus());

        return commException;

    };

}
