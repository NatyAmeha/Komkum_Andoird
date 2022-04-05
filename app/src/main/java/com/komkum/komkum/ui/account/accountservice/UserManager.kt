package com.komkum.komkum.ui.account.accountservice

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.AdViewProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.komkum.komkum.data.api.UserApi
import com.komkum.komkum.data.model.AuthenticationModel
import com.komkum.komkum.data.model.Subscription
import com.komkum.komkum.data.model.User
import com.komkum.komkum.di.ActivityScope
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import com.komkum.komkum.util.extensions.exhaustive
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


class UserManager @Inject constructor (var accountService: EmailAccountService , @ApplicationContext var context: Context , var userApi: UserApi) {
    var preference: SharedPreferences = PreferenceHelper.getInstance(context)
    var error = accountService.error

    suspend fun checkEmail(authModel : AuthenticationModel) = accountService.checkEmailExistenceBeta(authModel)
    suspend fun registerUser(user : User) = accountService.register(user)
    suspend fun signUpWithPhoneNumber(user : User) = accountService.signUpWithPhone(user)
    suspend fun continueWithFacebook(user: User , signupSource : String?) = accountService.continueWithFacebook(user , signupSource)

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

            var isvalid = preference.get(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)
            return isvalid == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
        }

        var subscriptionPreference=  preference.get(AccountState.SUBSCRIPTON_PREFERENCE , AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)
        return subscriptionPreference == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
    }

    fun getCheck() = accountService.check
}

class a : AdsLoader{
    override fun setPlayer(player: Player?) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun setSupportedContentTypes(vararg contentTypes: Int) {
        TODO("Not yet implemented")
    }

    override fun start(
        adsMediaSource: AdsMediaSource,
        adTagDataSpec: DataSpec,
        adsId: Any,
        adViewProvider: AdViewProvider,
        eventListener: AdsLoader.EventListener
    ) {
        TODO("Not yet implemented")
    }

    override fun stop(adsMediaSource: AdsMediaSource, eventListener: AdsLoader.EventListener) {
        TODO("Not yet implemented")
    }

    override fun handlePrepareComplete(
        adsMediaSource: AdsMediaSource,
        adGroupIndex: Int,
        adIndexInAdGroup: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun handlePrepareError(
        adsMediaSource: AdsMediaSource,
        adGroupIndex: Int,
        adIndexInAdGroup: Int,
        exception: IOException
    ) {
        TODO("Not yet implemented")
    }

}
