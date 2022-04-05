package com.komkum.komkum.usecase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.errors.InvalidPaymentException
import com.yenepaySDK.model.OrderedItem
import com.komkum.komkum.data.model.Payment
import com.komkum.komkum.data.model.Transaction
import com.komkum.komkum.data.repo.PaymentRepository
import com.komkum.komkum.data.repo.SubscriptionRepository
import com.komkum.komkum.di.UsecaseModule.*
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import kotlin.properties.Delegates

interface IPayment{
    var paymentMethod : Int
    var paymentStatus : MutableLiveData<String>
    var error : MutableLiveData<String>
    var paymentCompletedListener : OnPaymentListener
    fun setPaymentListener(paymentLstnr : OnPaymentListener)

//    suspend fun pay(amount : Double  , action : (suspend () -> Unit)?) : Boolean?
    suspend fun initPayment(amount : Double , paymentType: Int , context : Context , id : String , name : String? = null , owner : LifecycleOwner? = null , action: (suspend () -> Unit)?)
    suspend fun completePayment(token : String? = null , paymentType: Int? = null)
    suspend fun initiatePaymentFlow(amount : Double, uniqueId : String, name : String, context: Context, action: (suspend () -> Unit)?)

    suspend fun startPaymentFlow(amount: Int, identifier: String , context: Context , errorCallback: (message: String?) -> Unit)

}


interface IPaymentVendor{
    suspend fun transferFrom(amount : Int , trIdentifier : String , context : Context)

}



class YenePayVendor @Inject constructor(var paymentRepo : PaymentRepository): IPayment , IPaymentVendor{

    override var error = MutableLiveData<String>()
    override var paymentMethod: Int = Payment.PAYMENT_METHOD_YENEPAY
    override lateinit var paymentStatus: MutableLiveData<String>
    override lateinit var paymentCompletedListener: OnPaymentListener

    override fun setPaymentListener(paymentLstnr: OnPaymentListener) {
        paymentCompletedListener = paymentLstnr
    }

//    override suspend fun pay(amount: Double, action: (suspend () -> Unit)?): Boolean? {
//        TODO("Not yet implemented")
//    }

