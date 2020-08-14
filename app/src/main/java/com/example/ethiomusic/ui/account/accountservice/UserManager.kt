package com.example.ethiomusic.ui.account.accountservice

import android.content.Context
import com.example.ethiomusic.data.model.AuthenticationModel
import com.example.ethiomusic.data.model.Subscription
import com.example.ethiomusic.data.model.User
import com.example.ethiomusic.di.ActivityScope
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.get
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.extensions.exhaustive
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ActivityScope
class UserManager @Inject constructor (var accountService: EmailAccountService , var context: Context) {
    var preference = PreferenceHelper.getInstance(context)

    suspend fun checkEmail(authModel : AuthenticationModel) = accountService.checkEmailExistence(authModel)
    suspend fun registerUser(user : User) = accountService.register(user)

    suspend fun continueWithFacebook(user: User) = accountService.continueWithFacebook(user)

    suspend fun authenticateUser(authData : AuthenticationModel) = accountService.authenticate(authData)


    fun isLoggedIn() : Boolean{
        var loginPref = preference.get(AccountState.LOGIN_PREFERENCE , AccountState.LOGOUT_PREFERENCE_VALUE)
        return loginPref == AccountState.LOGIN_PREFERENCE_VALUE
    }

    fun isValidUser(subscriptionInfo: Subscription? = null) : Boolean{
        var loginPreference = preference.get(AccountState.LOGIN_PREFERENCE , AccountState.LOGOUT_PREFERENCE_VALUE)
        return if(loginPreference == AccountState.LOGIN_PREFERENCE_VALUE) hasValidSubscription(subscriptionInfo)
        else false
    }


    fun hasValidSubscription(subscription: Subscription? = null) : Boolean {
        subscription?.let {
            var currentDate = Date().time
            var expiredata = subscription.expireDate.time



            return if(currentDate <= expiredata) {
                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                true
            } else{
                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
                false
            }

        }

        var subscriptionPreference=  preference.get(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)

        return if(subscriptionPreference == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE) true
        else{
            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
            false
        }


    }

    fun getCheck() = accountService.check
}
