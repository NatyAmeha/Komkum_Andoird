package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Keep
@Parcelize
data class ProductPackage<P>(
    var _id: String? = null,
    var name : String? = null,
    var products : List<@RawValue P>? = null,
    var category : String? = null,
    var tags : List<String>? = null,
    var additionalQty : Int? = 0,
    var priority : Int? = null,
    var teamSize : Int? = null,
    var teamDuration : Int? = null,
    var active : Boolean? = true
) : Parcelable
