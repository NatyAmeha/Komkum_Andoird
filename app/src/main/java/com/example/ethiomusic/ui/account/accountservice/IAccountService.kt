package com.example.ethiomusic.ui.account.accountservice

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.example.ethiomusic.data.api.UserApi
import com.example.ethiomusic.data.model.AuthenticationModel
import com.example.ethiomusic.data.model.FacebookAuthResponse
import com.example.ethiomusic.data.model.Subscription
import com.example.ethiomusic.data.model.User
import com.example.ethiomusic.di.ActivityScope
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.toResource
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.extensions.dataRequestHelper
import javax.inject.Inject

interface IAccountService {
    suspend fun register(user: User): AccountState
    suspend fun authenticate(authData: AuthenticationModel): AccountState
}


@ActivityScope
class EmailAccountService @Inject constructor(var userApi: UserApi, var context: Context) : IAccountService {
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
            return AccountState.HalfRegistered(user = user , token = token)
        } catch (ex: Throwable) {
            return AccountState.UnRegistered(ex.message)
        }
    }


    override suspend fun authenticate(authData: AuthenticationModel): AccountState {
        try {
            val result = userApi.authenticate(authData).toResource()
            var token = result.data!!
            var user = decodetoken(token)

            var accountstateresult = user.subscription?.subscriptionId?.let {
                preference[AccountState.TOKEN_PREFERENCE] = token
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
        var response = dataRequestHelper(userInfo , fun(message : String){}){
            userApi.continueWithFacebook(it!!)
        }
        var accountStateResult  = response.data?.let {
            var decodedUserResult  = decodetoken(it.token)

            return@let if(decodedUserResult.subscription?.subscriptionId.isNullOrEmpty()){
                AccountState.NoSubscription(user = decodedUserResult, message = "No Subscription")
            } else{
                if(it.isNewUser){preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.HALF_REGISTERED_PREFERENCE_VALUE}
                else{preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE}

                preference[AccountState.TOKEN_PREFERENCE] = it.token
                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
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


class FacebookAccountService(var userApi: UserApi, var context: Context) :
    IAccountService {
    override suspend fun register(user: User): AccountState {
        return AccountState.LoggedOut()
    }

    override suspend fun authenticate(authData: AuthenticationModel): AccountState {
        return AccountState.LoggedOut()
    }

}