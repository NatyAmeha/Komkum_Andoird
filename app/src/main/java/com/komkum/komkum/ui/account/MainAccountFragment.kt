package com.komkum.komkum.ui.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.OnboardingActivityViewmodel

import com.komkum.komkum.R
import com.komkum.komkum.data.model.User
import com.komkum.komkum.databinding.FragmentMainAccountBinding
import com.komkum.komkum.ui.account.accountservice.UserManager
import com.komkum.komkum.util.extensions.sendIntent
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainAccountFragment : Fragment() {

    @Inject
    lateinit var userManager : UserManager

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    lateinit var binding: FragmentMainAccountBinding

    lateinit  var fbcallbackManager : CallbackManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        var application = requireActivity().application as ZomaTunesApplication
//        application.appComponent.authSubcomponent().create().inject(this)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        var registrationCheck = (this.toControllerActivity().isHalfRegistered(requireActivity().applicationContext) or
//                    this.toControllerActivity().isFullyRegistered(requireActivity().applicationContext))
//
//        if (!registrationCheck) {
//            findNavController().navigate(R.id.action_mainAccountFragment_to_mainOnboardingFragment)
//        }
//
//
//        else if(userManager.isLoggedIn()){}
//
////        else if(!userManager.hasValidSubscription()){
////            findNavController().navigate(R.id.action_mainAccountFragment_to_subscriptionFragment)
////        }

        binding = FragmentMainAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fbcallbackManager =  CallbackManager.Factory.create()

        binding.createAccountBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_mainAccountFragment_to_registrationFragment)
            findNavController().navigate(R.id.phoneRegistrationFragment)
        }

        binding.loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainAccountFragment_to_loginFragment)
        }

        binding.fbLoginBtn.setOnClickListener {
            binding.fbLoginProgressbar.isVisible = true
            var permissions = listOf("email")
            LoginManager.getInstance().logIn(this , permissions)
        }

        LoginManager.getInstance().registerCallback(fbcallbackManager , object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                result.let {
            //                    var r = GraphRequest.newMyFriendsRequest(it.accessToken){ _, response ->
            //                        var jsonResponse  = response.jsonObject.getJSONArray("data")
            //                        for (i in 0 until jsonResponse.length()){
            //                            var user = jsonResponse.getJSONObject(i)
            //                            var id = user.getString("id")
            //                            var name  = user.getString("name")
            //                            var email = user.getString("email")
            //                            var imageUrl = user.getJSONObject("picture").getJSONObject("data").getString("url")
            //                            var userResult = User(facebookId = id ,  username = name  , profileImagePath = imageUrl , phoneNumber = "")
            //                        }
            //                        Log.i("resultdata" , jsonResponse.toString())
            //                    }
            //                    var  param =  Bundle()
            //                    param.putString("fields", "id,name, picture");
            //                    r.parameters = param
            //                    r.executeAsync()

                    var request = GraphRequest.newMeRequest(it.accessToken) { _, response ->
                        try{
                            var jsongResponse = response?.jsonObject
                            var name = jsongResponse?.getString("name")
                            var email = if (jsongResponse?.has("email") == true) jsongResponse.getString("email") else null
                            var id = jsongResponse?.getString("id")
                            var imageUrl = jsongResponse?.getJSONObject("picture")?.getJSONObject("data")?.getString("url")

                            var userResult = User(facebookId = id , username = name , email = email , profileImagePath = imageUrl , category = listOf("GOSPEL" , "SECULAR"))
                            viewmodel.continueWithFacebook(userResult , viewmodel.signupSource).observe (viewLifecycleOwner , Observer {accountState ->
                                binding.fbLoginProgressbar.isVisible = false
                                when(accountState){
                                    is AccountState.LoggedIn -> {
                                        var pref = PreferenceHelper.getInstance(requireContext())
                                        pref[PreferenceHelper.WALLET_BONUS_DIALOG_BEFORE_SIGN_UP] = false

                                        viewmodel.isSubscriptionValid().observe(viewLifecycleOwner){
                                            if(it == true)
                                                pref[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                                            else pref[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE

                                            var isNavRequireLogin = pref.get(AccountState.IS_REDIRECTION , false)
                                            if(isNavRequireLogin){
                                                pref[AccountState.IS_REDIRECTION] = false
                                                pref[PreferenceHelper.RELOAD_STORE_PAGE] = true
                                                requireActivity().finish()
                                            } else{
                                                pref[PreferenceHelper.RELOAD_STORE_PAGE] = false
                                                this@MainAccountFragment.sendIntent(MainActivity::class.java)
                                            }
                                        }
                                    }
                                    is AccountState.LoggedOut-> {
                                        Toast.makeText(context ,"${accountState.message}" , Toast.LENGTH_LONG).show()
                                    }
                                }
                            })

                        }catch (ex : Exception){
                            binding.fbLoginProgressbar.isVisible = false
                            ex.message?.let { it1 -> Log.i("resultdataerror" , it1) }
                        }
                    }
                    var  parameters =  Bundle()
                    parameters.putString("fields", "id,name,link,email , picture");
                    request.parameters = parameters;
                    request.executeAsync()
                }
            }

            override fun onCancel() {
                binding.fbLoginProgressbar.isVisible = false
                Toast.makeText(requireContext() , "cancel" , Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                binding.fbLoginProgressbar.isVisible = false
                Toast.makeText(requireContext() , error?.message , Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Toast.makeText(requireContext() , "onactivity called" , Toast.LENGTH_LONG).show()
        fbcallbackManager.onActivityResult(requestCode , resultCode ,data)
    }
}
