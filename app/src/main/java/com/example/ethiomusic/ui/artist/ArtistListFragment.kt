package com.example.ethiomusic.ui.artist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.R

import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.FragmentArtistListBinding
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class ArtistListFragment : Fragment() , IRecyclerViewInteractionListener<Artist<String,String>>{

    @Inject
    lateinit var artistViewmodelFactory : ArtistViewmodelFactory
    val artistviewmodel : ArtistViewModel by viewModels { GenericSavedStateViewmmodelFactory(artistViewmodelFactory , this) }

    lateinit var binding : FragmentArtistListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.artistListToolbar , "Favorite Artists")
        var info = RecyclerViewHelper(type = "Artist" , interactionListener = this)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = artistviewmodel
        binding.listInfo = info


        binding.findMoreArtistBtn.setOnClickListener {
            var bundle = bundleOf("FROM_FAVORITE" to true)
            findNavController().navigate(R.id.action_artistListFragment_to_artisSelectiontListFragment2 , bundle)
        }

    }

    override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {

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
