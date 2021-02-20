package com.zomatunes.zomatunes.ui.user

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.ArtistRepository
import com.zomatunes.zomatunes.data.repo.PlaylistRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.data.repo.UserRepository
import kotlinx.coroutines.launch


class UserViewModel @ViewModelInject constructor(@Assisted var savedState : SavedStateHandle , var userRepo : UserRepository , var songRepo : SongRepository,
                                                 var playlistRepo : PlaylistRepository , var artistRepo : ArtistRepository) : ViewModel() {

    var error = MutableLiveData<String>()

    var user = userRepo.getUser()
    var publicPlaylist = MutableLiveData<List<Playlist<String>>>()
     var artistList = MutableLiveData<List<Artist<String, String>>>()

    var friendsActivity = MutableLiveData<FriendActivity>()

    fun getError() : MediatorLiveData<String>{
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(userRepo.error){ error.value = it}
        a.addSource(playlistRepo.error){ error.value = it}
        a.addSource(artistRepo.error){ error.value = it}
        return a
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


    fun getUserFriendsData(userId : String) = liveData {
        var friendsList = userRepo.getFacebookFriendsFromDb(userId)
        friendsList?.let {
            var friendsId = it.map { friend -> friend.friendId }
            var result = userRepo.getUserInfo(friendsId).data ?: emptyList()
            return@liveData emit(result)
        }
        emit(emptyList())
    }

    suspend fun getUserFbFriendActivity(userId : String){
        friendsActivity.value = userRepo.getUserFacebookFriendActivity(userId).data
    }

    fun updateUserInfo(user : UserWithSubscription) = liveData {
        var result = userRepo.updateUser(user).data ?: false
        emit(result)
    }

    fun getUserPublicPlaylist(userId : String) = viewModelScope.launch {
        publicPlaylist.value  =  playlistRepo.getUserPublicPlaylist(userId).data
    }

    fun getFavoriteArtist(userId : String) = viewModelScope.launch {
        var result = artistRepo.getFavoriteArtist(userId).data
        artistList.value = result
    }

    fun getBookIntereset() = liveData {
        var result = songRepo.getBrowseCategories("GENRE").data
        Log.i("browseitem" , result?.size.toString())
        emit(result)
    }

}