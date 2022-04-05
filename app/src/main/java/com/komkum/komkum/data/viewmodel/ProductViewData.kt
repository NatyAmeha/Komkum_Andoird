package com.komkum.komkum.data.viewmodel

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

class ProductViewData()

@Parcelize
@Keep
data class ReviewViewData(
   var totalReview : Int? = null,
   var totalRating : Float? = null,
   var fiveStar : Int? = null,
   var  fourStar: Int? = null,
   var  threeStar: Int? = null,
   var  twoStar: Int? = null,
   var  oneStar: Int? = null
) : Parcelable


