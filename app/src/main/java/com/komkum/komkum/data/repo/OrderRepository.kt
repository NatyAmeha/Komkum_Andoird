package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.OrderApi
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.Order
import com.komkum.komkum.data.model.OrderedItem
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.util.extensions.dataRequestHelper
import javax.inject.Inject

class OrderRepository @Inject constructor(var orderApi: OrderApi) {
    var error = MutableLiveData<String>()

    suspend fun placeOrder(orderInfoInfo : Order<String , Address> , paymentMethod : Int) = dataRequestHelper(orderInfoInfo , fun(message) = error.postValue(message)){
        orderApi.createOrder(it!!, paymentMethod)
    }

    suspend fun addtoCart(itemInfo : OrderedItem<String>) = dataRequestHelper(itemInfo , fun(message) = error.postValue(message)){
        orderApi.addToCart(it!!)
    }

    suspend fun removeFromCart(id : String) = dataRequestHelper(id , fun(message) = error.postValue(message)){
        orderApi.removeFromCart(it!!)
    }

    suspend fun clearCart() = dataRequestHelper(null , fun(message) = error.postValue(message)){
        orderApi.clearCart()
    }

    suspend fun getCartItems() = dataRequestHelper<Any , List<OrderedItem<Product>>>( errorHandler = fun(message) = error.postValue(message)){
        orderApi.getCart()
    }

    suspend fun getRelatedProductsInCart(productIds: String) = dataRequestHelper(productIds , fun(message) = error.postValue(message)){
        orderApi.getRelatedProducts(it!!)
    }

    suspend fun getOrder(orderId : String) = dataRequestHelper( orderId , fun(message) = error.postValue(message)){
        orderApi.getOrder(orderId)
    }

    suspend fun getDiscountcodeForOrder(productIds: String) = dataRequestHelper(productIds , fun(message) = error.postValue(message)){
        orderApi.getDiscountCodesForProducts(it!!)
    }

    suspend fun applyDiscountCode(code : String , productIds : String? = null) = dataRequestHelper(code , fun(message) = error.postValue(message)){
        orderApi.applyDiscount(code , productIds)
    }

    suspend fun getDeliveryDestinations() = dataRequestHelper<Any , MutableList<String>>(null,  fun(message) = error.postValue(message)){
        orderApi.getDeliveryDestination()
    }
}