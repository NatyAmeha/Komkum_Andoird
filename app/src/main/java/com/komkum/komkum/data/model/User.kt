package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import java.util.*
import javax.annotation.Nonnull

@Parcelize
@Keep
data class User(
    var _id: String? = null,
    var username: String? = null,
    var email: String? = null,
    var emailVerfied: Boolean? = null,

    var password: String? = null,
    var passwordChangedAt: Date? = null,
    var passwordResetToken: String? = null,
    var passwordTokenExpireDate: Date? = null,

    var token: String? = null,
    var tokenExpiredata : Date? = null,

    var authModel: String? = null,

    var phoneNumber: String? = null,
    var profileImagePath: String? = null,

    var accountType: String? = null,
    var category: List<String>? = null,
    var tags: List<String>? = null,
    var bookGenre : List<String>? = null,
    var podcastCategory : List<String>? = null,

    var subscription: @RawValue Subscription? = null,

    var recent: List<RecentActivity>? = null,
    var library: @RawValue Library? = null,
    var downloads: @RawValue Download? = null,

    var facebookId : String? = null,
    var googleplayPurchaseToken : String? = null,

    var orders : List<@RawValue UserActivity>? = null,
    var wishlists : List<@RawValue UserActivity>? = null,
    var teams : List<@RawValue UserActivity>? = null,
    var addresses : List<@RawValue UserActivity>? = null,
    var cart : List<@RawValue OrderedItem<String>>? = null

    ) : BaseModel(_id)


@Parcelize
@Keep
data class UserWithSubscription(
    var _id: String? = null,
    var username: String? = null,
    var email: String? = null,
    var emailVerfied: Boolean? = null,

    var password: String? = null,
    var passwordChangedAt: Date? = null,
    var passwordResetToken: String? = null,
    var passwordTokenExpireDate: Date? = null,

    var token: String? = null,
    var tokenExpiredata : Date? = null,

    var authModel: String? = null,

    var phoneNumber: String? = null,
    var profileImagePath: String? = null,

    var accountType: String? = null,
    var category: MutableList<String>? = null,
    var tags: List<String>? = null,
    var bookGenre : MutableList<String>? = null,
    var podcastCategory : MutableList<String>? = null,
    var musicGenre : MutableList<String>? = null,


    var subscription: @RawValue SubscriptionWithPlan? = null,

    var recent: List<RecentActivity>? = null,
    var library: @RawValue Library? = null,
    var downloads: @RawValue Download? = null,

    var facebookId : String? = null,
    var googleplayPurchaseToken : String? = null,
    var walletBalance : Double? = null
) : Parcelable , BaseModel(_id)

@Entity
data class Friend(
    @PrimaryKey(autoGenerate = true)
    @Nonnull
    var _id : Int?,
    var friendId : String,
    var userId : String
)
@Keep
data class FriendActivity(
    var publicPlaylists : List<Playlist<String>>? = null ,
    var favoriteArtists : List<Artist<String , String>>? = null,
    var books : List<Book<String>>? = null,
    var favoriteAuthors : List<Author<String , String>>? = null
)



data class RegistrationModel(var username : String , var email: String , var password: String ,  var category: Array<String>? = null )
@Keep
data class AuthenticationModel(
    var email: String? = null, var password: String? =null , var phoneNumber : String? = null
)

@Keep
data class UserVerification(
    var userFound: Boolean = false , var phoneNumber: String?=null , var resetToken : String? =null
)

@Keep
data class FacebookAuthResponse(val token : String , var isNewUser : Boolean)


@Keep
data class FacebookUser(
    var _id : String,
    var name : String,
    var email : String? = null

)

@Keep
data class UserActivity(var id : String , var date : Date , var default : Boolean? = false)