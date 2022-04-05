package com.komkum.komkum.ui.user

import android.content.Context
import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.*
import com.komkum.komkum.data.viewmodel.RewardInfo
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.usecase.PaymentUsecase
import com.komkum.komkum.usecase.WalletUsecase
import kotlinx.coroutines.launch
import okhttp3.MultipartBody


class UserViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle , var userRepo : UserRepository , var songRepo : SongRepository,
                                                 var playlistRepo : PlaylistRepository , var artistRepo : ArtistRepository ,  var paymentManager: PaymentManager,
var walletUsecase: WalletUsecase , var paymentUsecase: PaymentUsecase) : ViewModel() {

    var error = MutableLiveData<String>()
    var userData = MutableLiveData<UserWithSubscription>()

    var publicPlaylist = MutableLiveData<List<Playlist<String>>>()
    var artistList = MutableLiveData<List<Artist<String, String>>>()

    var allRewards = MutableLiveData<List<RewardInfo>>()


    var friendsActivity = MutableLiveData<FriendActivity>()

    fun getUserData() = viewModelScope.launch {
        var result = userRepo.getUser().data
        if(result != null) userData.value = result!!
    }

    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(userRepo.error){ error.value = it}
        a.addSource(playlistRepo.error){ error.value = it}
        a.addSource(artistRepo.error){ error.value = it}
        a.addSource(walletUsecase.error){ error.value = it}
        return a
    }

    fun removeOldErrors(){
        songRepo.error.value = null
        userRepo.error.value = null
        playlistRepo.error.value = null
        artistRepo.error.value = null
        walletUsecase.error.value = null

    }


    fun getTotalGameReward() = viewModelScope.launch {
        var result = userRepo.getGameAndCommissionRewardAmount().data
        if(result != null) allRewards.value = result!!
    }



    fun uploadImage(file: MultipartBody.Part) = liveData {
        var result = userRepo.uploadImage(file).data
        emit(result)
    }


    fun saveUserFriendsData(userId : String , friendsId : List<String>) = liveData{
        var result = userRepo.getUserFacebookFriendsData(friendsId).data
        result?.let {
            var friendsInfo = it.map { userinfo ->
                Friend(null ,  userinfo._id!! , userId)
            }
            var insertedIds = userRepo.savedUserFriendsToDb(friendsInfo)
            emit(insertedIds)
        }
    }

//    fun makeDonationUsingYenePay(price : Double, context : Context, orderId : String, orderName : String, errorCallback : (message : String?) -> Unit){
//        paymentManager.purchaseUsingYenepay(price , orderId , orderName , context , errorCallback)
//    }
//
//    fun makeDonation(donation: Donation) = liveData {
//         userRepo.makeDonation(donation).data?.let {
//             emit(it)
//         }
//    }


    fun getUserFriendsData(userId : String) = liveData {
        var friendsList = userRepo.getFacebookFriendsFromDb(userId)
        friendsList?.let {
            var friendsId = it.map { friend -> friend.friendId }.toList()
            var result = userRepo.getUserInfo(friendsId).data ?: emptyList()
            return@liveData emit(result)
        }
        emit(emptyList())
    }

    suspend fun getUserFbFriendActivity(userId : String){
        friendsActivity.value = userRepo.getUserFacebookFriendActivity(userId).data ?: null
    }

    fun updateUserInfo(user : UserWithSubscription) = liveData {
        var result = userRepo.updateUser(user).data ?: false
        emit(result)
    }

    fun getUserPublicPlaylist(userId : String) = viewModelScope.launch {
        publicPlaylist.value  =  playlistRepo.getUserPublicPlaylist(userId).data ?: null
    }

    fun getFavoriteArtist(userId : String) = viewModelScope.launch {
        var result = artistRepo.getFavoriteArtist(userId).data
        artistList.value = result ?: null
    }

    fun getBookIntereset() = liveData {
        var result = songRepo.getBrowseCategories("GENRE").data
        Log.i("browseitem" , result?.size.toString())
        emit(result)
    }

    fun getBrowseLists() = liveData{
        var result =  songRepo.getBrowseCategories("ALL").data
       emit(result)
    }


    fun startPaymentForWalletRecharge(amount: Int , id : String , context: Context) = viewModelScope.launch{
        var result = paymentUsecase.initYenePayFlow(amount , id , context)
    }


    fun getWalletBalance() = liveData {
        var result = walletUsecase.getWalletBalance()
        emit(result)
    }

    fun getWalletTransactions() = liveData {
        var result = walletUsecase.getWalletTransactions()
        emit(result)
    }

    fun makeDonation(donationInfo : Donation) = liveData {
        var result = walletUsecase.makeDonation(donationInfo)
        emit(result)
    }




}