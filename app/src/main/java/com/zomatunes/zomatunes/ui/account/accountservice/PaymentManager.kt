package com.zomatunes.zomatunes.ui.account.accountservice

import android.content.Context
import android.util.Log
import com.auth0.android.jwt.JWT
import com.zomatunes.zomatunes.data.model.Payment
import com.zomatunes.zomatunes.data.model.SubscriptionPlan
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.errors.InvalidPaymentException
import com.yenepaySDK.model.OrderedItem
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class PaymentManager @Inject constructor(@ApplicationContext  var context : Context) {
    var preference = PreferenceHelper.getInstance(context)

    companion object{
        const val PAYMENT_FOR_SUBSCRIPTION = 0
        const val PAYMENT_FOR_EBOOK_PURCHASE = 1
        const val PAYMENT_FOR_AUDIOBOOK_PURCHASE = 2
    }

    fun getFullSubscriptionInfo(token : String) : SubscriptionPlan?{
        var jwt = JWT(token)
        return jwt.claims.get("realSubscriptionInfo")?.asObject(SubscriptionPlan::class.java)
    }

    fun handlePaymentConfiguration(bookBrirrPrice : Float , bookDollarPrice : Float) : Payment?{
        var token = preference.get(AccountState.TOKEN_PREFERENCE, AccountState.INVALID_TOKEN_PREFERENCE_VALUE)
        if(bookBrirrPrice.toInt() == 0 || bookDollarPrice.toInt() == 0){
            return Payment(Payment.PAYMENT_TYPE_FREE , 0f , 0f)
        }
        else {
            getFullSubscriptionInfo(token)?.let {
                when(it.level){
                    SubscriptionPlan.SUBSCRIPTION_LEVEL_2 ->{
                        var modifiedPriceInBirr =bookBrirrPrice.minus(bookBrirrPrice * it.discount!! /100)
                        var modifiedPriceInDollar = bookDollarPrice.minus (bookDollarPrice * it.discount!! /100)
                        var payment = Payment(Payment.PAYMENT_TYPE_DISCOUNT , modifiedPriceInBirr , modifiedPriceInDollar)
                        return@handlePaymentConfiguration payment
                    }
                    SubscriptionPlan.SUBSCRIPTION_LEVEL_3 ->{
                        var usedAudiobookCredit = preference.get(AccountState.USED_AUDIOBOOK_CREDIT , 0)
                        Log.i("oldcredit" , usedAudiobookCredit.toString())
                        if(it.bookCredit!! > 0 && usedAudiobookCredit < it.bookCredit!!){
                            var payment = Payment(Payment.PAYMENT_TYPE_FREE , 0f , 0f)
                            return@handlePaymentConfiguration payment
                        }
                        else{
                            var modifiedPriceInBirr = bookBrirrPrice.minus(bookBrirrPrice * it.discount!! /100)
                            var modifiedPriceInDollar = bookDollarPrice.minus (bookDollarPrice * it.discount!! /100)
                            var payment = Payment(Payment.PAYMENT_TYPE_DISCOUNT , modifiedPriceInBirr , modifiedPriceInDollar)
                            return@handlePaymentConfiguration payment
                        }
                    }
                    else ->{
                        var payment = Payment(Payment.PAYMENT_TYPE_REGULAR , bookBrirrPrice , bookDollarPrice)
                        return@handlePaymentConfiguration payment
                    }
                }
            }
            return null
        }

    }

    fun purchaseUsingYenepay(price : Double , orderId : String , orderName : String , activity : Context ,  errorCallback : (message : String?) -> Unit){
        var merchantId = "0235"
        var orderItem = OrderedItem(orderId , orderName ,  1 , price)
        var successUrl = "http://192.168.1.3:4000/api/yenepay/succussurl"
        var returnUrl = "com.zomatunes.zomatunes.yenepay:/payment2redirect"
        var failureUrl = "http://192.168.1.3:4000/api/yenepay/failureurl"

        var paymentOrder = PaymentOrderManager(merchantId , orderId)
        paymentOrder.paymentProcess = PaymentOrderManager.PROCESS_EXPRESS
        paymentOrder.isUseSandboxEnabled = true
        paymentOrder.isShoppingCartMode = false
        paymentOrder.returnUrl = returnUrl
        try {
            paymentOrder.addItem(orderItem)
            paymentOrder.startCheckout(activity)

        }catch (ex : InvalidPaymentException){
            errorCallback(ex.message)
        }
    }
}