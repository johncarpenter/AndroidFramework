package com.twolinessoftware.network;


import com.twolinessoftware.authentication.Token;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface BaseApiService {


    @FormUrlEncoded
    @POST("/user/login")
    Observable<Token> login(@Field("e") String email, @Field("p") String password);

    @FormUrlEncoded
    @POST("/user/register")
    Observable<Token> register(@Field("e") String email, @Field("p") String password);

    @FormUrlEncoded
    @POST("/user/forgot")
    Observable<ApiResponse> resetPassword(@Field("e") String email);

    @FormUrlEncoded
    @POST("/user/gcm")
    Observable<ApiResponse> updateGcm(@Field("gcm") String gcm);


}

