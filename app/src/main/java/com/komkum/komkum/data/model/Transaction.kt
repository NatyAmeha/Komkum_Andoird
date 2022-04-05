package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class Transaction(
    var amount : Double? = null,
    var sourceName : String? = null,
    var recepientName : String? = null,
    var description : String? = null,
    var type : Int? = null,  // inclucing donation , product purchase,  recharge , cashout
    var created : Date? = null
) : Parcelable{
    companion object{
        const val TRANSACTION_TYPE_DEPOSIT = 0
        const val TRANSACTION_TYPE_WITHDRAWAL = 1
        const val TRANSACTION_TYPE_PURCHASE = 2
        const val TRANSACTION_TYPE_DONATION = 3
        const val TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE = 4
        const val TRANSACTION_TYPE_REWARD = 5
        const val TRANSACTION_TYPE_COMMISSION = 6


        //amount status flag
        const val AMOUNT_ADDED = 0
        const val AMOUNT_DEDUCTED = 1
    }
}
