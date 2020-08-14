package com.example.ethiomusic.data.model

import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*

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
    var category: Array<String>? = null,
    var tags: Array<String>? = null,

// trialAccount : Boolean

    var subscription: @RawValue Subscription? = null,

    var recent: List<RecentActivity>? = null,
    var library: @RawValue Library? = null,

    var downloads: @RawValue Download? = null
) : BaseModel(_id)


data class RegistrationModel(var username : String , var email: String , var password: String ,  var category: Array<String>? = null )

data class AuthenticationModel(
    var email: String, var password: String?=null
)

data class FacebookAuthResponse(val token : String , var isNewUser : Boolean)