package com.komkum.komkum.data.repo

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.komkum.komkum.util.Resource
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject


class BillingRepository @Inject constructor(@ActivityContext var context : Context) :
    PurchasesUpdatedListener , ConsumeResponseListener , BillingClientStateListener , SkuDetailsResponseListener , PurchaseHistoryResponseListener {

    lateinit  var billingClient : BillingClient


    var error = MutableLiveData<String>()
    var productType : String = BillingClient.SkuType.INAPP
    lateinit var productId : String
    lateinit var selectedProductName : String
    var skuDetail : SkuDetails? = null

    var PurchaseStatus = MutableLiveData<Resource<Purchase>>()

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
                launchBillingFlow(skuDetaild)
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
           BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->  PurchaseStatus.value =  Resource.Error(error = "This device not supported Google play billing")
           BillingClient.BillingResponseCode.USER_CANCELED -> PurchaseStatus.value =  Resource.Error(error = "Payment cancelled")
           BillingClient.BillingResponseCode.ERROR -> PurchaseStatus.value =  Resource.Error(error = "Unkown error")
           BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->{
               Log.i("billing purchase alrdy" , p1?.size.toString())
               //query allpurchases and find purchase with prouct identifier from db
//               var result = billingClient.queryPurchases(productType)

//               PurchaseStatus.value = Resource.Success(message = "You are not charged for this purchase")
               PurchaseStatus.value =  Resource.Error(error = "Please try again after few minute. payment will refund if there is incorrect payment.")

               if(!p1.isNullOrEmpty()){
                   PurchaseStatus.value =  Resource.Error(error = "You are not charged for this activity")
                   for (i in p1) verifyPurchase(i)
               }
               else Log.i("billing purchase update" , "no puchase found in the list")

           }
           BillingClient.BillingResponseCode.SERVICE_TIMEOUT ->PurchaseStatus.value =  Resource.Error(error = "Service timeout. please try again")


       }
    }

    override fun onConsumeResponse(p0: BillingResult, p1: String) {
        Log.i("billing consume" , p0.responseCode.toString())
        if(p0.responseCode == BillingClient.BillingResponseCode.OK)
            Toast.makeText(context , "downloaded or subscribed", Toast.LENGTH_LONG).show()
    }


    fun verifyPurchase(purchase : Purchase) {
        Log.i("billing purchase state" , purchase.purchaseState.toString())
         if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
            PurchaseStatus.value = Resource.Success(data = purchase)
            // verify on serve
         }
         else if(purchase.purchaseState == Purchase.PurchaseState.PENDING){
             //save purchase token alongside with productIdentifier to the database
             Log.i("billing purchase save" , purchase.purchaseToken)

             PurchaseStatus.value = Resource.Error(error = "Your payment is pending. Download automaticaly started after payment is completed." +
                     "If Payment is deducted without downloading, the payment is refunded")

         }

         else PurchaseStatus.value =  Resource.Error(error = "unable to verify purchase on server")
    }

    suspend fun consumePurchases(purchaseToken : String) : ConsumeResult{
        var consumeParam = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken).build()
//        var a = billingClient.queryPurchases("")
//        var b = a.purchasesList?.get(0)?.purchaseState
        return billingClient.consumePurchase(consumeParam )
    }

    suspend fun acknowledgePurchases(purchaseToken : String): BillingResult {
        var params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build()
        return billingClient.acknowledgePurchase(params )
    }


    fun initAndLaunchBillingClient(id : String, type : String){
        productType = type
        productId = id
        billingClient = BillingClient.newBuilder(context).setListener(this).enablePendingPurchases().build()
        billingClient.startConnection(this)
//        return if(true){
//
//            true
//        } else{
//            error.value = "Unbale to connect to google play"
//            false
//        }
    }

    fun endClientConnection(){
        billingClient.endConnection()
    }

    fun launchBillingFlow(selectedSku : SkuDetails ){
        var params = BillingFlowParams.newBuilder().setSkuDetails(selectedSku).build()
        billingClient.launchBillingFlow(context as Activity, params)
    }

    fun queryProducts(){
        var skuParams = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(productId)).setType(productType).build()
        billingClient.querySkuDetailsAsync(skuParams , this)
    }

    override fun onPurchaseHistoryResponse(p0: BillingResult, p1: MutableList<PurchaseHistoryRecord>?) {

    }


}