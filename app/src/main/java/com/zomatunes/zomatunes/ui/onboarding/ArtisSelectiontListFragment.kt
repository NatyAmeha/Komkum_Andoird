package com.zomatunes.zomatunes.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.data.model.Artist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper

import com.zomatunes.zomatunes.databinding.ArtistSelectionListFragmentBinding
import com.zomatunes.zomatunes.ui.album.adapter.ArtistAdapter
import com.zomatunes.zomatunes.ui.artist.ArtistViewModel
import com.zomatunes.zomatunes.util.extensions.handleListDataObservation
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtisSelectiontListFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>> {

    lateinit var binding : ArtistSelectionListFragmentBinding
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()


    val artistViewmodel : ArtistViewModel by viewModels()

    lateinit var artistSearchResultAdapter : ArtistAdapter

    val rStatemanager : RecyclerviewStateManager<Artist<String,String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    var isFromFavorite = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
              isFromFavorite = it.getBoolean("FROM_FAVORITE" , false)
        }
        binding = ArtistSelectionListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var listInfo = RecyclerViewHelper("ARTISTSELECTION" , rStatemanager , this , viewLifecycleOwner)

        if(isFromFavorite){
            binding.onboardingFinishBtn.visibility = View.GONE
            binding.backToCategorySelectionBtn.visibility = View.GONE
        }
        else{
            binding.lifecycleOwner = viewLifecycleOwner
        }

        artistViewmodel.popularArtists.handleListDataObservation(viewLifecycleOwner){
            artistSearchResultAdapter = ArtistAdapter(listInfo)
            binding.popularArtistRecyclerview.layoutManager = GridLayoutManager(requireContext() ,3)
            binding.popularArtistRecyclerview.adapter = artistSearchResultAdapter
            binding.artistlistLoadingProgressBar.visibility = View.GONE
            artistSearchResultAdapter.submitList(it)
        }

        artistViewmodel.newArtist.handleListDataObservation(viewLifecycleOwner){
            artistSearchResultAdapter = ArtistAdapter(listInfo)
            binding.newartistRecyclerview.layoutManager = GridLayoutManager(requireContext() ,3)
            binding.newartistRecyclerview.adapter = artistSearchResultAdapter
            binding.artistlistLoadingProgressBar.visibility = View.GONE
            artistSearchResultAdapter.submitList(it)
        }

        binding.artistSearchEditTextView.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.artistLayoutGroup.visibility = View.GONE
                binding.artistSearchresultRecyclerview.visibility = View.VISIBLE
            }

        }

        binding.artistSearchEditTextView.doOnTextChanged { text, start, before, count ->
            artistViewmodel.getSearchResult(text.toString()).handleSingleDataObservation(viewLifecycleOwner){
                Log.i("artist" , it.artists?.get(0)?.name)
                artistSearchResultAdapter = ArtistAdapter(listInfo)
                binding.artistSearchresultRecyclerview.layoutManager = GridLayoutManager(requireContext() ,3)
                binding.artistSearchresultRecyclerview.adapter = artistSearchResultAdapter
                artistSearchResultAdapter.submitList(it.artists)
            }
        }


        binding.onboardingFinishBtn.setOnClickListener {
            var ids = rStatemanager.multiselectedItems.value!!.map { artist -> artist._id }.toSet().toList()
              viewmodel.updateUserAndFollowArtists(viewmodel.user!! , ids).observe(viewLifecycleOwner , Observer { resources ->
                  this.toControllerActivity().updateToFullyRegisteredAccount(view.context)
                 sendIntent(MainActivity::class.java)
              })
        }
    }

    override fun onItemClick(artist: Artist<String, String>, position: Int, option: Int?) {
        if(isFromFavorite){
            (requireActivity() as MainActivity).moveToArtist(artist._id)
        }else{
            rStatemanager.addOrRemoveItem(artist)
        }
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }
}
