package com.komkum.komkum.data.api

import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.ProductDetailViewmodel
import com.komkum.komkum.data.viewmodel.ProductHomepageViewmodel
import retrofit2.Response
import retrofit2.http.*

interface ProductApi {

    @GET("product/{id}")
    suspend fun getProductDetail(@Path("id") productId : String) : Response<ProductDetailViewmodel>

    @GET("product/{id}/small")
    suspend fun getProductInfo(@Path("id") productId : String) : Response<Product>

    @GET("products")
    suspend fun getProductsMetadata(@Query("ids") productIds : String) : Response<List<ProductMetadata<Product,String>>>

    @GET("product/categories")
    suspend fun getProductCategories() : Response<List<ProductCategory>>

    @GET("products/browse/dep")
    suspend fun browseProductByDep(@Query("dep") department : String) : Response<ProductHomepageViewmodel>

    @GET("products/browse/cat")
    suspend fun browseProductByCategory(@Query("cat") category : String) : Response<List<Product>>

    @GET("products/trending")
    suspend fun getTrendingProducts(@Query("limit") limit : Int? = null) : Response<List<Product>>

    @GET("products/new")
    suspend fun getNewArrival() : Response<List<Product>>


    @GET("products/bestselling")
    suspend fun getBestsellingProducts(@Query("limit") limit : Int? = null) : Response<List<Product>>

    @GET("packages/trending")
    suspend fun getTrendingPackageofProducts(@Query("long") long : Double? = null , @Query("lat") lat : Double? = null) : Response<TeamList>

    @GET("products/expiresoon")
    suspend fun productsExpireSoon(@Query("long") long : Double? = null , @Query("lat") lat : Double? = null) : Response<TeamList>

    @GET("product/search")
    suspend fun searchProducts(@Query("query") query : String) : Response<Search>

    @POST("product/review")
    suspend fun reviewProduct(@Body reviewInfo : Review) : Response<Boolean>

    @GET("product/{id}/reviews")
    suspend fun productReviews(@Path("id") productId : String , @Query("page") page : Int) : Response<MutableList<Review>>

    @GET("seller/{id}")
    suspend fun getSeller(@Path("id") sellerId : String) : Response<Seller<Product>>

    @GET("seller/{id}/reviews")
    suspend fun getSellerReviews(@Path("id") sellerId : String , @Query("rating") rating : String) : Response<MutableList<Review>>

}