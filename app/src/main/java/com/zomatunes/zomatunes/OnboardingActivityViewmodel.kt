package com.zomatunes.zomatunes

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.ArtistRepository
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.data.repo.SubscriptionRepository
import com.zomatunes.zomatunes.data.repo.UserRepository
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.ui.account.accountservice.UserManager
import com.zomatunes.zomatunes.ui.account.subscription.SubscriptionManager
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import javax.inject.Inject

class OnboardingActivityViewmodel @ViewModelInject constructor(@Assisted var savedState: SavedStateHandle, var userManager: UserManager, var userRepo : UserRepository,
                                                                   var subscriptionManager: SubscriptionManager, var subscriptionrepo : SubscriptionRepository,
                                                                   var artistRepo : ArtistRepository, var songRepo : SongRepository , var paymentManager: PaymentManager) : ViewModel() {


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


    fun forgotPassword(authModel: AuthenticationModel) = userRepo.fogotPassword(authModel)
    fun resetPassword(authModel: AuthenticationModel , token : String) = userRepo.resetPassword(authModel , token)


    var subscriptionList = liveData {
        var result = subscriptionrepo.getsubscriptions()
        emit(result)
    }


    fun subscriptionRequest(context : Context , subscriptionInfo: SubscriptionPlan , paymentmethod : String) = liveData {
          user?.let {
              var result = subscriptionManager.yenePaySubscriptionHandler(context , it , subscriptionInfo , paymentmethod)
              emit(result)
          }
    }

    fun verifySubscriptionPayment(paymentInfo : VerifyPayment) = liveData {
        var result = subscriptionManager.verifySubscription(paymentInfo)
        emit(result)
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

    fun updateUserAndFollowArtists(userInfo : User, artistList : List<String>) =  liveData{
//        var result = userManager.updateUserInfo(userInfo)
        var artistFollowResult =  artistRepo.followArtist(artistList)
        emitSource(artistFollowResult)
    }

    fun handleyenepayPayment(price : Double , orderId : String , orderName : String , context : Context){
        paymentManager.purchaseUsingYenepay(price , orderId , orderName , context){}
    }

}

