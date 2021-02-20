package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class Subscription(
    var subscriptionId: String,
    var usedBookCredit : Int?,
    var startDate: Date,
    var expireDate: Date
)

data class SubscriptionWithPlan(
    var subscriptionId: SubscriptionPlan,
    var usedBookCredit : Int?,
    var startDate: Date,
    var expireDate: Date
)


@Parcelize
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
    var bookCredit : Int? = null

) : BaseModel(_id) , Parcelable{
    companion object{
        const val SUBSCRIPTION_LEVEL_1 = 1      // only access subscription service and regular price purchase
        const val SUBSCRIPTION_LEVEL_2 = 2      // access subscription service plus discount purchase
        const val SUBSCRIPTION_LEVEL_3 = 3      // access subscription service , discount purchase and credit purchase
    }
}



data class SubscriptionPaymentInfo(
    var userId : String,
    var subscriptionId : String,
    var paymentMethod : String

)

