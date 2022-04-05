package com.komkum.komkum

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.*
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.account.accountservice.UserManager
import com.komkum.komkum.ui.account.subscription.SubscriptionManager
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import okhttp3.MultipartBody
import javax.inject.Inject

class OnboardingActivityViewmodel @ViewModelInject constructor(@Assisted var savedState: SavedStateHandle, var userManager: UserManager, var userRepo : UserRepository,
                                                                   var subscriptionManager: SubscriptionManager, var subscriptionrepo : SubscriptionRepository,
                                                                   var artistRepo : ArtistRepository, var songRepo : SongRepository , var paymentManager: PaymentManager ,
var billingRepo : BillingRepository) : ViewModel() {

    val firebaseAnalytics = Firebase.analytics
    var googlePlaysubscriptionPurchaseState = billingRepo.PurchaseStatus

    var signupSource : String? = null

    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String> {
        var a = MediatorLiveData<String>()
        a.addSource(songRepo.error){ error.value = it }
        a.addSource(userRepo.error){error.value = it}
        a.addSource(artistRepo.error){error.value = it}
        a.addSource(userManager.error){error.value = it}
        a.addSource(subscriptionrepo.error){ error.value = it}
        a.addSource(billingRepo.error){ error.value = it}
        return a
    }

    fun removeOldErrors(){
        userRepo.error.value = null
        subscriptionrepo.error.value = null
        userManager.error.value = null
        billingRepo.error.value = null

    }


    var a = userManager.getCheck()
    val categoryRecyclerviewStateManager: RecyclerviewStateManager<String> by lazy { RecyclerviewStateManager<String>() }


    fun checkEmail(authModel : AuthenticationModel) = liveData {
        var result = userManager.checkEmail(authModel).data
        emit(result)
    }

    var user : User? = null


     fun registrationRequest() = liveData {
         var result = userManager.registerUser(user!!)
         emit(result)
     }

    fun signUpWithPhoneNumber(userInfo: User) = liveData {
        var result = userManager.signUpWithPhoneNumber(userInfo)
        emit(result)
    }


    fun forgotPassword(authModel: AuthenticationModel) = userRepo.fogotPassword(authModel)
    fun resetPassword(authModel: AuthenticationModel , token : String) = userRepo.resetPassword(authModel , token)


//    var subscriptionList = liveData {
//        var result = subscriptionrepo.getSubscriptionsBeta().data
//        emit(result)
//    }


    fun subscriptionRequest(context : Context , subscriptionInfo: SubscriptionPlan , paymentmethod : String) = liveData {
          user?.let {
              var result = subscriptionManager.yenePaySubscriptionHandler(context , it , subscriptionInfo , paymentmethod)
              emit(result)
          }
    }

    fun verifySubscriptionPayment(paymentInfo : VerifyPayment) = liveData {
        try{
            var result = subscriptionManager.verifySubscription(paymentInfo)
            emit(result)
        }catch (ex : Throwable){
            error.value = ex.message
        }

    }

    fun authRequest(data : AuthenticationModel) = liveData {
        var result = userManager.authenticateUser(data)
        emit(result)
    }

    fun continueWithFacebook(user : User , signupSource : String? = FcmService.F_EPV_ORGANIC) = liveData {
        var result = userManager.continueWithFacebook(user , signupSource)
        emit(result)
    }

    fun isSubscriptionValid() = liveData {
        var result = userRepo.checkSubscriptionFromServer()
        emit(result.data)
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

    fun purchaseUsingGooglePlay(productId : String , productType : String){
        billingRepo.initAndLaunchBillingClient(productId , productType)
    }

    fun acknowledgeSubscription(purchaseToken : String) = liveData{
        var result = billingRepo.acknowledgePurchases(purchaseToken)
        emit(result)
    }

}

