package com.example.ethiomusic.ui.onboarding

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.*
import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.RecyclerViewHelper

import com.example.ethiomusic.databinding.ArtistSelectionListFragmentBinding
import com.example.ethiomusic.ui.album.adapter.ArtistAdapter
import com.example.ethiomusic.ui.artist.ArtistViewModel
import com.example.ethiomusic.ui.artist.ArtistViewmodelFactory
import com.example.ethiomusic.util.addArtistData
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import javax.inject.Inject

class ArtisSelectiontListFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>> {

    lateinit var binding : ArtistSelectionListFragmentBinding
    val viewmodel : OnboardingActivityViewmodel by activityViewModels()

    @Inject
    lateinit var artistViewmodelFactory : ArtistViewmodelFactory
    val artistViewmodel : ArtistViewModel by viewModels{GenericSavedStateViewmmodelFactory(artistViewmodelFactory , this)}

    lateinit var artistSearchResultAdapter : ArtistAdapter

    val rStatemanager : RecyclerviewStateManager<Artist<String,String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    var isFromFavorite = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
              isFromFavorite = it.getBoolean("FROM_FAVORITE" , false)
        }
        binding = ArtistSelectionListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var listInfo = RecyclerViewHelper("ARTISTSELECTION" , rStatemanager , this , viewLifecycleOwner)
        binding.listInfo = listInfo

        if(isFromFavorite){

            binding.onboardingFinishBtn.visibility = View.GONE
            binding.backToCategorySelectionBtn.visibility = View.GONE
            binding.artistLayoutGroup.visibility = View.GONE
        }
        else{
            binding.lifecycleOwner = viewLifecycleOwner
            binding.onboardingActivityViewmodel = viewmodel
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
              viewmodel.addArtistToFavorite(ids).observe(viewLifecycleOwner , Observer { resources ->
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
