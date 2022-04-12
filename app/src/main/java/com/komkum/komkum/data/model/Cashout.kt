package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class Cashout(
    var _id : String? = null,
    var user: String? = null,
    var amount: Int? = null,
    var phoneNumber: String? = null,
    var cashoutMethod: String? = null,
    var date: Date? = null,
    var status : Int? = null
) : Parcelable{
    companion object{
        const val CASHOUT_PENDING = 0
        const val CASHOUT_APPROVED = 1
        const val CASHOUT_DECLINED = 2
    }
}
