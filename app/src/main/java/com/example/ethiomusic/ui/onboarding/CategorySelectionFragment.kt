package com.example.ethiomusic.ui.onboarding

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
import com.example.ethiomusic.*
import com.example.ethiomusic.data.model.MusicBrowse
import com.example.ethiomusic.data.model.RecyclerViewHelper

import com.example.ethiomusic.databinding.FragmentCategorySelectionBinding
import com.example.ethiomusic.ui.account.AccountState
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.adaper.BrowseCategoryAdapter
import com.example.ethiomusic.util.extensions.handleListDataObservation
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager

/**
 * A simple [Fragment] subclass.
 */
class CategorySelectionFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {


    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    val stateManager : RecyclerviewStateManager<MusicBrowse> by lazy {
        RecyclerviewStateManager<MusicBrowse>()
    }



    lateinit var binding : FragmentCategorySelectionBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)


    }

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

        viewmodel.songRepo.getMusicBrowsingString("GENRE").handleListDataObservation(viewLifecycleOwner){
            var categoryMap = it.groupBy { musicbrowse -> musicbrowse.category }
            var browseAdapter = BrowseCategoryAdapter(info , categoryMap)
            binding.categorySelectionRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.categorySelectionRecyclerview.adapter = browseAdapter
        }

        stateManager.multiselectedItems.observe(viewLifecycleOwner , Observer{ data ->
             binding.categorySelectionContinuBtn.isEnabled = data.isNotEmpty()
            viewmodel.user?.category = arrayOf("GOSPEL" , "SECULAR" , "ORTODOX" , "ISLAMIC")
            viewmodel.user?.tags = data.map { musicBrowse -> musicBrowse.name!! }.toTypedArray()
        })

        binding.categorySelectionContinuBtn.setOnClickListener {
            continueRegisteration(view.context)
        }
    }

    fun continueRegisteration(context : Context){
        viewmodel.registrationRequest().observe(viewLifecycleOwner , Observer { result ->
            when(result){
                is AccountState.HalfRegistered -> {
                    viewmodel.user  = result.user
                    findNavController().navigate(R.id.action_categorySelectionFragment_to_subscriptionFragment)
                }
                is AccountState.UnRegistered -> {
                    Log.i("error" , result.message)
                    Toast.makeText(context , result.message , Toast.LENGTH_LONG).show()}
            }
        })

    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        stateManager.addOrRemoveItem(data)
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}
