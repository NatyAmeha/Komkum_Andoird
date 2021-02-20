package com.zomatunes.zomatunes.data.repo

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.preference.Preference
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.db.AppDb
import com.zomatunes.zomatunes.data.model.AuthenticationModel
import com.zomatunes.zomatunes.data.model.Friend
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.data.model.UserWithSubscription
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.extensions.apiDataRequestHelper
import com.zomatunes.zomatunes.util.extensions.dataRequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(var userApi: UserApi , var appDb: AppDb) {
    var error = MutableLiveData<String>()


    fun fogotPassword(info : AuthenticationModel) = apiDataRequestHelper(info , fun(message : String) = error.postValue(message)){
        userApi.forgotPassword(it!!)
    }

    fun resetPassword(info : AuthenticationModel , token : String) = apiDataRequestHelper(info , fun(message : String) = error.postValue(message)){
        userApi.resetPassword(it!! , token)
    }

    suspend fun getUserFacebookFriendsData(friendsId: List<String>) = dataRequestHelper(friendsId , fun(message : String) = error.postValue(message)){
        userApi.getFacebookFriendsData(it!!)
    }


    suspend fun getUserInfo(friendsId: List<String>) = dataRequestHelper(friendsId , fun(message : String) = error.postValue(message)){
        userApi.getUsers(it!!)
    }

    suspend fun updateUser(user : UserWithSubscription) = dataRequestHelper(user , fun(message : String) = error.postValue(message)){
        userApi.updateUser(user)
    }

    fun getUser() = apiDataRequestHelper<Any , UserWithSubscription>(errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getUser()
    }

    suspend fun getUserFacebookFriendActivity(userId : String) = dataRequestHelper(userId , fun(message : String) = error.postValue(message)){
        userApi.getUserFbFriendActivity(it!!)
    }

    suspend fun savedUserFriendsToDb(friendsList : List<Friend>) : List<Long>?{
        return withContext(Dispatchers.IO){
           return@withContext appDb.friendsDao.addFriends(friendsList)
        }
    }

    suspend fun getFacebookFriendsFromDb(userId : String) : List<Friend>?{
       return withContext(Dispatchers.IO){
           return@withContext appDb.friendsDao.getFriends(userId)
       }
    }

    fun updateFcmToken(token : String) = apiDataRequestHelper(token , fun(message : String) = error.postValue(message)){
        userApi.updateFcmRegistrationToken(it!!)
    }
}