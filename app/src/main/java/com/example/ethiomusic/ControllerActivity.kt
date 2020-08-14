package com.example.ethiomusic

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.ethiomusic.data.model.Subscription
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.PreferenceHelper.get
import com.example.ethiomusic.util.PreferenceHelper.set
import com.example.ethiomusic.util.extensions.sendIntent
import com.yenepaySDK.PaymentOrderManager
import com.yenepaySDK.PaymentResponse
import com.yenepaySDK.YenePayPaymentActivity
import com.yenepaySDK.handlers.PaymentHandlerActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

open class ControllerActivity : AppCompatActivity() {

    fun hideBottomView() {
        var animation =
            android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
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

    fun fakeSubscriptionChecking(context: Context): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var checksubs = preference.get(
            AccountState.SUBSCRIPTON_PREFERENCE,
            AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
        )
        checksubs?.let {
            return it == AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
        }
        return false
    }


    fun fakeLoginCheckin(context: Context): Boolean {
        var preference = PreferenceHelper.getInstance(context)
        var checksubs =
            preference.get(AccountState.LOGIN_PREFERENCE, AccountState.LOGOUT_PREFERENCE_VALUE)
        checksubs?.let {
            return it == AccountState.LOGIN_PREFERENCE_VALUE
        }
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
        var registrationValue = preference.get<Int>(
            AccountState.REGISTRATION_PREFERENCE,
            AccountState.UNREGISTERED_PREFERENCE_VALUE
        )
        registrationValue?.let {
            return when (it) {
                AccountState.REGISTERED_PREFERENCE_VALUE -> true
                else -> false
            }
        }
        return false
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





//    override fun onPaymentResponseArrived(response: PaymentResponse?) {
//        var  preference = PreferenceHelper.getInstance(this)
//        response?.let {
//            if (it.isPaymentCompleted) {
//
//                preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                preference["PAY"] = true
////                if (isHalfRegistered(this)) {
////                    Toast.makeText(this , "completed" , Toast.LENGTH_LONG).show()
////
////                } else {
////                    Toast.makeText(this , "Payment error or completed" , Toast.LENGTH_LONG).show()
////                    preference["PAY"] = true
////                    sendIntent(MainActivity::class.java)
////                }
//            }
//            else {
//                Toast.makeText(this , "Payment error" , Toast.LENGTH_LONG).show()
//                preference["PAY"] = false
//            }
//        }
//    }
//
//    override fun onPaymentResponseError(error: String?) {
//        var  preference = PreferenceHelper.getInstance(this)
//        Toast.makeText(this , "Payment response error" , Toast.LENGTH_LONG).show()
//        preference["PAY"] = false
//    }



}
