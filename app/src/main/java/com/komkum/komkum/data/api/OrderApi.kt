package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("order/create")
    suspend fun createOrder(@Body orderInfo : Order<String,Address> , @Query("pay") paymentMethod : Int) : Response<Order<String,String>>

    @PUT("cart/add")
    suspend fun addToCart(@Body itemInfo : OrderedItem<String>) : Response<Boolean>

    @PUT("cart/remove")
    suspend fun removeFromCart(@Query("id") id : String) : Response<Boolean>

    @PUT("cart/clear")
    suspend fun clearCart() : Response<Boolean>

    @GET("order/discountcodes")
    suspend fun getDiscountCodesForProducts(@Query("products")productIds: String) : Response<List<GiftViewData>>

    @PUT("order/applydiscount")
    suspend fun applyDiscount(@Query("code") code : String , @Query("pids") productId : String? ) : Response<Coupon>

    @GET("user/cart")
    suspend fun getCart() : Response<List<OrderedItem<Product>>>


    @GET("cart/relatedproducts")
    suspend fun getRelatedProducts(@Query("products") productIds: String) : Response<List<Product>>

    @GET("order/{id}")
    suspend fun getOrder(@Path("id") orderId : String) : Response<Order<Product , String>>

    @GET("destinations")
    suspend fun getDeliveryDestination() : Response<MutableList<String>>
}