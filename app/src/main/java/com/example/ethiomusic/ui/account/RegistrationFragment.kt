package com.example.ethiomusic.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.example.ethiomusic.OnboardingActivityViewmodel


import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.AuthenticationModel
import com.example.ethiomusic.data.model.User
import com.example.ethiomusic.databinding.FragmentRegistrationBinding

/**
 * A simple [Fragment] subclass.
 */
class RegistrationFragment : Fragment() {

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    lateinit var binding: FragmentRegistrationBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegistrationBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.continueRegistrationBtn.setOnClickListener {
            register()
        }
    }


    fun register(){
        val email = binding.registrationEmailEditText.text.toString().removeSurrounding(".")
        var username = binding.registrationUsernameEditText.text.toString()
        var password = binding.registrationPasswordEditText.text.toString()
        viewmodel.checkEmail(AuthenticationModel(email = email)).observe(viewLifecycleOwner , Observer { resource ->
            binding.registrationProgressbar.visibility = View.VISIBLE
            resource.data?.let {isEmailUnique ->
                binding.registrationProgressbar.visibility = View.GONE
                if(isEmailUnique) {
                    var user = User(username = username , password = password , email = email)
                    viewmodel.user = user
                    findNavController().navigate(R.id.action_registrationFragment_to_categorySelectionFragment)
                }
                else {
                    binding.registrationEmailInputText.error = "email is not unique"
                    Toast.makeText(requireActivity() , "Email Address is not uniuqe" , Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}
