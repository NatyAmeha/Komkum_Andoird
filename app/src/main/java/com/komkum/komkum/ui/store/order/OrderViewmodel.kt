package com.komkum.komkum.ui.store.order

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.ktx.api.net.awaitFetchPlace
import com.google.android.libraries.places.ktx.api.net.awaitFindAutocompletePredictions
import com.google.android.libraries.places.ktx.api.net.fetchPlaceRequest
import com.google.android.libraries.places.ktx.api.net.findAutocompletePredictionsRequest
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.OrderRepository
import com.komkum.komkum.data.repo.UserRepository
import com.komkum.komkum.usecase.PaymentUsecase
import com.komkum.komkum.usecase.WalletUsecase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OrderViewmodel @ViewModelInject constructor(@Assisted savedStateHandle: SavedStateHandle , var orderRepo : OrderRepository,
var userRepo : UserRepository , var paymentUsecase: PaymentUsecase , var walletUsecase : WalletUsecase) : ViewModel() {

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String>{
        var errorSource = MediatorLiveData<String>()
        errorSource.addSource(paymentUsecase.error){error.value = it}
        errorSource.addSource(walletUsecase.error){error.value  = it}
        errorSource.addSource(orderRepo.error){error.value = it}
        errorSource.addSource(userRepo.error){error.value = it}
        return errorSource
    }

    fun removeOldError(){
        paymentUsecase.error.value = null
        walletUsecase.error.value = null
        orderRepo.error.value = null
        userRepo.error.value = null

    }

    var orderLists = MutableLiveData<List<Order<Product, String>>>()
    var orderDetails = MutableLiveData<Order<Product,String>>()
    var cartInfo = MutableLiveData<List<OrderedItem<Product>>>()
    var shippingADdressWithDeliveryInfo = MutableLiveData<ShippingAddressInfo>()


    suspend fun getOrderDetails(orderId : String) =  orderRepo.getOrder(orderId).data


    fun placeOrder(orderInfo : Order<String , Address> , paymentMethod : Int) = liveData {
        var result = orderRepo.placeOrder(orderInfo , paymentMethod).data
        emit(result)
    }



    fun getDiscountCodesForProducts(productIds : String) = liveData {
        var result = orderRepo.getDiscountcodeForOrder(productIds).data
        emit(result)
    }

    fun applyDiscountCode(code : String , productIds : String? = null) = liveData {
        var result = orderRepo.applyDiscountCode(code , productIds).data
        emit(result)
    }


    fun addToCart(item : OrderedItem<String>) = liveData {
        var result = orderRepo.addtoCart(item).data
        emit(result)
    }

    fun removeFromCart(cartInfoId : String) = liveData {
        var result = orderRepo.removeFromCart(cartInfoId).data
        emit(result)
    }

    fun clearCart() = liveData {
        var result = orderRepo.clearCart().data
        if(result == true) cartInfo.value = emptyList()
        emit(result)
    }

    fun getShippingAndDeliveryInfo(pIds : String , addressId : String? = null) = liveData {
        var result = userRepo.getShippingAndDeliveryInfo(pIds , addressId).data
        emit(result)
    }

    fun addNewShippingAddress(address: Address) = liveData {
        var result = userRepo.creatNewShippingAddress(address).data
        emit(result)
    }

    fun getUserCart() = viewModelScope.launch {
        var result = orderRepo.getCartItems().data
        if(result != null)cartInfo.value = result!!
    }

    fun getRelatedProductsInCart(productIds: String) = liveData {
        var result = orderRepo.getRelatedProductsInCart(productIds)
        emit(result.data)
    }

    fun getUserOrders() = viewModelScope.launch {
        var result = userRepo.getUserOrderHistory().data
        if(result != null) orderLists.value = result!!
    }

    fun getDeliveryDestination() = liveData {
        var result = orderRepo.getDeliveryDestinations().data
        emit(result)
    }


    //0---------------------------------
    fun removeItemFromCartList(item : OrderedItem<Product>){
        var previousCartInf = cartInfo.value?.toMutableList()
         previousCartInf?.remove(item)
        cartInfo.value = previousCartInf ?: emptyList()
    }

    fun getOrderInformation() = liveData{
        var result = orderRepo.getCartItems().data
        if(!result.isNullOrEmpty()){
            var orderedItems = result.map { info ->
                OrderedItem(null , info.product!!._id , info.qty , info.srcLocation , info.price ,
                    info.team ,  info.image, info.sku , info.purchaseType , info.additionalQty)
            }
            var totalPrice = orderedItems
                .map { item -> item.price!!.times(item.qty ?: 1)}
                .reduce { acc, i -> acc.plus(i) }
            var order = Order<String , Address>(items = orderedItems , totalPrice = totalPrice)
            emit(order)
        }
        emit(null)

    }


    fun getWalletBalance() = liveData {
        var result = walletUsecase.getWalletBalance()
        emit(result)
    }

    fun initiateYenePay(amount : Double , id : String , name : String , context : Context , action : () -> Unit) = viewModelScope.launch{
        paymentUsecase.initiateYenePayPayment(amount , id , name , context){
            action()
        }
    }

    fun isAuthenticated() = userRepo.userManager.isLoggedIn()

    fun getPlaceSuggestion(query : String , token: AutocompleteSessionToken , placesClient: PlacesClient) = liveData {
        try {
            var request = findAutocompletePredictionsRequest {
                setCountries("ET")
                typeFilter = TypeFilter.ESTABLISHMENT
                sessionToken = token
                setQuery(query)

            }
            var response = placesClient.awaitFindAutocompletePredictions(request)
            var result = response.autocompletePredictions.map { it ->
                Address(_id = it.placeId , address = it.getFullText(null).toString() , )
            }
            emit(result)
        }catch (ex : Throwable){
            error.value = ex.message
        }

    }

    fun getPlaceDetails(placeId : String , placesClient: PlacesClient) = liveData{
        var fieldRequest = Place.Field.LAT_LNG
        var request = fetchPlaceRequest(placeId , listOf( Place.Field.LAT_LNG , Place.Field.ADDRESS) )
        var response = placesClient.awaitFetchPlace(request)
        var placeDetails = response.place

        var result = Address(nearLocation = placeDetails.name ,  address = placeDetails.address ,
            location = GeoSpacial(type = "Point" , coordinates = listOf(placeDetails.latLng.longitude , placeDetails.latLng.latitude)))
        emit(result)
    }
}