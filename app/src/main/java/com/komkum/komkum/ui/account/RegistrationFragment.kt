package com.komkum.komkum.ui.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController

import com.komkum.komkum.OnboardingActivityViewmodel


import com.komkum.komkum.R
import com.komkum.komkum.data.model.AuthenticationModel
import com.komkum.komkum.data.model.User
import com.komkum.komkum.databinding.FragmentRegistrationBinding
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.notification.FcmService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()
    lateinit var binding: FragmentRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.create_an_account))
        binding.continueRegistrationBtn.setOnClickListener {
            register()
        }

        binding.textView20.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        viewmodel.getError().observe(viewLifecycleOwner){ Log.i("mainerror" , it)}
        viewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.registrationProgressbar.isVisible = false
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }


    fun register(){
        binding.countryCodePicker.registerCarrierNumberEditText(binding.phoneNumberEditText)
        var phone = binding.countryCodePicker.fullNumberWithPlus
        if(!binding.countryCodePicker.isValidFullNumber) {
            Toast.makeText(requireContext() , getString(R.string.phone_number_format_error) , Toast.LENGTH_LONG).show()
            return
        }

        binding.registrationProgressbar.isVisible = true

        val email = binding.registrationEmailEditText.text.toString().removeSurrounding(".")
        var username = binding.registrationUsernameEditText.text?.trim().toString()
        var password = binding.registrationPasswordEditText.text?.trim().toString()
        var confirmPassword = binding.registrationConfirmPasswordEditText.text?.trim().toString()
        if(email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
            if(password == confirmPassword){
                viewmodel.checkEmail(AuthenticationModel(email = email)).observe(viewLifecycleOwner , Observer { resource ->
                    binding.registrationProgressbar.visibility = View.VISIBLE
                    resource?.let {isEmailUnique ->
                        if(isEmailUnique) {
                            var user = User(username = username , password = password , email = email , phoneNumber = phone , category = listOf("GOSPEL" , "SECULAR"))
                            viewmodel.user = user
                            continueRegisteration(requireContext())
//                    findNavController().navigate(R.id.action_registrationFragment_to_categorySelectionFragment)
                        }
                        else {
                            binding.registrationEmailInputText.error = getString(R.string.email_not_unique)
                            Toast.makeText(requireActivity() , getString(R.string.email_not_unique) , Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
            else{
                binding.registrationProgressbar.isVisible = false
                Toast.makeText(requireContext() , getString(R.string.password_not_correct) , Toast.LENGTH_LONG).show()
            }

        }
        else{
            binding.registrationProgressbar.isVisible = false
            Toast.makeText(requireContext() , getString(R.string.fill_all_form_fields) , Toast.LENGTH_LONG).show()
        }
    }

    fun continueRegisteration(context : Context){
        var pref = PreferenceHelper.getInstance(requireContext())
        viewmodel.registrationRequest().observe(viewLifecycleOwner , Observer { result ->
            binding.registrationProgressbar.isVisible = false
            when(result){
                is AccountState.Registered -> {
                    viewmodel.user  = result.user
                    pref[PreferenceHelper.WALLET_BONUS_DIALOG_BEFORE_SIGN_UP] = false
                    pref[PreferenceHelper.WALLET_BONUS_DIALOG_AFTER_SIGN_UP] = true
                    pref[PreferenceHelper.FIRST_TIME_FOR_RECOMMENDATION_DIALOG] = true
                    pref[PreferenceHelper.FIRST_TIME_FOR_GAME_INSTRUCTION] = true
                    findNavController().navigate(R.id.loginFragment)
                }
                is AccountState.UnRegistered -> {
                    result.message?.let { Log.i("error" , it) }
                    Toast.makeText(context , result.message , Toast.LENGTH_LONG).show()
                }
            }
        })

    }
}
