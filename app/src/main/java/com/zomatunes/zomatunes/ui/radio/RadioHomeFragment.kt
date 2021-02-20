package com.zomatunes.zomatunes.ui.radio

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.zomatunes.zomatunes.databinding.FragmentRadioHomeBinding
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RadioHomeFragment : Fragment() {

    val radioViewmodel : RadioViewModel by viewModels()

    lateinit var binding : FragmentRadioHomeBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
//        var appComponent = (requireActivity().application as ZomaTunesApplication).appComponent
//        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRadioHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.radioToolbar , "Radio Stations")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = radioViewmodel

    }
}