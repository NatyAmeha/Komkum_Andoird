package com.komkum.komkum.data.model

import androidx.annotation.Keep

@Keep
data class Payment(
    var type : Int,
    var priceInBirr : Float? = null,
    var priceInDollar : Float? = null
) {
    companion object{
        const val PAYMENT_TYPE_CREDIT = 0
        const val PAYMENT_TYPE_DISCOUNT = 1
        const val PAYMENT_TYPE_REGULAR = 2
        const val PAYMENT_TYPE_NO_CHARGE = 3

        const val PAYMENT_TYPE_SINGLE_PAYMENT = 5
        const val PAYMENT_TYPE_SUBSCRIPTION = 4

        const val PAYMENT_METHOD_YENEPAY = 0
        const val PAYMENT_METHOD_WALLET = 1
        const val PAYMENT_METHOD_GOOGLE_PLAY =2
        const val PAYMENT_METHOD_PAYPAL = 3
        const val PAYMENT_METHOD_CASH_ON_DELIVERY = 4
    }
}