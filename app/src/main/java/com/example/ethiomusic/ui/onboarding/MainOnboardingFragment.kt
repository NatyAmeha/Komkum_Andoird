package com.example.ethiomusic.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.example.ethiomusic.R
import com.example.ethiomusic.util.extensions.toControllerActivity
import kotlinx.android.synthetic.main.fragment_main_onboarding.*

class MainOnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onboarding_next_btn.setOnClickListener {
           this.toControllerActivity().updateToHalfRegisteredAccount(view.context)
            findNavController().navigate(R.id.action_mainOnboardingFragment_to_mainAccountFragment)
        }
    }
}