    override suspend fun initPayment(
        amount: Double,
        paymentType: Int,
        context: Context,
        id: String,
        name: String?,
        owner: LifecycleOwner?,
        action: (suspend () -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun completePayment(token: String?, paymentType: Int?) {
        paymentRepo.getWalletTransactions()
    }


    override suspend fun initiatePaymentFlow(amount : Double , uniqueId : String , name : String , context: Context, action: (suspend () -> Unit)?) {
        if (action != null) action()
        var result = purchaseUsingYenepay(amount , uniqueId , name , context){
            error.value = it
        }
    }



    override suspend fun transferFrom(amount: Int, trIdentifier: String , context : Context) {
       var result = purchaseUsingYenepay(amount.toDouble() , trIdentifier+"uniqueid" , "ordername" , context){
           error.value = it
       }
    }

    override suspend fun startPaymentFlow(amount: Int, identifier: String, context: Context, errorCallback: (message: String?) -> Unit) {
        var result = purchaseUsingYenepay(amount.toDouble() , identifier , "ordername" , context){
            errorCallback(it)
        }
    }


    fun purchaseUsingYenepay(price : Double, orderId : String, orderName : String, activity : Context, errorCallback : (message : String?) -> Unit){
        var merchantId = "0783"  //"7354" //"2892"  // "0235"  //  7354
        var orderItem = OrderedItem(orderId , orderName ,  1 , price)
        var successUrl = "https://api.zomatunes.com/yenepay/succussurl"
        var returnUrl = "com.komkum.komkum.yenepay:/payment2redirect"
        var failureUrl = "https://api.zomatunes.com/yenepay/failureurl"

        var paymentOrder = PaymentOrderManager(merchantId , orderId)
        paymentOrder.paymentProcess = PaymentOrderManager.PROCESS_EXPRESS

        paymentOrder.isUseSandboxEnabled = false
        paymentOrder.isShoppingCartMode = false
        paymentOrder.returnUrl = returnUrl
        try {
            paymentOrder.addItem(orderItem)
            paymentOrder.openPaymentBrowser(activity)

        }catch (ex : InvalidPaymentException){
            errorCallback(ex.message)
        }
    }
}


class GooglePlayVendor @Inject constructor(@ActivityContext var actContext : Context) : PurchasesUpdatedListener, ConsumeResponseListener, BillingClientStateListener,
    SkuDetailsResponseListener, PurchaseHistoryResponseListener , AcknowledgePurchaseResponseListener  , IPayment{

    lateinit  var billingClient : BillingClient
    var productType : String = BillingClient.SkuType.INAPP
    lateinit var productId : String

    override var paymentMethod: Int = Payment.PAYMENT_METHOD_GOOGLE_PLAY
    override lateinit var paymentCompletedListener : OnPaymentListener
    override fun setPaymentListener(paymentLstnr : OnPaymentListener){
        paymentCompletedListener = paymentLstnr
    }

    override var paymentStatus = MutableLiveData<String>()
    override var error = MutableLiveData<String>()

//    override suspend fun pay(amount: Double, action: (suspend () -> Unit)?): Boolean? {
//        TODO("Not yet implemented")
//    }

    override suspend fun initPayment(
        amount: Double,
        paymentType: Int,
        context: Context,
        id: String,
        name: String?,
        owner : LifecycleOwner?,
        action: (suspend () -> Unit)?
    ) {
        paymentStatus.observe(owner!!){
            Log.i("billing observer" , it)
            paymentCompletedListener.onPaymentCompleted(this , amount , it)
        }

        productType = when(paymentType){
           Transaction.TRANSACTION_TYPE_SUBSCRIPTION_UPGRADE -> BillingClient.SkuType.SUBS
            else -> BillingClient.SkuType.INAPP
        }
        productId = id
        billingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        billingClient.startConnection(this)

    }


    override suspend fun completePayment(token: String? , paymentType: Int?) {
        if(productType == BillingClient.SkuType.SUBS) acknowledgePurchases(token!!)
        else if(productType == BillingClient.SkuType.INAPP) consumePurchases(token!!)

    }

    override suspend fun initiatePaymentFlow(
        amount: Double,
        uniqueId: String,
        name: String,
        context: Context,
        action: (suspend () -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun startPaymentFlow(
        amount: Int,
        identifier: String,
        context: Context,
        errorCallback: (message: String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }


    override fun onBillingSetupFinished(p0: BillingResult) {
        Log.i("billing setup" , p0.responseCode.toString() + "   " + p0.debugMessage)
        when (p0.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.i("billing setup", "onBillingSetupFinished successfully")
                queryProducts()

            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                //Some apps may choose to make decisions based on this knowledge.
                error.value = "The device not support this payment method"
                Log.i("billing unsupported", "onBillingSetupFinished but billing is not available on this device")
            }
            else -> {
                //do nothing. Someone else will connect it through retry policy.
                //May choose to send to server though
                error.value = "Billing failed please try again ${p0.responseCode} response code"
                Log.i("billing failed", "onBillingSetupFinished with failure response code: ${p0.responseCode}")
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        error.value = "billing disconnected  connect again"
        Log.i("billing disconnected" , "disconnected")

    }

    override fun onSkuDetailsResponse(p0: BillingResult, p1: MutableList<SkuDetails>?) {
        Log.i("billing sku list" , p1?.size.toString())
        if(p0.responseCode != BillingClient.BillingResponseCode.OK){
            error.value = p0.debugMessage

        }
        else{
            if(!p1.isNullOrEmpty()){
                var  skuDetaild = p1[0]
                Log.i("billing first sku" , skuDetaild.title)
                var params = BillingFlowParams.newBuilder().setSkuDetails(skuDetaild).build()
                billingClient.launchBillingFlow(actContext as Activity, params)
            }
            else  error.value = "Unable to fetch products ${p0.responseCode}"
        }
    }


    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        Log.i("billing purchase update" , p0.responseCode.toString()+ "  " + p0.debugMessage.toString())

        when(p0.responseCode){
            BillingClient.BillingResponseCode.OK -> {
                if(!p1.isNullOrEmpty()) for (i in p1) verifyPurchase(i)
                else Log.i("billing purchase update" , "no puchase found in the list")
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->  error.value =  "This device not supported Google play billing"
            BillingClient.BillingResponseCode.USER_CANCELED -> error.value = "Payment cancelled"
            BillingClient.BillingResponseCode.ERROR -> error.value = "Unkown error"
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->{
                Log.i("billing purchase alrdy" , p1?.size.toString())
                //query allpurchases and find purchase with prouct identifier from db
//               var result = billingClient.queryPurchases(productType)

//               PurchaseStatus.value = Resource.Success(message = "You are not charged for this purchase")
                error.value = "Please try again after few minute. payment will refund if there is incorrect payment."

                if(!p1.isNullOrEmpty()){
                    for (i in p1) verifyPurchase(i)
                }
                else Log.i("billing purchase update" , "no puchase found in the list")

            }
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> error.value = "Service timeout. please try again"


        }
    }

    override fun onConsumeResponse(p0: BillingResult, p1: String) {
        Log.i("billing consume" , p0.responseCode.toString())
        if(p0.responseCode != BillingClient.BillingResponseCode.OK) error.value = "Unable to consume payment"
    }


     fun verifyPurchase(purchase : Purchase) {
        Log.i("billing purchase state" , purchase.purchaseState.toString())
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
            paymentStatus.value = purchase.purchaseToken
            // verify on serve
        }
        else if(purchase.purchaseState == Purchase.PurchaseState.PENDING){
            //save purchase token alongside with productIdentifier to the database
            Log.i("billing purchase save" , purchase.purchaseToken)
            error.value = "Your payment is pending. Download automaticaly started after payment is completed.\" +\n" +
                    "                    \"If Payment is deducted without downloading, the payment is refunded"
        }

        else error.value = "Unable to verify purchase"
    }


    fun consumePurchases(purchaseToken : String) {
        var consumeParam = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken).build()
//        var a = billingClient.queryPurchases("")
//        var b = a.purchasesList?.get(0)?.purchaseState
         billingClient.consumeAsync(consumeParam , this )
    }

    fun acknowledgePurchases(purchaseToken : String) {
        var params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.acknowledgePurchase(params , this )
    }



    fun endClientConnection(){
        billingClient.endConnection()
    }



    fun queryProducts(){
        var skuParams = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(productId)).setType(productType).build()
        billingClient.querySkuDetailsAsync(skuParams , this)
    }

    override fun onPurchaseHistoryResponse(p0: BillingResult, p1: MutableList<PurchaseHistoryRecord>?) {

    }

    override fun onAcknowledgePurchaseResponse(p0: BillingResult) {
        if(p0.responseCode != BillingClient.BillingResponseCode.OK) error.value = "Unable to acknowladge payment"
    }
}



interface OnPaymentListener {
     fun onPaymentCompleted(paymentMethod : IPayment , amount : Double , token : String? = null)
}



class PaymentUsecase @Inject constructor(@Yenepay var yenePay : IPayment) {
    var error = MutableLiveData<String>()
    var transactionType by Delegates.notNull<Int>()
    lateinit var productIdentifier : String



    // depreciated
    suspend fun initiateYenePayPayment(amount : Double, id : String, name : String, context: Context, action: (() -> Unit)?){
//        yenePay.initiatePaymentFlow(amount , id , name , context){
//            action?.let { it() }
//        }
    }

    suspend fun initYenePayFlow(amount: Int, identifier: String , context: Context){
        yenePay.startPaymentFlow(amount , identifier , context){
            it?.let { error.value = it }
        }
    }



    suspend fun initPaymentFlow(payment : IPayment , payType: Int, amount : Double, id : String, context: Context, identifierName : String, owner : LifecycleOwner, paymentListener : OnPaymentListener ){
        productIdentifier = id
        transactionType = payType
        payment.setPaymentListener(paymentListener)
        payment.initPayment(amount , transactionType , context , productIdentifier , identifierName , owner){}
    }
}











class SubscriptionUsecase @Inject constructor(var subscriptionRepo : SubscriptionRepository) {
    var error = subscriptionRepo.error
    var paymentType by Delegates.notNull<Int>()
    lateinit var subscriptionId : String

    suspend fun initPaymentForSubscription(payment : IPayment, amount : Double, subId : String, context: Context, payType: Int, paymentListener : OnPaymentListener ){
        subscriptionId = subId
        paymentType = payType
        payment.setPaymentListener(paymentListener)
        payment.initPayment(amount , Payment.PAYMENT_TYPE_SUBSCRIPTION , context , subscriptionId){}
    }

}