package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.ProductApi
import com.komkum.komkum.data.model.Review
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject

class ProductRepository @Inject constructor(var productApi : ProductApi) {
    var error = MutableLiveData<String>()

    suspend fun getProductDetails(productId : String) = dataRequestHelper(productId , fun(message) = error.postValue(message)){
        productApi.getProductDetail(it!!)
    }

    suspend fun getSmallProductInfo(productId : String) = dataRequestHelper(productId , fun(message) = error.postValue(message)){
        productApi.getProductInfo(it!!)
    }

    suspend fun getProductMetadata(productIds : String) = dataRequestHelper(productIds , fun(message) = error.postValue(message)){
        productApi.getProductsMetadata(it!!)
    }

    suspend fun getProductCategories() = dataRequestHelper(null , fun(message) = error.postValue(message)){
        productApi.getProductCategories()
    }

    suspend fun getBrowseListbyDepartment(department : String) = dataRequestHelper(department , fun(message) = error.postValue(message)){
        productApi.browseProductByDep(it!!)
    }

    suspend fun getBrowseListbyCategory(category : String) = dataRequestHelper(category , fun(message) = error.postValue(message)){
        productApi.browseProductByCategory(it!!)
    }

    suspend fun getTrendingProducts(limit : Int) = dataRequestHelper(limit , fun(message) = error.postValue(message)){
        productApi.getTrendingProducts(limit)
    }

    suspend fun getNewArrivalProducts() = dataRequestHelper(null , fun(message) = error.postValue(message)){
        productApi.getNewArrival()
    }

    suspend fun getBestsellingProducts(limit : Int) = dataRequestHelper(limit , fun(message) = error.postValue(message)){
        productApi.getBestsellingProducts(limit)
    }

    suspend fun getTrendingPackagesofProducts(long: Double? = null , lat : Double? = null) = dataRequestHelper(lat , fun(message) = error.postValue(message)){
        productApi.getTrendingPackageofProducts(long , lat)
    }

    suspend fun getProductsExpiresoon(long : Double? = null , lat : Double? = null) = dataRequestHelper(long , fun(message) = error.postValue(message)){
        productApi.productsExpireSoon(long , lat)
    }

    suspend fun searchProducts(query : String) = dataRequestHelper(query , fun(message) = error.postValue(message)){
        productApi.searchProducts(it!!)
    }

    suspend fun reviewProduct(reviewInfo : Review) = dataRequestHelper(reviewInfo , fun(message) = error.postValue(message)){
        productApi.reviewProduct(it!!)
    }

    suspend fun getProductReview(productId : String , page : Int) = dataRequestHelper(productId , fun(message) = error.postValue(message)){
        productApi.productReviews(it!! , page)
    }

    suspend fun getSellerDetails(sellerId : String) = dataRequestHelper(sellerId , fun(message) = error.postValue(message)){
        productApi.getSeller(it!!)
    }

    suspend fun getSellerReview(sellerId: String , rating : String) = dataRequestHelper(sellerId , fun(message) = error.postValue(message)){
        productApi.getSellerReviews(it!! , rating)
    }

}