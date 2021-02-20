package com.zomatunes.zomatunes.ui.onboarding

import android.content.Context
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.data.model.MusicBrowse
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper

import com.zomatunes.zomatunes.databinding.FragmentCategorySelectionBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.adaper.BrowseCategoryAdapter
import com.zomatunes.zomatunes.util.extensions.handleListDataObservation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class CategorySelectionFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {

    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    val stateManager : RecyclerviewStateManager<MusicBrowse> by lazy {
        RecyclerviewStateManager<MusicBrowse>()
    }



    lateinit var binding : FragmentCategorySelectionBinding



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
         binding = FragmentCategorySelectionBinding.inflate(inflater)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.owner = viewLifecycleOwner
        binding.onboardingActivityViewmodel = viewmodel
        binding.interactionManager = this

        stateManager.changeState(RecyclerviewState.MultiSelectionState())
        var info = RecyclerViewHelper("" , stateManager , this , viewLifecycleOwner)

//        viewmodel.songRepo.getMusicBrowsingString("GENRE").handleListDataObservation(viewLifecycleOwner){
//            var categoryMap = it.groupBy { musicbrowse -> musicbrowse.category }
//            var browseAdapter = BrowseCategoryAdapter(info , categoryMap)
//            binding.categorySelectionRecyclerview.layoutManager = LinearLayoutManager(requireContext())
//            binding.categorySelectionRecyclerview.adapter = browseAdapter
//        }

        stateManager.multiselectedItems.observe(viewLifecycleOwner , Observer{ data ->
             binding.categorySelectionContinuBtn.isEnabled = data.isNotEmpty()
            viewmodel.user?.category = listOf("GOSPEL" , "SECULAR" , "ORTODOX" , "ISLAMIC")
            viewmodel.user?.tags = data.map { musicBrowse -> musicBrowse.name!! }
        })

        binding.categorySelectionContinuBtn.setOnClickListener {
           findNavController().navigate(R.id.artistSelectionListFragment)
        }
    }



    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        stateManager.addOrRemoveItem(data)
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}
