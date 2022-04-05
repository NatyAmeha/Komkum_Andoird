package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Keep
@Parcelize
data class Destination(
    var src: String? = null,
    var srcLocation: @RawValue GeoSpacial? = null,
    var dest: String? = null,
    var destLocation: @RawValue GeoSpacial? = null,
    var deliveryTime: Int? = null,   // number of days
    var smallPackagePrice: Float? = null,
    var mediumPackagePrice: Float? = null,
    var largePackagePrice: Float? = null
) : Parcelable
