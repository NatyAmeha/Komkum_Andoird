package com.zomatunes.zomatunes.ui.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.OnboardingActivityViewmodel

import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.databinding.FragmentMainAccountBinding
import com.zomatunes.zomatunes.ui.account.accountservice.UserManager
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
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
        // Inflate the layout for this fragment
        var registrationCheck =
            (this.toControllerActivity().isHalfRegistered(requireActivity().applicationContext) or
                    this.toControllerActivity().isFullyRegistered(requireActivity().applicationContext))

        if (!registrationCheck) {
            findNavController().navigate(R.id.action_mainAccountFragment_to_mainOnboardingFragment)
        }

        else if(userManager.isLoggedIn()){}

//        else if(!userManager.hasValidSubscription()){
//            findNavController().navigate(R.id.action_mainAccountFragment_to_subscriptionFragment)
//        }

        binding = FragmentMainAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fbcallbackManager =  CallbackManager.Factory.create()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
           requireActivity().finishAndRemoveTask()
        }

        binding.createAccountBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainAccountFragment_to_registrationFragment)
        }

        binding.loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainAccountFragment_to_loginFragment)
        }

        binding.fbLoginBtn.setOnClickListener {
            var permissions = listOf("email")
            LoginManager.getInstance().logIn(this , permissions)
        }

        LoginManager.getInstance().registerCallback(fbcallbackManager , object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                result?.let {
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
                            var jsongResponse = response.jsonObject
                            var name = jsongResponse.getString("name")
                            var email = if (jsongResponse.has("email")) jsongResponse.getString("email") else null
                            var id = jsongResponse.getString("id")
                            var imageUrl = jsongResponse.getJSONObject("picture").getJSONObject("data").getString("url")

                            var userResult = User(facebookId = id , username = name , email = email , profileImagePath = imageUrl , phoneNumber = "")
                            viewmodel.continueWithFacebook(userResult).observe (viewLifecycleOwner , Observer {accountState ->
                                when(accountState){
                                    is AccountState.LoggedIn -> {
                                        if(viewmodel.userManager.hasValidSubscription(accountState.user?.subscription)){
                                            this@MainAccountFragment.sendIntent(MainActivity::class.java)
                                        }
                                        else{
                                            viewmodel.user = accountState.user
                                            findNavController().navigate( R.id.subscriptionFragment)
                                        }
                                    }
                                    is AccountState.NoSubscription -> {
                                        viewmodel.user = accountState.user
                                        findNavController().navigate(R.id.subscriptionFragment)
                                    }
                                    is AccountState.LoggedOut-> {
                                        Toast.makeText(context ,"${accountState.message} may be vpn error" , Toast.LENGTH_LONG).show()
                                    }
                                }
                            })

                        }catch (ex : Exception){
                            Log.i("resultdataerror" , ex.message)
                        }
                    }
                    var  parameters =  Bundle()
                    parameters.putString("fields", "id,name,link,email , picture");
                    request.parameters = parameters;
                    request.executeAsync()
                }
            }

            override fun onCancel() {
                Toast.makeText(requireContext() , "cancel" , Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(requireContext() , error?.message , Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fbcallbackManager.onActivityResult(requestCode , resultCode ,data)
    }
}
