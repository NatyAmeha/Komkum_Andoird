package com.zomatunes.zomatunes.data.api

import com.zomatunes.zomatunes.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @POST("auth/checkemail")
    suspend fun checkEmail(@Body authmodel : AuthenticationModel) : Response<Boolean>

    @POST("user/update")
    suspend fun updateUser(@Body user : UserWithSubscription) : Response<Boolean>

    @GET("user")
    suspend fun getUser() : Response<UserWithSubscription>

    @GET("user/friends/activity/{id}")
    suspend fun getUserFbFriendActivity(@Path("id") userId : String) : Response<FriendActivity>

    @POST("auth/signup")
    suspend fun registerUser(@Body userData : User) : Response<String>

    @POST("auth/signin")
    suspend fun authenticate(@Body authenticationData : AuthenticationModel) : Response<String>

    @POST("auth/forgotpassword")
    suspend fun forgotPassword(@Body resetInfo: AuthenticationModel) : Response<Boolean>

    @POST("auth/resetpassword")
    suspend fun resetPassword(@Body resetInfo : AuthenticationModel , @Query("token") token : String) : Response<Boolean>

    @POST("auth/signinfacebook")
    suspend fun continueWithFacebook(@Body userData: User) : Response<FacebookAuthResponse>

    @POST("subscription/add")
    suspend fun addSubscription(@Body subscriptionInfo : SubscriptionPaymentInfo) : Response<String>

    @GET("subscription")
    suspend fun getSubscriptions() : Response<List<SubscriptionPlan>>

    @PUT("payment/verify")
    suspend fun verifyPayment(@Body paymentInfo : VerifyPayment) : Response<String>

    @PUT("user/friends")
    suspend fun getFacebookFriendsData(@Body friendsId : List<String>) : Response<List<User>>

    @PUT("user/list")
    suspend fun getUsers(@Body userId : List<String>) : Response<List<User>>

    @PUT("user/fcm/android")
    suspend fun updateFcmRegistrationToken(@Query("fcmtoken") token : String) : Response<Boolean>
}