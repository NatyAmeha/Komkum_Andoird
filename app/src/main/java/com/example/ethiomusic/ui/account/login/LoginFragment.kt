package com.example.ethiomusic.ui.account.login

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.OnboardingActivityViewmodel

import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.AuthenticationModel
import com.example.ethiomusic.databinding.FragmentLoginBinding
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
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
    }



    fun login(context : Context){
        var email = binding.usernameEditTextview.text.toString()
        var password = binding.passwordEditTextview.text.toString()
        var authData = AuthenticationModel(email , password)

        binding.loginProgressbar.visibility = View.VISIBLE
        viewmodel.authRequest(authData).observe(viewLifecycleOwner , Observer{ authState ->
            when(authState ){
               is AccountState.LoggedIn -> {
                   if(viewmodel.userManager.hasValidSubscription(authState.user?.subscription)){
                       this.sendIntent(MainActivity::class.java)
                   }
                   else{
                       viewmodel.user = authState.user
                       findNavController().navigate(R.id.action_loginFragment_to_subscriptionFragment)
                   }
               }
                is AccountState.NoSubscription -> {
                    viewmodel.user = authState.user
                    findNavController().navigate(R.id.action_loginFragment_to_subscriptionFragment)
                }
                is AccountState.LoggedOut-> {
                    binding.loginProgressbar.visibility = View.GONE
                    Log.i("errorlogin" ,authState.message)
                    Toast.makeText(context ,"${authState.message} Your Email or Password is not correct" , Toast.LENGTH_LONG).show()
                }
            }
        })

    }
}
