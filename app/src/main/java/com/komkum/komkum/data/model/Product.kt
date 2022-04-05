package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep

import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*

//pid in metadata to product
//pId in product review to product
//pId in ordereditem to product

@Keep
@Parcelize
data class Product(
    var _id: String? = null,
    var title: String? = null,
    var sellerName: String? = null,
    var category: String? = null,
    var subcategory: String? = null,
    var tags: List<String>? = null,
    var date: Date? = null,
    var stdPrice: Double? = null,
    var dscPrice: Double? = null,
    var avgDeliveryDay : Int? = 1,
    var minQty : Int = 1,
    var refund : Boolean? = null,
    var totalSell: Int? = null,
    var gallery: List<@RawValue ProductGallery>? = null,

    var teams: List<@RawValue ProductTeam>? = null,
    var purchaseType: Int,
    var unit : String? = "unit"
) : Parcelable {
    companion object {
        const val STANDARD_PURCHASE = 0
        const val SINGLE_PRODUCT_TEAM_PURHASE = 1
        const val MULTI_PRODUCT_TEAM_PURCHASE = 2
        const val PACKAGE_PURCHASE = 3
    }

}

@Parcelize
@Keep
data class ProductMetadata<P, S>(
    var _id: String? = null,
    var product: @RawValue P? = null,
    var desc: List<String>? = null,
    var seller: @RawValue S? = null,
    var reviews: List<String>? = null,

    var teamPurchase : Boolean? = null,
    var teamSize: Int? = null,//
    var teamDuration: Int? = null, // number of days
    var skuOptions: Map<String, List<String>>? = null, //
    var skuComb: List<@RawValue SKUCombination>? = null,

    var shipping: List<@RawValue ProductSource>? = null, //
    var packageType: Int? = null,
    var stock: Int? = null,
    var variants: List<@RawValue P>? = null,
) : Parcelable {

}

@Parcelize
@Keep
data class ProductCategory(var subcategory: List<Category>) : Category(), Parcelable

@Parcelize
@Keep
open class Category(var name: String? = null, var image: String? = null) : Parcelable



@Parcelize
@Keep
data class SKUCombination(
    var value: Map<String, String>? = null,
    var stock: Int? = null,
    var addPrice: Int? = 0,
    var images: List<String>? = null
) : Parcelable



@Keep
@Parcelize
data class ProductGallery(var path: String? = null, var type: String? = null) : Parcelable

