package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

@Keep
@Parcelize
data class Order<P, A>(
    var _id: String? = null,
    var totalPrice: Int? = null,
    var user: String? = null,
    var date: Date? = null,
    var estDelTime: Date? = null,
    var status: Int? = null,
    var statusMsg: String? = null,
    var code: String? = null,
    var items: List<@RawValue OrderedItem<P>>? = null,
    var address: @RawValue A? = null,
    var paymentMethod : Int? = null,
    var discountCodes : MutableList<String> = mutableListOf(),
    var discountApplied : Boolean? = null
) : Parcelable{
    companion object{
        const val STATUS_SELLER_STORE = 0
        const val STATUS_IN_STORE = 1
        const val STATUS_NEAR_STORE = 2
        const val STATUS_OUT_FOR_DELIVERY = 3
        const val STATUS_DELIVERED = 4
        const val STATUS_CANCELLED = 5
    }
}

@Keep
@Parcelize
data class OrderedItem<P>(
    var _id: String? = null,
    var product: @RawValue P? = null,
    var qty: Int? = null,
    var srcLocation: String? = null,
    var price: Int? = null,
    var team: String? = null,
    var image : String? = null,
    var sku: Map<String, String>? = null,
    var purchaseType : Int? = null,
    var additionalQty : Int = 0  // if product added to cart from packages. some packages has additional qty
) : Parcelable


@Parcelize
@Keep
data class ShippingAddressInfo(
    var shippingAddresses : List<Address>? = null,
    var selectedAddress :  Address? = null,
    var deliveryDestinationInfo : List<Destination>? = null,
    var deliveryPrice : Int? = 0,
    var deliveryTiime : Int? = 0
//    var order? : IOrder
) : Parcelable
