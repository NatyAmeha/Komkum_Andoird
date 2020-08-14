package com.example.ethiomusic.data.api

import com.example.ethiomusic.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {

    @POST("auth/checkemail")
    suspend fun checkEmail(@Body authmodel : AuthenticationModel) : Response<Boolean>

    @POST("auth/signup")
    suspend fun registerUser(@Body userData : User) : Response<String>

    @POST("auth/signin")
    suspend fun authenticate(@Body authenticationData : AuthenticationModel) : Response<String>

    @POST("auth/signinfacebook")
    suspend fun continueWithFacebook(@Body userData: User) : Response<FacebookAuthResponse>

    @POST("subscription/add")
    suspend fun addSubscription(@Body subscriptionInfo : SubscriptionPaymentInfo) : Response<String>

    @GET("subscription")
    suspend fun getSubscriptions() : Response<List<SubscriptionPlan>>
}