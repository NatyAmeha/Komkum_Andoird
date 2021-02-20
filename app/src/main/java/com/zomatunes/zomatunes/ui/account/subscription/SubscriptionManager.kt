package com.zomatunes.zomatunes.ui.account.subscription

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.auth0.android.jwt.JWT
import com.zomatunes.zomatunes.data.api.UserApi
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.toResource
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.errors.InvalidPaymentException
import com.yenepaySDK.model.OrderedItem
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject


@ActivityScoped
class SubscriptionManager @Inject constructor (var userApi: UserApi, @ApplicationContext var context: Context) {
    var preference = PreferenceHelper.getInstance(context)

    companion object {
        val YENEPAY_PAYMENT_METHOD = "YENEPAY"
        val PAYPAL_PAYMENT_METHOD = "PAYPAL"
        val NO_PAYMENT_METHOD = "NOPAYMENT"
    }


    suspend fun yenePaySubscriptionHandler(activityContext : Context, user: User, subscriptionInfo: SubscriptionPlan, PaymentMethod: String) : String {
         try {
//            var subscriptionData = SubscriptionPaymentInfo(user._id!! , SubscriptionId , PaymentMethod)
//            var result = userApi.addSubscription(subscriptionData).toResource()

             var subscriptionData = SubscriptionPaymentInfo(user._id!! , subscriptionInfo._id!! , PaymentMethod)
             var result = userApi.addSubscription(subscriptionData).toResource()
             return result.data!!
//            if(PaymentMethod == "YENEPAY") {
//                yenepayPayment(activityContext, user._id!!, subscriptionInfo)
//            }
//            else{
//                var subscriptionData = SubscriptionPaymentInfo(user._id!! , subscriptionInfo._id!! , PaymentMethod)
//                var result = userApi.addSubscription(subscriptionData).toResource()
////                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
////                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
////                AccountState.ValidSubscription()
//            }



        } catch (ex: Throwable) {
            Log.i("paymenterrormanager", ex.message)
//            AccountState.InvalidSubscription()
             return ""
        }


        //register subscription expirey data

    }

    suspend fun verifySubscription(paymentInfo : VerifyPayment) : AccountState{
       try{
           var result = userApi.verifyPayment(paymentInfo).toResource()
           var token = result.data!!
           var user = decodetoken(token)
           var accountstateresult = user.subscription?.subscriptionId?.let {
               preference[AccountState.TOKEN_PREFERENCE] = token
               preference[AccountState.USER_ID] = user._id ?: ""
               preference[AccountState.EMAIL] = user.email ?: ""
               preference[AccountState.USERNAME] = user.username ?: ""
               preference[AccountState.PROFILE_IMAGE] = user.profileImagePath ?: ""

               preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
               return@let AccountState.ValidSubscription(user = user, token = token)
           }
           return accountstateresult ?: AccountState.NoSubscription(user = user, message = "Invalid  Subscription")
       }catch (ex : Exception){
           return AccountState.NoSubscription(message = "Invalid  Subscription")
       }

    }

    fun yenepayPayment(context : Context , userId : String , subscriptionInfo : SubscriptionPlan){
        var merchantId = "0235"
        var orderId = "${subscriptionInfo._id}_$userId"
        var orderItem = OrderedItem(subscriptionInfo._id , subscriptionInfo.name ,  1 , subscriptionInfo.priceInBirr!!.toDouble())
        var successUrl = "http://192.168.1.3:4000/api/yenepay/succussurl"
        var returnUrl = "com.zomatunes.yenepay:/payment2redirect"
        var failureUrl = "http://192.168.1.3:4000/api/yenepay/failureurl"

        var paymentOrder = PaymentOrderManager(merchantId , orderId)
        paymentOrder.paymentProcess = PaymentOrderManager.PROCESS_EXPRESS
        paymentOrder.isUseSandboxEnabled = true
        paymentOrder.isShoppingCartMode = false
        paymentOrder.returnUrl = returnUrl
        try {
            paymentOrder.addItem(orderItem)
            paymentOrder.startCheckout(context)

        }catch (ex : InvalidPaymentException){
            Toast.makeText(context , ex.message , Toast.LENGTH_LONG).show()
        }
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
        var fullSubscriptionInfo = jwt.claims.get("realSubscriptionInfo")?.asObject(SubscriptionPlan::class.java)
        Log.i("subscription" , fullSubscriptionInfo.toString())
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