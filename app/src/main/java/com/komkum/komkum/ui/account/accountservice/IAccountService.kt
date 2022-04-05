package com.komkum.komkum.ui.account.accountservice

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.auth0.android.jwt.JWT
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.komkum.komkum.data.api.UserApi
import com.komkum.komkum.data.model.*
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.extensions.toResource
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.dataRequestHelper
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

interface IAccountService {
    suspend fun register(user: User): AccountState
    suspend fun authenticate(authData: AuthenticationModel): AccountState
}



class EmailAccountService @Inject constructor(var userApi: UserApi, @ApplicationContext var context: Context) : IAccountService {
    var preference = PreferenceHelper.getInstance(context)
    var error = MutableLiveData<String>()
    var check = "check"

    suspend fun checkEmailExistence(authModel : AuthenticationModel) : Resource<Boolean>{
        return try {
            var result = userApi.checkEmail(authModel).toResource()
            result
        }catch (ex : Throwable){
            Resource.Success(false)
        }
    }

    suspend fun checkEmailExistenceBeta(authModel: AuthenticationModel) = dataRequestHelper(authModel ,fun(message : String) = error.postValue(message)){
        userApi.checkEmail(it!!)
    }

    // deprecated
    override suspend fun register(userData: User): AccountState {
        return try {
            var result = userApi.registerUser(userData).toResource()
            if(result.data != null){
                preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE
                AccountState.Registered(user = result.data!! , null)
            }
            else AccountState.UnRegistered("Error occured")
//
//            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
//            preference[AccountState.TOKEN_PREFERENCE] = token
//            preference[AccountState.USER_ID] = user._id ?: ""
//            preference[AccountState.EMAIL] = user.email ?: ""
//            preference[AccountState.USERNAME] = user.username ?: ""
//            preference[AccountState.PROFILE_IMAGE] = user.profileImagePath ?: ""

        } catch (ex: Throwable) {
            AccountState.UnRegistered(ex.message)
        }
    }


    suspend fun signUpWithPhone(userData: User): AccountState {
        return try {
            var result = userApi.signUp(userData).toResource()

            if(result.data != null){
                var user = decodetoken(result.data!!)

                preference[AccountState.TOKEN_PREFERENCE] = result.data!!
                preference[AccountState.USER_ID] = user._id ?: ""
                preference[AccountState.EMAIL] = user.email ?: ""
                preference[AccountState.USERNAME] = user.username ?: ""
                preference[AccountState.PHONE_NUMBER] = user.phoneNumber ?: ""
                preference[AccountState.PROFILE_IMAGE] = user.profileImagePath ?: ""
                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
                preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE

                if(!user.subscription?.subscriptionId.isNullOrEmpty())
                    preference[AccountState.USED_AUDIOBOOK_CREDIT] = user.subscription?.usedBookCredit ?: 0

                return AccountState.Registered(user = user , result.data!!)

            }
            else AccountState.UnRegistered("Error occured")

        } catch (ex: Throwable) {
            error.postValue(ex.message)
            AccountState.UnRegistered(ex.message)
        }
    }


    override suspend fun authenticate(authData: AuthenticationModel): AccountState {
        try {
            val result = userApi.authenticate(authData).toResource()
            var token = result.data!!
            var user = decodetoken(token)

            Log.i("userinfo" , user.toString())
            preference[AccountState.TOKEN_PREFERENCE] = token
            preference[AccountState.USER_ID] = user._id ?: ""
            preference[AccountState.EMAIL] = user.email ?: ""
            preference[AccountState.USERNAME] = user.username ?: ""
            preference[AccountState.PHONE_NUMBER] = user.phoneNumber ?: ""
            preference[AccountState.PROFILE_IMAGE] = user.profileImagePath ?: ""
            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
            preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE

           if(!user.subscription?.subscriptionId.isNullOrEmpty())
                preference[AccountState.USED_AUDIOBOOK_CREDIT] = user.subscription?.usedBookCredit ?: 0

            return AccountState.LoggedIn(user, token)

        } catch (ex: Throwable) {
            Toast.makeText(context , ex.message , Toast.LENGTH_LONG).show()
            return AccountState.LoggedOut(ex.message)
        }
    }


