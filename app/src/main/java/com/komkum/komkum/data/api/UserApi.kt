package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.RewardInfo
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface UserApi {

    @Multipart
    @POST("user/uploadimage")
    suspend fun uploadImage(@Part file: MultipartBody.Part) : Response<String>


    @POST("auth/checkemail")
    suspend fun checkEmail(@Body authmodel : AuthenticationModel) : Response<Boolean>

    @POST("user/update")
    suspend fun updateUser(@Body user : UserWithSubscription) : Response<Boolean>

    @GET("user")
    suspend fun getUser() : Response<UserWithSubscription>

    @GET("subscription/isvalid")
    suspend fun checkSubscription() : Response<Boolean>

    @GET("user/friends/activity/{id}")
    suspend fun getUserFbFriendActivity(@Path("id") userId : String) : Response<FriendActivity>

    @POST("auth/signup")
    suspend fun registerUser(@Body userData : User) : Response<User>

    @POST("auth/register")
    suspend fun signUp(@Body userData : User) : Response<String>

    @POST("auth/signin")
    suspend fun authenticate(@Body authenticationData : AuthenticationModel) : Response<String>

    @POST("creator/donate")
    suspend fun makeDonation(@Body donation: Donation) : Response<Donation>

    @POST("auth/forgotpassword")
    suspend fun forgotPassword(@Body resetInfo: AuthenticationModel) : Response<UserVerification>

    @POST("auth/resetpassword")
    suspend fun resetPassword(@Body resetInfo : AuthenticationModel , @Query("token") token : String) : Response<Boolean>

    @POST("auth/signinfacebook")
    suspend fun continueWithFacebook(@Body userData: User) : Response<FacebookAuthResponse>

    @POST("subscription/add")
    suspend fun addSubscription(@Body subscriptionInfo : SubscriptionPaymentInfo) : Response<String>

    @GET("subscription")
    suspend fun getSubscriptions() : Response<List<SubscriptionPlan>>

    @PUT("payment/verify")
    suspend fun verifyPayment(@Body paymentInfo : VerifyPayment) : Response<Boolean>

    @PUT("subscription/upgrade")
    suspend fun upgradeSubscription(@Query("id") subscriptionId : String , @Query("method")paymentMethod : Int , @Query("token")token : String? = null) : Response<Boolean>

    @PUT("user/friends")
    suspend fun getFacebookFriendsData(@Body friendsId : List<String>) : Response<List<User>>

    @PUT("users/list")
    suspend fun getUsers(@Body userId : List<String>) : Response<List<User>>

    @GET("ads")
    suspend fun getAds(@Query("limit") limit : Int , @Query("type") type : Int? = null) : Response<List<Ads>>



    @PUT("user/fcmtoken/add")
    suspend fun updateFcmRegistrationToken(@Query("fcmtoken") token : String) : Response<Boolean>

    @GET("user/wishlists")
    suspend fun getUserWishlists() : Response<List<Product>>


    @PUT("user/wishlist/add")
    suspend fun addtoWishlist(@Query("pid") productId : String) : Response<Boolean>

    @PUT("user/wishlist/remove")
    suspend fun removeFromWishlist(@Query("pid") productId : String) : Response<Boolean>


    @PUT("user/shippingaddr/add")
    suspend fun addNewAddress(@Body addressInfo : Address) : Response<Address>

    @GET("user/shippingaddr")
    suspend fun getUserAddress(@Query("pid") productIds : String , @Query("address") addressId : String? = null) : Response<ShippingAddressInfo>

    @GET("user/orders")
    suspend fun getUserOrders() : Response<List<Order<Product,String>>>


    @GET("user/commission")
    suspend fun getCommissionAmount() : Response<Double>

    @GET("user/rewards")
    suspend fun getRewardAmount() : Response<List<RewardInfo>>

}