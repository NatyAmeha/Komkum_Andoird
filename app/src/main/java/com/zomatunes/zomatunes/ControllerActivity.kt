package com.zomatunes.zomatunes

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.ui.book.BookViewModel
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.delete
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.google.android.material.snackbar.Snackbar
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.PaymentResponse
import com.yenepaySDK.YenePayPaymentActivity
import com.yenepaySDK.handlers.PaymentHandlerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
@AndroidEntryPoint
open class ControllerActivity : YenePayPaymentActivity() {

    val activityviewmodel : OnboardingActivityViewmodel by viewModels()
    val bookViewmodel : BookViewModel by viewModels()

    var selectedEbookForPurchase : EBook<Author<String, String>>? = null
    var selectedAudioBookForPurchase : Audiobook<Chapter, Author<String, String>>? = null
    lateinit var viewContainer : View

    fun hideBottomView() {
        var animation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        player_card.animation = animation
        bottom_nav_view.animation = animation

        player_card.visibility = View.GONE
        bottom_nav_view.visibility = View.GONE
    }

    fun hideplayer() {
        player_card.visibility = View.GONE
    }

    fun showBottomView() {
        player_card.visibility = View.VISIBLE
        bottom_nav_view.visibility = View.VISIBLE
    }

    fun hasValidSubscription(context: Context, subscription: Subscription? = null): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var currentDate = Date().time
        var invalidDate = Date().apply {
            month -= 2
        }

        subscription?.let {
            var expiredata = subscription.expireDate.time
            if (currentDate <= expiredata) {
                preference[AccountState.SUBSCRIPTON_PREFERENCE] =
                    AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                return true
            }
            preference[AccountState.SUBSCRIPTON_PREFERENCE] =
                AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
            return false
        }

        var expireDate =
            preference.get(AccountState.SUBSCRIPTION_EXPIRE_DATE_PREFERENCE, invalidDate.time)
        expireDate?.let {
            if (currentDate <= it) {
                return true
            }
        }

        preference[AccountState.SUBSCRIPTON_PREFERENCE] =
            AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
        preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
        return false
    }



    fun isValidUser(context: Context): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var loginPreference = preference.get(AccountState.LOGIN_PREFERENCE, AccountState.LOGOUT_PREFERENCE_VALUE)
        return if (loginPreference == AccountState.LOGIN_PREFERENCE_VALUE) {
            var subscriptionPreference = preference.get(AccountState.SUBSCRIPTON_PREFERENCE, AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE)

            return if (subscriptionPreference == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE) true
            else {
                preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
                false
            }
        } else false
    }

    fun Logout(context: Context) {
        var invalidDate = Date()
        invalidDate.month -= 2
        var preference = PreferenceHelper.getInstance(context)
        preference[AccountState.TOKEN_EXPIRE_DATE_PREFERENCE] = invalidDate.time
        preference[AccountState.TOKEN_PREFERENCE] = AccountState.INVALID_TOKEN_PREFERENCE_VALUE
        preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGOUT_PREFERENCE_VALUE
        preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE

        preference.delete(AccountState.USER_ID)
        preference.delete(AccountState.USERNAME)
        preference.delete(AccountState.EMAIL)
        preference.delete(AccountState.PROFILE_IMAGE)
        preference.delete(AccountState.USED_AUDIOBOOK_CREDIT)
    }


    fun isHalfRegistered(context: Context): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var registrationValue = preference.get(AccountState.REGISTRATION_PREFERENCE, AccountState.UNREGISTERED_PREFERENCE_VALUE)
        registrationValue?.let {
            return when (it) {
                AccountState.HALF_REGISTERED_PREFERENCE_VALUE -> true
                else -> false
            }
        }
        return false
    }


    fun isFullyRegistered(context: Context): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var registrationValue = preference.get(AccountState.REGISTRATION_PREFERENCE, AccountState.UNREGISTERED_PREFERENCE_VALUE)
        return when (registrationValue) {
            AccountState.REGISTERED_PREFERENCE_VALUE -> true
            else -> false
        }
    }

    fun updateToFullyRegisteredAccount(context: Context) {
        var preference = PreferenceHelper.getInstance(context)
        preference[AccountState.REGISTRATION_PREFERENCE] = AccountState.REGISTERED_PREFERENCE_VALUE
        preference[AccountState.LOGIN_PREFERENCE] = AccountState.LOGIN_PREFERENCE_VALUE
//        preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
    }

    fun updateToHalfRegisteredAccount(context: Context) {
        var preference = PreferenceHelper.getInstance(context)
        preference[AccountState.REGISTRATION_PREFERENCE] =
            AccountState.HALF_REGISTERED_PREFERENCE_VALUE

    }


    override fun onDestroy() {
        super.onDestroy()
    }



    override fun onPaymentResponseArrived(response: PaymentResponse?) {
        var  preference = PreferenceHelper.getInstance(this)
        response?.let {
            if (it.isPaymentCompleted) {
                var yenepayPaymentFor = preference.get("PAYMENT_FOR" , -1)
                when(yenepayPaymentFor){

                    PaymentManager.PAYMENT_FOR_SUBSCRIPTION -> {
                        var userId = preference.get("SUB_USER_ID" , "")
                        var subscriptionId = preference.getString("SUB_SUBSCRIPTION_ID" , "")
                        var verifyPaymentInfo = VerifyPayment(userId , subscriptionId ,  VerifyPayment.SUBSCRIPTION_PAYMENT_TYPE)
                        activityviewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(this , Observer{ accountState ->
                            when(accountState){
                                is AccountState.ValidSubscription ->{
                                    activityviewmodel.user = accountState.user
                                    preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                                    if(isHalfRegistered(this)){
                                        findNavController(R.id.onboarding_navhost).navigate(R.id.categorySelectionFragment)
                                    }
                                    else{
                                        sendIntent(MainActivity::class.java)
                                    }
                                }
                                is AccountState.NoSubscription ->{
                                    Toast.makeText(this , "${accountState.message} subscription" , Toast.LENGTH_LONG).show()
                                }
                            }
                        })
                    }

                    PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE ->{
                        selectedAudioBookForPurchase?.let {
                            bookViewmodel.downloadAudiobook(it , this).observe(this , Observer{
                                if(it) showSnacbar2("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 1)
                                    findNavController(R.id.nav_host_fragment).navigate(R.id.downloadListFragment , bundle)
                                }
                            })
                        }

                    }

                    PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE -> {
                        var bookId = preference.get("BOOK_ID" , "")
                        selectedEbookForPurchase?.let {
                            bookViewmodel.downloadEbook(it).observe(this , Observer{
                                if(it) showSnacbar2("Download added to the task" , "view"){
                                    var bundle = bundleOf("SELECTED_PAGE" to 2)
                                    findNavController(R.id.nav_host_fragment).navigate(R.id.downloadListFragment , bundle)
                                }
                            })
                        }

                    }

                    else -> {}
                }
            }
            else {
                Toast.makeText(this , "Payment error From Activity" , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPaymentResponseError(error: String?) {
        var  preference = PreferenceHelper.getInstance(this)
        Toast.makeText(this , "Payment not completed : $error" , Toast.LENGTH_LONG).show()
    }


    fun showSnacbar2(message : String , commandString: String? = null , action : (() -> Unit)? = null){
        Snackbar.make(viewContainer , message , Snackbar.LENGTH_LONG)
            .setAction(commandString){
                if (action != null)  action()
            }
            .show()
    }


}
