package com.example.ethiomusic.ui.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.OnboardingActivityViewmodel

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.AuthenticationModel
import com.example.ethiomusic.data.model.User
import com.example.ethiomusic.databinding.FragmentMainAccountBinding
import com.example.ethiomusic.ui.account.accountservice.UserManager
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.facebook.*
import com.facebook.login.LoginResult
import org.json.JSONObject
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MainAccountFragment : Fragment() {

    @Inject
    lateinit var userManager : UserManager

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    lateinit var binding: FragmentMainAccountBinding

    lateinit  var fbcallbackManager : CallbackManager

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.authSubcomponent().create().inject(this)
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

        var fbLoginButton = binding.fbLoginBtn
        fbLoginButton.fragment = this

        var permissions = listOf("user_friends" , "email")
        fbLoginButton.setPermissions(permissions)
        fbLoginButton.registerCallback(fbcallbackManager , object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                result?.let {
                    var request = GraphRequest.newMeRequest(it.accessToken) { _, response ->
                        Log.i("resultdata" , response.toString())
                        var jsongResponse = response.jsonObject
                        var name = jsongResponse.getString("name")
                        var email = jsongResponse.getString("email")
                        var imageUrl = jsongResponse.getJSONObject("picture").getJSONObject("data").getString("url")

                        var userResult = User(username = name , email = email , profileImagePath = imageUrl , phoneNumber = "")
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
                                    findNavController().navigate(R.id.action_loginFragment_to_subscriptionFragment)
                                }
                                is AccountState.LoggedOut-> {
                                    Toast.makeText(context ,"${accountState.message} may be vpn error" , Toast.LENGTH_LONG).show()
                                }
                            }
                        })
                    }
                    var  parameters =  Bundle()
                    parameters.putString("fields", "id,name,link,email , picture , friends");
                    request.parameters = parameters;
                    request.executeAsync()
                }


            }

            override fun onCancel() {
                Toast.makeText(requireContext() , "cancel" , Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException?) {
                Log.i("error" , error?.message)
                Toast.makeText(requireContext() , "error" , Toast.LENGTH_LONG).show()
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fbcallbackManager.onActivityResult(requestCode , resultCode ,data)
    }
}
