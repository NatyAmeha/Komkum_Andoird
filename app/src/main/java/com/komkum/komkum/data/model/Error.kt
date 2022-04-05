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
    }
}