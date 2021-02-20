package com.zomatunes.zomatunes.ui.radio

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Radio
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.FragmentRadioListBinding
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RadioListFragment : Fragment() , IRecyclerViewInteractionListener<Radio> {

    lateinit var binding : FragmentRadioListBinding
    val radioViewmodel : RadioViewModel by viewModels()

    var isUserStation = false
    var name : String? = null
    var category : String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            name  = it.getString("NAME")
            isUserStation = it.getBoolean("USER_STATION")
            category = it.getString("CATEGORY")
        }
        binding = FragmentRadioListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , name)
        var radioLIstInfo = RecyclerViewHelper("RADIO" , interactionListener = this , owner = viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.radioInfo = radioLIstInfo
        binding.viewmodel = radioViewmodel

        category?.let { radioViewmodel.getRadiosByCategory(it) }

        if(isUserStation) radioViewmodel.userRadioStations()

    }

    override fun onItemClick(data: Radio, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoSingleRadioStation(null , data)
    }
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}