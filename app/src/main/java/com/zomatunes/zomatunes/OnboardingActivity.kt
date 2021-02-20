package com.zomatunes.zomatunes

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
import com.zomatunes.zomatunes.data.model.VerifyPayment
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.ui.account.subscription.SubscriptionFragment
import com.zomatunes.zomatunes.util.IYenepayPaymentHandler
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import dagger.hilt.android.AndroidEntryPoint
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class OnboardingActivity : ControllerActivity() {


//    val viewmodel : OnboardingActivityViewmodel by viewModels()

    fun gettoken(){
        try {
            val info = packageManager.getPackageInfo(
                "com.zomatunes.zomatunes",
                PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.i("Keyhash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("Keyhashe:", e.message)
        } catch (e: NoSuchAlgorithmException) {
            Log.i("Keyhashee:", e.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
//         (application as ZomaTunesApplication).appComponent.authSubcomponent().create().inject(this)
        setTheme(R.style.AppthemeDark)
//        gettoken()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // handle navigation from payment activity
        var yenepayPaymentFor = intent.getIntExtra("PAYMENT_FOR" , -1)

        when(yenepayPaymentFor){
            PaymentManager.PAYMENT_FOR_SUBSCRIPTION ->{
                Toast.makeText(this , "Double request" , Toast.LENGTH_LONG).show()
                var  preference = PreferenceHelper.getInstance(this)
                var userId = preference.get("SUB_USER_ID" , "")
                var subscriptionId = preference.get("SUB_SUBSCRIPTION_ID" , "")
                var verifyPaymentInfo = VerifyPayment(userId , subscriptionId ,  VerifyPayment.SUBSCRIPTION_PAYMENT_TYPE)
                activityviewmodel.verifySubscriptionPayment(verifyPaymentInfo).observe(this , Observer{ accountState ->
                    when(accountState){
                        is AccountState.ValidSubscription ->{
                            activityviewmodel.user = accountState.user
                            preference[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                            preference[AccountState.USED_AUDIOBOOK_CREDIT] = 0
                            sendIntent(MainActivity::class.java)
//                            if(isHalfRegistered(this)){
//                                findNavController(R.id.onboarding_navhost).navigate(R.id.categorySelectionFragment)
//                            }
//                            else{
//                                sendIntent(MainActivity::class.java)
//                            }
                        }
                        is AccountState.NoSubscription ->{
                            Toast.makeText(this , "${accountState.message} subscription" , Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }


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
