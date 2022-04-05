package com.komkum.komkum.data.repo

import androidx.lifecycle.MutableLiveData
import com.komkum.komkum.data.api.UserApi
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.RewardInfo
import com.komkum.komkum.ui.account.accountservice.UserManager
import com.komkum.komkum.util.extensions.apiDataRequestHelper
import com.komkum.komkum.util.extensions.dataRequestHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class UserRepository @Inject constructor(var userApi: UserApi , var appDb: AppDb , var userManager : UserManager) {
    var error = MutableLiveData<String>()


    suspend fun uploadImage(file: MultipartBody.Part) = dataRequestHelper(file , fun(message : String) = error.postValue(message)){
        userApi.uploadImage(it!!)
    }


    fun fogotPassword(info : AuthenticationModel) = apiDataRequestHelper(info , fun(message : String) = error.postValue(message)){
        userApi.forgotPassword(it!!)
    }

    suspend fun checkSubscriptionFromServer() = dataRequestHelper<Any , Boolean>( null, fun(message : String) = error.postValue(message)){
        userApi.checkSubscription()
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

    suspend fun getUser() = dataRequestHelper<Any , UserWithSubscription>(errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getUser()
    }

    suspend fun makeDonation(donation: Donation) = dataRequestHelper(donation , fun(message : String) = error.postValue(message)){
        userApi.makeDonation(donation)
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



    suspend fun getAdvertisement(limit : Int , type : Int? = null) = dataRequestHelper(limit , fun(message : String) = error.postValue(message)){
        userApi.getAds(it!! , type)
    }

    suspend fun getWishList() = dataRequestHelper(null , fun(message : String) = error.postValue(message)){
        userApi.getUserWishlists()
    }

    suspend fun addProductToWishlist(productId : String) = dataRequestHelper(productId , fun(message : String) = error.postValue(message)){
        userApi.addtoWishlist(it!!)
    }

    suspend fun removeFromWishlist(productId : String) = dataRequestHelper(productId , fun(message : String) = error.postValue(message)){
        userApi.removeFromWishlist(it!!)
    }

    suspend fun creatNewShippingAddress(addressInfo : Address) = dataRequestHelper(addressInfo , fun(message : String) = error.postValue(message)){
        userApi.addNewAddress(it!!)
    }

    suspend fun getShippingAndDeliveryInfo(pIds : String , addressId : String? = null) = dataRequestHelper(pIds ,  fun(message : String) = error.postValue(message)){
        userApi.getUserAddress(it!! , addressId)
    }

    suspend fun getUserOrderHistory() = dataRequestHelper<Any , List<Order<Product , String>>>( errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getUserOrders()
    }

    suspend fun getCommissionAmount() = dataRequestHelper<Any , Double>( errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getCommissionAmount()
    }

    suspend fun getGameAndCommissionRewardAmount() = dataRequestHelper<Any , List<RewardInfo>>( errorHandler = fun(message : String) = error.postValue(message)){
        userApi.getRewardAmount()
    }


}