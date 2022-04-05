package com.komkum.komkum.ui.radio

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentRadioListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RadioListFragment : Fragment() , IRecyclerViewInteractionListener<Radio> {

    lateinit var binding : FragmentRadioListBinding
    val radioViewmodel : RadioViewModel by viewModels()

    var isUserStation = false
    var name : String? = null
    var category : String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

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
        configureActionBar(binding.toolbar , "$name stations")
        var radioLIstInfo = RecyclerViewHelper("RADIO" , interactionListener = this , owner = viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.radioInfo = radioLIstInfo
        binding.viewmodel = radioViewmodel

        radioViewmodel.radioStationList.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()) binding.radioListErrorTextview.isVisible = true
        }

        category?.let { radioViewmodel.getRadiosByCategory(it) }

        if(isUserStation) radioViewmodel.userRadioStations()

    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(data: Radio, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoSingleRadioStation(null , data)
    }
    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

}