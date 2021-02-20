package com.zomatunes.zomatunes.data.model

data class VerifyPayment(
    var userId : String,
    var subscriptionId : String? = null,
    var paymentType : Int
){
    companion object{
        const val EXPRESS_PAYMENT_TYPE = 0
        const val SUBSCRIPTION_PAYMENT_TYPE = 1
        const val FREE_PAYMENT_TYPE = 2
    }
}