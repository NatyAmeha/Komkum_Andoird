package com.example.ethiomusic

import android.content.Context
import android.telephony.SubscriptionInfo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.data.repo.ArtistRepository
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.data.repo.SubscriptionRepository
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.ui.account.accountservice.UserManager
import com.example.ethiomusic.ui.account.subscription.SubscriptionManager
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

class OnboardingActivityViewmodel(var savedState: SavedStateHandle, var userManager: UserManager ,
                                  var subscriptionManager: SubscriptionManager , var subscriptionrepo : SubscriptionRepository ,
                                  var artistRepo : ArtistRepository , var songRepo : SongRepository) : ViewModel() {


    var a = userManager.getCheck()
    val categoryRecyclerviewStateManager: RecyclerviewStateManager<String> by lazy { RecyclerviewStateManager<String>() }

    fun checkEmail(authModel : AuthenticationModel) = liveData {
        emit(Resource.Loading())
        var result = userManager.checkEmail(authModel)
        emit(result)
    }

    var user : User? = null


     fun registrationRequest() = liveData {
         var result = userManager.registerUser(user!!)
         emit(result)
     }


    var subscriptionList = liveData {
        var result = subscriptionrepo.getsubscriptions()
        emit(result)
    }


    fun subscriptionRequest(context : Context , subscriptionInfo: SubscriptionPlan , paymentmethod : String) = liveData {
          user?.let {
              var result = subscriptionManager.addSubscription(context , it , subscriptionInfo , paymentmethod)
              emit(result)
          }

      }

    fun authRequest(data : AuthenticationModel) = liveData {
        var result = userManager.authenticateUser(data)
        emit(result)
    }

    fun continueWithFacebook(user : User) = liveData {
        var result = userManager.continueWithFacebook(user)
        emit(result)
    }


//    suspend fun addSubscriptioin(paymentMethod : String){
//        var accountStateResult  = subscriptionManager.addSubscription(user!! , subscriptionInfo!!.subscriptionId , paymentMethod)
//    }


    var categoryList: List<String> = mutableListOf("GOSPEL", "SECULAR", "ORTODOX", "ISLAMIC")

    var popularArtistList = artistRepo.getPopularArtists()
    var newArtistsList = artistRepo.getNewArtists()

    fun addArtistToFavorite(artistList : List<String>) = artistRepo.followArtist(artistList)

}


class OnboardingActivityViewmodelFactory @Inject constructor(private var userManager: UserManager, private var subscriptionManager: SubscriptionManager ,
                                                             var subscriptionrepo : SubscriptionRepository ,  var artistRepo : ArtistRepository
                                                             , var songRepo : SongRepository) : IViewmodelFactory<OnboardingActivityViewmodel>{
    override fun create(savedStateHandle: SavedStateHandle): OnboardingActivityViewmodel {
        return OnboardingActivityViewmodel(savedStateHandle , userManager , subscriptionManager , subscriptionrepo , artistRepo , songRepo)
    }

}