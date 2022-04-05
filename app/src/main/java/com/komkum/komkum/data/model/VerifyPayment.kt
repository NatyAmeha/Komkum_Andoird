package com.komkum.komkum.data.model

import androidx.annotation.Keep

@Keep
data class VerifyPayment(
    var userId : String? = null,
    var subscriptionId : String? = null,
    var paymentType : Int,
    var googlePlayPurchaseToken : String?= null
){
    companion object{
//        const val SUBSCRIPTION_PAYMENT_TYPE = 1
        const val FREE_PAYMENT_TYPE = 3

        const val PAYMENT_METHOD_GOOGLE_PAY = 1
        const val PAYMENT_METHOD_YENEPAY = 2
        const val PAYMENT_METHOD_PAYPAL = 4
    }
}