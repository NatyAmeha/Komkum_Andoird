package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Keep
@Parcelize
data class Address(
    var _id : String? = null,
    var country: String? = null,
    var city: String? = null,
    var address: String? = null,
    var name : String? = null,
    var email: String? = null,
    var phone: String? = null,
    var nearLocation: String? = null,
    var default: Boolean? = null,
    var location: @RawValue GeoSpacial? = null
) : Parcelable

@Parcelize
@Keep
data class GeoSpacial(var type: String? = null, var coordinates: List<Double>? = null) : Parcelable

@Parcelize
@Keep
data class ProductSource(var location: Address? = null, var price: Float? = null ) : Parcelable
