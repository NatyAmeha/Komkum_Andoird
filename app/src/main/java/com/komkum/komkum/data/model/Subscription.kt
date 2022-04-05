package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class Subscription(
    var subscriptionId: String,
    var usedBookCredit : Int?,
    var startDate: Date,
    var expireDate: Date
) : Parcelable

@Keep
@Parcelize
data class SubscriptionWithPlan(
    var subscriptionId: SubscriptionPlan? = null,
    var usedBookCredit : Int?,
    var startDate: Date?,
    var expireDate: Date?
) : Parcelable


@Parcelize
@Keep
data class SubscriptionPlan(
    var _id: String? = null,
    var name: String? = null,
    var priceInBirr: Float? = null,
    var priceInDollar: Float? = null,
    var features : List<String>? = null,
    var planType: String? = null,
    var trialPeriod : Int? = null,
    var dayLength: Int? = null,
    var isActivated: Boolean? = null,
    var level : Int? = null,
    var discount : Int? = null,
    var bookCredit : Int? = null,
    var googleplaySubscriptionId : String? = null

) : BaseModel(_id) , Parcelable{
    companion object{
        const val SUBSCRIPTION_LEVEL_1 = 1      // only access subscription service and regular price purchase
        const val SUBSCRIPTION_LEVEL_2 = 2      // access subscription service plus discount purchase
        const val SUBSCRIPTION_LEVEL_3 = 3      // access subscription service , discount purchase and credit purchase
    }
}


@Keep
@Parcelize
data class SubscriptionPaymentInfo(
    var userId : String,
    var subscriptionId : String,
    var paymentMethod : String

) : Parcelable

