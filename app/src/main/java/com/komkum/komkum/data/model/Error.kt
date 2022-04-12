package com.komkum.komkum.data.model

import androidx.annotation.Keep

@Keep
data class Error (
    var name : String?,
    var message : String?,
    var status : Int?
){
    companion object {
        const val PHONE_NUMBER_NOT_UNIQUE_ERROR_MSG = "Phone number is not unique"

        const val CASHOUT_REQUEST_ERROR_50_BIRR_MUST_BE_LEFT = "50 birr must be left on your wallet after cashout"
        const val CASHOUT__REQUEST_AMOUNT_0_ERROR = "The withdrawal amount must be greater than 0"
        const val CASHOUT__REQUEST_INSUFFIIENT_BALANCE = "You don't have enough balance to cashout"
        const val CASHOUT_REQUEST_ELIGABLITY_ERROR = "You can withdraw money only if you have more than ETB 500 on your wallet"

        const val CASHOUT_DUPLICATE_REQUEST_ERROR = "You can't request another cash out before the previous one is approved."
    }
}