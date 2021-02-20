package com.zomatunes.zomatunes.data.model

data class Payment(
    var type : Int,
    var priceInBirr : Float? = null,
    var priceInDollar : Float? = null
) {
    companion object{
        const val PAYMENT_TYPE_FREE = 0
        const val PAYMENT_TYPE_DISCOUNT = 1
        const val PAYMENT_TYPE_REGULAR = 2
    }
}