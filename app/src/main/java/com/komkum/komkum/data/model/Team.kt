package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


import java.util.*


@Parcelize
@Keep
data class Team<P>(
    var _id: String? = null,
    var name: String? = null,
    var desc: String? = null,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var closed: Boolean? = null,
    var type: Int? = null,
    var active : Boolean = false,
    var products: List<@RawValue P>? = null,
    var creator: String? = null,
    var creatorName: String? = null,
    var additionalQty : Int? = 0 , // value came from package
    var teamSize: Int? = null,
    var members: List<@RawValue TeamMember>? = null,
    var city : String? = null,
    var location: @RawValue GeoSpacial? = null,
    var ad : Ads? = null,
    var duration : Int? = null

) : Parcelable{
    companion object{
        const val  SINGLE_PRODUCT_TEAM = 0
        const val MULTI_PRODUCT_TEAM = 1
        const val TRIVIA_TEAM = 2
        const val DEFAULT_TEAM = 3
    }
}

@Parcelize
@Keep
data class TeamMember(
    var user: String? = null,
    var username: String? = null,
    var image : String? = null,
    var joined: Boolean? = false,
    var ordered: Boolean? = false,
    var reward: List<String>? = null,
    var date: Date? = null,

    var answers : List<@RawValue Answer>? = null,
    var result : Int? = null,
    var played : Boolean? = null,
    var rewarded : Boolean? = null
) : Parcelable

@Parcelize
@Keep
data class Answer(
    var question : String? = null,
    var value : Int? = null
) : Parcelable

@Keep
@Parcelize
data class ProductTeam(var id :  String? = null , var expDate : Date? = null) : Parcelable


@Keep
data class TeamList(
    var nearTeamList : List<Team<Product>>? = null,
    var teamList : List<Team<Product>>? = null,
    var userTeams : List<Team<Product>>? = null,
    var userCollections : List<Team<Product>>? = null,
    var gameTeams : List<Team<Product>>? = null,
    var expiredTeams : List<Team<Product>>? = null
)
