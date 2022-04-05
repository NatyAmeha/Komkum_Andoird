package com.komkum.komkum.ui.browse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.GenericSavedStateViewmmodelFactory
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.BrowseMusicFragmentBinding
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.PlaylistAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class BrowseMusicFragment : Fragment() {

    companion object{
        const val GENRE = "GENRE"
        const val MOODS = "MOODS"
    }

    @Inject lateinit var viewmodelFactory: BrowseMusicViewmodelFactory
    val viewModel: BrowseMusicViewModel by viewModels { GenericSavedStateViewmmodelFactory(viewmodelFactory , this) }

    lateinit var binding : BrowseMusicFragmentBinding

    var browse : MusicBrowse? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                browse = it.getParcelable("BROWSE")
            }
            browse?.let { viewModel.getBrowseResult(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = BrowseMusicFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        configureActionBar(binding.toolbar)
        binding.browsingCollapseToolabar.title = browse?.name
        Picasso.get().load(browse?.imagePath).placeholder(R.drawable.music_placeholder)
            .fit().centerCrop().into(binding.appBarImage)

        var artistInteractionLIstener = object :
            IRecyclerViewInteractionListener<Artist<String, String>> {
            override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToArtist(data._id)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var playlistInteractionLIstener = object : IRecyclerViewInteractionListener<Playlist<String>> {
            override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST_SONGS , null , data , false)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var radioINteractionList = object : IRecyclerViewInteractionListener<Radio>{
            override fun onItemClick(data: Radio, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoSingleRadioStation(null , data)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var artistInfo= RecyclerViewHelper("ARTIST" , null , artistInteractionLIstener , viewLifecycleOwner)
        var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM, layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        var radioLIstInfo = RecyclerViewHelper("RADIO" , interactionListener = radioINteractionList , owner = viewLifecycleOwner)

        viewModel.browseResult.observe(viewLifecycleOwner){
            if (it == null) {
                binding.progressBar.isVisible = false
                binding.musicBrowseErrorTextview.isVisible = true
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        binding.artistInfo = artistInfo
        binding.playlistInfo = playlistInfo
        binding.radioInfo = radioLIstInfo



//        when(browse.category){
//            GENRE -> {
//                binding.songsPlaylistHeaderTextview.text = "Songs recap"
//                viewModel.getNewSongsPlaylist(browse.name)
//                viewModel.getNewAlbums(browse.name)
//                viewModel.getPopularAlbums(browse.name)
//                viewModel.getTopArtist()
//                viewModel.getRadio(browse.name)
//            }
//            MOODS ->{}
//        }
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

}