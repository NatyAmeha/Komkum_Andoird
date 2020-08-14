package com.example.ethiomusic.ui.radio

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.R
import com.example.ethiomusic.databinding.FragmentRadioHomeBinding
import com.example.ethiomusic.util.adaper.BrowseAdapter
import com.example.ethiomusic.util.extensions.configureActionBar
import javax.inject.Inject


class RadioHomeFragment : Fragment() {

    @Inject lateinit var radioViewmodelFactory : RadioViewmodelFactory
    val radioViewmodel : RadioViewModel by viewModels { GenericSavedStateViewmmodelFactory(radioViewmodelFactory , this)  }

    lateinit var binding : FragmentRadioHomeBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var appComponent = (requireActivity().application as EthioMusicApplication).appComponent
        appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRadioHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.radioToolbar , "Radio Stations")
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = radioViewmodel

//        radioViewmodel.browseString.observe(viewLifecycleOwner , Observer { resource ->
//            resource.data?.let {
//                binding.radioLoadingProgressBar.visibility  = View.GONE
//                binding.browseContainer.layoutManager = GridLayoutManager(requireContext() , 2)
//                var adapter = BrowseAdapter(it)
//                binding.browseContainer.adapter = adapter
//            }
//        })
        var category = arrayOf("GOSPEL" , "SECCULAR" , "ORTODOX" , "ISLAMIC")
        var arrayAdapter = ArrayAdapter<String>(requireContext() , android.R.layout.simple_list_item_1 , category)
        binding.categorySpinner.adapter = arrayAdapter


    }
}