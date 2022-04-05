package com.komkum.komkum

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.komkum.komkum.data.model.VerifyPayment
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.account.subscription.SubscriptionFragment
import com.komkum.komkum.util.IYenepayPaymentHandler
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.sendIntent
import com.komkum.komkum.util.extensions.showLanguageSelelectDialog
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.notification.FcmService
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class OnboardingActivity : ControllerActivity() {


    val viewmodel : OnboardingActivityViewmodel by viewModels()


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        findNavController(R.id.onboarding_navhost).handleDeepLink(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppthemeLight)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding)

        //get signup source transferred from mainactivity
        viewmodel.signupSource = intent?.getStringExtra(FcmService.F_EP_SIGN_UP_SOURCE)

        //if user visit the first time navigate to onboarding screen
        var  pref = PreferenceHelper.getInstance(this)
        var isFirstTimeUser = pref.get(PreferenceHelper.FIRST_TIME_USER , true)

        if(isFirstTimeUser)
            findNavController(R.id.onboarding_navhost).navigate(R.id.action_mainAccountFragment_to_mainOnboardingFragment)

        //SHOW LANGUAGE SELECTION DIALOG
        showLanguageSelelectDialog(true)

        // handle navigation from payment activity
//        var yenepayPaymentFor = intent.getIntExtra("PAYMENT_FOR" , -1)
//        when(yenepayPaymentFor){
//            PaymentManager.PAYMENT_FOR_SUBSCRIPTION ->{
//
//                var  preference = PreferenceHelper.getInstance(this)
//                var userId = preference.get("SUB_USER_ID" , "")
//                var subscriptionId = preference.get("SUB_SUBSCRIPTION_ID" , "")
//                var verifyPaymentInfo = VerifyPayment(userId , subscriptionId ,  VerifyPayment.PAYMENT_METHOD_YENEPAY)
//                activityviewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(this , Observer{ accountState ->
//                    when(accountState){
//                        is AccountState.ValidSubscription ->{
//                            activityviewmodel.user = accountState.user
//                            preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
//                            preference[AccountState.USED_AUDIOBOOK_CREDIT] = 0
//                            sendIntent(MainActivity::class.java)
////                            if(isHalfRegistered(this)){
////                                findNavController(R.id.onboarding_navhost).navigate(R.id.categorySelectionFragment)
////                            }
////                            else{
////                                sendIntent(MainActivity::class.java)
////                            }
//                        }
//                        is AccountState.NoSubscription ->{
//                            Toast.makeText(this , "${accountState.message}" , Toast.LENGTH_LONG).show()
//                        }
//                    }
//                })
//            }
//        }


//        if(ispayed){
//            setContentView(R.layout.activity_onboarding)
//
//            this.findNavController(R.id.onboarding_navhost).navigate(R.id.subscriptionFragment)
//        }
//        else{
//            setContentView(R.layout.activity_onboarding)
//        }

        var a = activityviewmodel.a
    }



//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(this , "result arrved " , Toast.LENGTH_LONG).show()
//        this.findNavController(R.id.onboarding_navhost).navigate(R.id.artistSelectionListFragment)
//
//    }

}
