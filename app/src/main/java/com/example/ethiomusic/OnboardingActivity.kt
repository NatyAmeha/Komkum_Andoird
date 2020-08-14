package com.example.ethiomusic

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.ethiomusic.ui.account.subscription.SubscriptionFragment
import com.example.ethiomusic.util.IYenepayPaymentHandler
import com.example.ethiomusic.util.PreferenceHelper
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class OnboardingActivity : ControllerActivity() {


    @Inject
    lateinit var viewmodelFactory: OnboardingActivityViewmodelFactory
    val activityviewmodel : OnboardingActivityViewmodel by viewModels{GenericSavedStateViewmmodelFactory(viewmodelFactory , this)}

    override fun onCreate(savedInstanceState: Bundle?) {
         (application as EthioMusicApplication).appComponent.authSubcomponent().create().inject(this)
        setTheme(R.style.AppthemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        var  preference = PreferenceHelper.getInstance(this)
        var ispayed = preference.getBoolean("PAY" , false)
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
