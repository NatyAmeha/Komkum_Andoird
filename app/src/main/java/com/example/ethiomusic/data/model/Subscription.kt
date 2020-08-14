package com.example.ethiomusic.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

data class Subscription(
    var subscriptionId: String,
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
    var dayLength: Int? = null,
    var isActivated: Boolean? = null

) : BaseModel(_id) , Parcelable

data class SubscriptionPaymentInfo(
    var userId : String,
    var subscriptionId : String,
    var paymentMethod : String

)

