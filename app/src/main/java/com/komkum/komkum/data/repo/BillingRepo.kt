package com.komkum.komkum.data.repo

import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class BillingRepo @Inject constructor(@ApplicationContext var context: Context) : BillingClientStateListener, PurchasesUpdatedListener,
    SkuDetailsResponseListener, ConsumeResponseListener {

    lateinit var playstoreBillingClient: BillingClient

    override fun onBillingSetupFinished(p0: BillingResult) {
        TODO("Not yet implemented")
    }

    override fun onBillingServiceDisconnected() {
        TODO("Not yet implemented")
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        TODO("Not yet implemented")
    }

    override fun onSkuDetailsResponse(p0: BillingResult, p1: MutableList<SkuDetails>?) {
        TODO("Not yet implemented")
    }

    override fun onConsumeResponse(p0: BillingResult, p1: String) {
        TODO("Not yet implemented")
    }


    private object GameSku {
        val GAS = "gas"
        val PREMIUM_CAR = "premium_car"
        val GOLD_MONTHLY = "gold_monthly"
        val GOLD_YEARLY = "gold_yearly"

        val INAPP_SKUS = listOf(GAS, PREMIUM_CAR)
        val SUBS_SKUS = listOf(GOLD_MONTHLY, GOLD_YEARLY)
        val CONSUMABLE_SKUS = listOf(GAS)
        //coincidence that there only gold_status is a sub
        val GOLD_STATUS_SKUS = SUBS_SKUS
    }


    private object RetryPolicies {
        private val maxRetry = 5
        private var retryCounter = AtomicInteger(1)
        private val baseDelayMillis = 500
        private val taskDelay = 2000L

        fun resetConnectionRetryPolicyCounter() {
            retryCounter.set(1)
        }

        fun connectionRetryPolicy(block: () -> Unit) {
            Log.d("connection retry", "connectionRetryPolicy")
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                val counter = retryCounter.getAndIncrement()
                if (counter < maxRetry) {
                    val waitTime: Long = (2f.pow(counter) * baseDelayMillis).toLong()
                    delay(waitTime)
                    block()
                }
            }
        }

        fun taskExecutionRetryPolicy(billingClient: BillingClient, listener: BillingRepo, task: () -> Unit) {
            val scope = CoroutineScope(Job() + Dispatchers.Main)
            scope.launch {
                if (!billingClient.isReady) {
                    Log.d("biiling repo", "taskExecutionRetryPolicy billing not ready")
                    billingClient.startConnection(listener)
                    delay(taskDelay)
                }
                task()
            }
        }

    }

}