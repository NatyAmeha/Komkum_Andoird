package com.zomatunes.zomatunes.ui.account.accountservice

import android.content.Context
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.model.AuthenticationModel
import com.zomatunes.zomatunes.data.model.Subscription
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.di.ActivityScope
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import com.zomatunes.zomatunes.util.extensions.exhaustive
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ActivityScoped
class UserManager @Inject constructor (var accountService: EmailAccountService , @ApplicationContext var context: Context , var userApi: UserApi) {
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
