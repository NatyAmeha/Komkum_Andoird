package com.komkum.komkum.data.viewmodel

import android.os.Parcelable
import androidx.annotation.Keep
import com.komkum.komkum.data.model.*

import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Keep
data class ProductHomepageViewmodel(
    var trendingProducts : List<Product>? = null,

    var featuredProducts :  List<Product>? = null,
    var bestSellingProducts :  List<Product>? = null,
    var packages : List<ProductPackage<Product>>? = null,
    var trendingPakages : List<Team<Product>>? = null,
    var departments : List<ProductCategory>? = null,
    var returnGuaranteedProducts: List<Product>? = null,
    var newArrival : List<Product>? = null,
    var yourTeams : List<Team<Product >>? = null,
    var teamsexpireSoon : List<Team<Product>>? = null,  // list of teams those will expire soon
    var ads : List<Ads>? = null,
    var cartSize : Int? = 0
)

@Keep
@Parcelize
data class  ProductDetailViewmodel(
    val product : ProductMetadata<Product, String>? = null,
    val totalRating : Float? = 0f,
    val recentReviews : List<@RawValue Review>? = null,
    val relatedProducts : List<@RawValue Product>? = null,
    var favorite : Boolean = false
) : Parcelable


data class RewardInfo(
    var title : String? = null,
    var amount : Double? = null,
    var rewarded : Boolean? = null,
    var image : String? = null,
    var team : String? = null,
    var type : Int? = null
)
