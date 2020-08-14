package com.example.ethiomusic.ui.account.subscription

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.ethiomusic.ControllerActivity
import com.example.ethiomusic.data.api.UserApi
import com.example.ethiomusic.data.model.Subscription
import com.example.ethiomusic.data.model.SubscriptionPaymentInfo
import com.example.ethiomusic.data.model.SubscriptionPlan
import com.example.ethiomusic.data.model.User
import com.example.ethiomusic.di.ActivityScope
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.extensions.toResource
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.errors.InvalidPaymentException
import com.yenepaySDK.model.OrderedItem
import javax.inject.Inject


@ActivityScope
class SubscriptionManager @Inject constructor (var userApi: UserApi, var context: Context) {
    var preference = PreferenceHelper.getInstance(context)

    companion object {
        val YENEPAY_PAYMENT_METHOD = "YENEPAY"
        val PAYPAL_PAYMENT_METHOD = "PAYPAL"
        val NO_PAYMENT_METHOD = "NOPAYMENT"
    }


    suspend fun addSubscription(activityContext : Context , user: User, subscriptionInfo: SubscriptionPlan, PaymentMethod: String) {
         try {
//            var subscriptionData = SubscriptionPaymentInfo(user._id!! , SubscriptionId , PaymentMethod)
//            var result = userApi.addSubscription(subscriptionData).toResource()

            if(PaymentMethod == "YENEPAY") {
                yenepayPayment(activityContext, user._id!!, subscriptionInfo)
            }
            else{
                var subscriptionData = SubscriptionPaymentInfo(user._id!! , subscriptionInfo._id!! , PaymentMethod)
                var result = userApi.addSubscription(subscriptionData).toResource()
//                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
//                AccountState.ValidSubscription()
            }



        } catch (ex: Throwable) {
            Log.i("paymenterrormanager", ex.message)
//            AccountState.InvalidSubscription()
        }


        //register subscription expirey data

    }

    fun yenepayPayment(context : Context , userId : String , subscriptionInfo : SubscriptionPlan){
        var merchantId = "0235"
        var orderId = "${subscriptionInfo._id}_$userId"
        var orderItem = OrderedItem(subscriptionInfo._id , subscriptionInfo.name ,  1 , subscriptionInfo.priceInBirr!!.toDouble())
        var successUrl = "http://192.168.1.3:4000/api/yenepay/succussurl"
        var returnUrl = "com.example.ethiomusic.yenepay:/payment2redirect"
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


}