    suspend fun continueWithFacebook(userInfo: User , signupSource : String?) : AccountState{
        var response = dataRequestHelper(userInfo , fun(message : String) = ""){
            userApi.continueWithFacebook(it!!)
        }
        Log.i("resultdata" , response.data.toString())
        return if(response.data != null){
            var decodedUserResult  = decodetoken(response.data!!.token)
            preference[AccountState.TOKEN_PREFERENCE] = response.data!!.token
            preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE

            preference[AccountState.USER_ID] = decodedUserResult._id ?: ""
            preference[AccountState.EMAIL] = decodedUserResult.email ?: ""
            preference[AccountState.USERNAME] = decodedUserResult.username ?: ""
            preference[AccountState.PHONE_NUMBER] = decodedUserResult.phoneNumber ?: ""
            preference[AccountState.PROFILE_IMAGE] = decodedUserResult.profileImagePath ?: ""
            preference[AccountState.USED_AUDIOBOOK_CREDIT] = decodedUserResult.subscription?.usedBookCredit ?: 0

            if(response.data!!.isNewUser){
                Firebase.analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP){
                    param(FirebaseAnalytics.Param.METHOD,  FcmService.F_EPV_FACEBOOK)
                    param(FcmService.F_EP_SIGN_UP_SOURCE , signupSource ?: FcmService.F_EPV_ORGANIC)
                }
                preference[PreferenceHelper.WALLET_BONUS_DIALOG_AFTER_SIGN_UP] = true
            }

            AccountState.LoggedIn(decodedUserResult, response.data!!.token)
        }
        else AccountState.LoggedOut("Unable to authenticate with facebook")
    }


    fun decodetoken(token: String): User {
        var jwt = JWT(token)
//        Log.i("jwt" , jwt.id ?: "")
//        Log.i("jwtissuer" , jwt.issuer ?: "")
//        Log.i("jwtsubject" , jwt.subject ?: "")
//        Log.i("jwtsignature" , jwt.signature ?: "")
//        Log.i("jwtclaims" , jwt.claims.toString() ?: "")
//        Log.i("jwtexpiresat" , jwt.expiresAt.toString() ?: "")
//        Log.i("jwtheader" , jwt.header.toString() ?: "")
//        Log.i("jwt" , jwt.notBefore.toString() ?: "")

        var tokenexpireDate = jwt.claims.get("exp")?.asDate()
        var subscription = jwt.claims.get("subscription")?.asObject(Subscription::class.java)
        Log.i("newsubsc" , subscription.toString())
        var fullSubscriptionInfo = jwt.claims.get("realSubscriptionInfo")?.asObject(SubscriptionPlan::class.java)
        var email = jwt.claims.get("email")?.asString()
        var username = jwt.claims.get("username")?.asString()
        var phoneNumber = jwt.claims.get("phoneNumber")?.asString()
        var _id = jwt.claims.get("_id")?.asString()
        var profileImagePath = jwt.claims.get("profileImagePath")?.asString()

        subscription?.let {
            return User(_id = _id ,  subscription = it, username = username, phoneNumber = phoneNumber ,  email = email, tokenExpiredata = tokenexpireDate , profileImagePath = profileImagePath)
        }
        return User(_id = _id , username = username, email = email, tokenExpiredata = tokenexpireDate , profileImagePath = profileImagePath)

    }
}


class FacebookAccountService(var userApi: UserApi, var context: Context) : IAccountService {
    override suspend fun register(user: User): AccountState {
        return AccountState.LoggedOut()
    }

    override suspend fun authenticate(authData: AuthenticationModel): AccountState {
        return AccountState.LoggedOut()
    }

}

