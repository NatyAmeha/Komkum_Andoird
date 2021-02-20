package com.zomatunes.zomatunes.ui.account.accountservice

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.extensions.toResource
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

interface IAccountService {
    suspend fun register(user: User): AccountState
    suspend fun authenticate(authData: AuthenticationModel): AccountState
}


@ActivityScoped
class EmailAccountService @Inject constructor(var userApi: UserApi, @ApplicationContext var context: Context) : IAccountService {
    var preference = PreferenceHelper.getInstance(context)

    var check = "check"

    suspend fun checkEmailExistence(authModel : AuthenticationModel) : Resource<Boolean>{
        return try {
            var result = userApi.checkEmail(authModel).toResource()
            result
        }catch (ex : Throwable){
            Resource.Success(false)
        }
    }

    override suspend fun register(userData: User): AccountState {
        return try {
            var result = userApi.registerUser(userData).toResource()
            var token = result.data!!
    //            var user = decodetoken(token)
            preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.HALF_REGISTERED_PREFERENCE_VALUE
            preference[AccountState.TOKEN_PREFERENCE] = token
            var user = decodetoken(token)
            AccountState.HalfRegistered(user = user , token = token)
        } catch (ex: Throwable) {
            AccountState.UnRegistered(ex.message)
        }
    }


    override suspend fun authenticate(authData: AuthenticationModel): AccountState {
        try {
            val result = userApi.authenticate(authData).toResource()
            var token = result.data!!
            var user = decodetoken(token)

            Log.i("userinfo" , user.toString())
            var accountstateresult = user.subscription?.subscriptionId?.let {
                preference[AccountState.TOKEN_PREFERENCE] = token

                preference[AccountState.USER_ID] = user._id ?: ""
                preference[AccountState.EMAIL] = user.email ?: ""
                preference[AccountState.USERNAME] = user.username ?: ""
                preference[AccountState.PROFILE_IMAGE] = user.profileImagePath ?: ""
                preference[AccountState.USED_AUDIOBOOK_CREDIT] = user.subscription?.usedBookCredit ?: 0

                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
                preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE
                return@let AccountState.LoggedIn(user, token)
            }
            return accountstateresult ?: AccountState.NoSubscription(user = user, message = "Invalid  Subscription")
        } catch (ex: Throwable) {
            return AccountState.LoggedOut(ex.message)
        }

    }


    suspend fun continueWithFacebook(userInfo: User) : AccountState{
        var response = dataRequestHelper(userInfo , fun(message : String) = ""){
            userApi.continueWithFacebook(it!!)
        }
        Log.i("resultdata" , response.data.toString())
        var accountStateResult  = response.data?.let {
            var decodedUserResult  = decodetoken(it.token)

            return@let if(it.isNewUser or (decodedUserResult.subscription?.subscriptionId == null)){
                preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.HALF_REGISTERED_PREFERENCE_VALUE
                AccountState.NoSubscription(user = decodedUserResult, message = "No Subscription")
            } else{
                preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE
                preference[AccountState.TOKEN_PREFERENCE] = it.token
                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE

                preference[AccountState.USER_ID] = decodedUserResult._id ?: ""
                preference[AccountState.EMAIL] = decodedUserResult.email ?: ""
                preference[AccountState.USERNAME] = decodedUserResult.username ?: ""
                preference[AccountState.PROFILE_IMAGE] = decodedUserResult.profileImagePath ?: ""
                preference[AccountState.USED_AUDIOBOOK_CREDIT] = decodedUserResult.subscription?.usedBookCredit ?: 0

                AccountState.LoggedIn(decodedUserResult, it.token)
            }
        }
        return accountStateResult ?: AccountState.LoggedOut("error occurred please try again")

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
        var _id = jwt.claims.get("_id")?.asString()
        var profileImagePath = jwt.claims.get("profileImagePath")?.asString()

        subscription?.let {
            return User(_id = _id, subscription = it, username = username, email = email, tokenExpiredata = tokenexpireDate , profileImagePath = profileImagePath)
        }
        return User(tokenExpiredata = tokenexpireDate)
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