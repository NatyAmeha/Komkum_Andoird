package com.komkum.komkum.data.model

import androidx.annotation.Keep

@Keep
data class Code(
    var value : String? = null,
    var used : Boolean? = null,
    var counter : Int? = null,
    var owner : String? = null,
    var ownerName : String? = null
)

@Keep
data class Coupon(
    var type : Int? = null,
    var code : List<Code>? = null,
    var visibility : Boolean? = false,
    var product : String? = null,
    var discount : Int? = null
){
    companion object{
        const val REWARD_TYPE_GAME = 0
        const val  REWARD_TYPE_COMMISSION = 1

        const val COUPON_TYPE_RECURRING = 0
        const val COUPON_TYPE_ONE_TIME = 1
    }
}

@Keep
data class GiftViewData(var code : Code , var productId: String? , var productName : String , var productImage : String? , var discountPercent : Int , var type : Int? , var used : Boolean = false)
