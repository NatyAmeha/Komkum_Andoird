package com.example.ethiomusic.ui.account

import com.example.ethiomusic.data.model.User
import java.util.*

sealed class AccountState(var user  : User? = null , var token : String? = null ,  var expireDate: Date? = null , var message : String? = null) {
    class UnRegistered(message : String? = null) : AccountState(message = message)
    class HalfRegistered(user : User? = null , token: String? = null) : AccountState(user = user , token = token)
    class Registered(user : User? = null , token: String? = null) :AccountState(user = user , token = token)

    class ValidSubscription(date: Date? = null ) : AccountState(expireDate = date)
    class InvalidSubscription(user : User? = null , message: String? = null) : AccountState(user = user ,  message = message)
    class NoSubscription( user : User? = null , message : String? = null) : AccountState(user , message)

    class LoggedIn(user : User , authToken : String) : AccountState(user = user ,  token = authToken)
    class LoggedOut(message : String? = null) : AccountState(message = message)

    companion object{
        val REGISTRATION_PREFERENCE = "IS_REGISTERED"
        val LOGIN_PREFERENCE = "IS_LOOGGEDIN"
        val SUBSCRIPTON_PREFERENCE = "IS_SUBSCRIPTION_VALID"
        val TOKEN_PREFERENCE = "TOKEN"
        val TOKEN_EXPIRE_DATE_PREFERENCE = "TOKEN_EXPIRE"
        val SUBSCRIPTION_EXPIRE_DATE_PREFERENCE = "SUBSCRIPTION_EXPIRE"

       val REGISTERED_PREFERENCE_VALUE = 1
        val HALF_REGISTERED_PREFERENCE_VALUE = 2
        val UNREGISTERED_PREFERENCE_VALUE = 3


        val VALID_SUBSCRIPTION_PREFENRECE_VALUE = 4
        val INVALID_SUBSCRIPTION_PREFENRECE_VALUE = 5
        val NO_SUBSCRIPTION_PREFENRECE_VALUE = 6

        val LOGIN_PREFERENCE_VALUE = 7
        val LOGOUT_PREFERENCE_VALUE = 8

        val INVALID_TOKEN_PREFERENCE_VALUE = "Invlid Token"
        val TOKEN_EXPIRE_DATE_PREFERENCE_VALUE = 10

    }
}

