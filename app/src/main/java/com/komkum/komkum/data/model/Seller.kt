package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.komkum.komkum.data.viewmodel.ReviewViewData
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Keep
data class Seller<P>(
    var _id : String? = null,
    var name: String? = null,
    var desc: String? = null,
    var products: List<@RawValue P>? = null,
    var images: List<String>? = null,
    var email: String? = null,
    var address: Address? = null,
    var reviewInfo : @RawValue ReviewViewData? = null
) : Parcelable
