package com.zomatunes.zomatunes.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import javax.annotation.Nonnull

@Parcelize
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

    var subscription: @RawValue Subscription? = null,

    var recent: List<RecentActivity>? = null,
    var library: @RawValue Library? = null,
    var downloads: @RawValue Download? = null,

    var facebookId : String? = null
) : BaseModel(_id)

@Parcelize
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
    var category: List<String>? = null,
    var tags: List<String>? = null,
    var bookGenre : List<String>? = null,

    var subscription: @RawValue SubscriptionWithPlan? = null,

    var recent: List<RecentActivity>? = null,
    var library: @RawValue Library? = null,
    var downloads: @RawValue Download? = null,

    var facebookId : String? = null
) : BaseModel(_id)

@Entity
data class Friend(
    @PrimaryKey(autoGenerate = true)
    @Nonnull
    var _id : Int?,
    var friendId : String,
    var userId : String
)

data class FriendActivity(
    var publicPlaylists : List<Playlist<String>>? = null ,
    var favoriteArtists : List<Artist<String , String>>? = null,
    var books : List<Book<String>>? = null,
    var favoriteAuthors : List<Author<String , String>>? = null
)



data class RegistrationModel(var username : String , var email: String , var password: String ,  var category: Array<String>? = null )

data class AuthenticationModel(
    var email: String? = null, var password: String?=null
)


data class FacebookAuthResponse(val token : String , var isNewUser : Boolean)

data class FacebookUser(
    var _id : String,
    var name : String,
    var email : String? = null

)