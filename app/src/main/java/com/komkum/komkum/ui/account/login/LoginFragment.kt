package com.komkum.komkum.ui.account.login

import android.content.Context
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
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.OnboardingActivityViewmodel

import com.komkum.komkum.R
import com.komkum.komkum.data.model.AuthenticationModel
import com.komkum.komkum.databinding.FragmentLoginBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.sendIntent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    lateinit var binding : FragmentLoginBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.signinBtn.setOnClickListener { login(requireContext()) }
        binding.forgotPasswordTextview.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment) }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().navigate(R.id.mainAccountFragment)
        }
    }



    fun login(context : Context){
        var email = binding.usernameEditTextview.text.toString()
        var password = binding.passwordEditTextview.text.toString()
        var authData = AuthenticationModel(email , password)

        binding.loginProgressbar.visibility = View.VISIBLE
        viewmodel.authRequest(authData).observe(viewLifecycleOwner , Observer{ authState ->
            when(authState ){
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
                       }
                       else{
                           pref[PreferenceHelper.RELOAD_STORE_PAGE] = false
                           this.sendIntent(MainActivity::class.java)
                       }
                   }


//                   if(viewmodel.userManager.hasValidSubscription(authState.user?.subscription)){
//                       this.sendIntent(MainActivity::class.java)
//                   }
//                   else{
//                       viewmodel.user = authState.user
//                       findNavController().navigate(R.id.action_loginFragment_to_subscriptionFragment)
//                   }
               }
                is AccountState.NoSubscription -> {
                    this.sendIntent(MainActivity::class.java)
//                    viewmodel.user = authState.user
//                    findNavController().navigate(R.id.action_loginFragment_to_subscriptionFragment)
                }
                is AccountState.LoggedOut-> {
                    binding.loginProgressbar.visibility = View.GONE
                    authState.message?.let { Log.i("errorlogin" , it) }
                }
            }
        })

    }
}
