package com.example.ethiomusic.ui.browse

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.databinding.BrowseMusicFragmentBinding
import com.example.ethiomusic.ui.playlist.PlaylistFragment
import com.example.ethiomusic.util.adaper.PlaylistAdapter
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.squareup.picasso.Picasso
import javax.inject.Inject

class BrowseMusicFragment : Fragment() {

    companion object{
        const val GENRE = "GENRE"
        const val MOODS = "MOODS"
    }

    @Inject lateinit var viewmodelFactory: BrowseMusicViewmodelFactory
    val viewModel: BrowseMusicViewModel by viewModels { GenericSavedStateViewmmodelFactory(viewmodelFactory , this) }

    lateinit var binding : BrowseMusicFragmentBinding

    lateinit var browse : MusicBrowse

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            browse = it.getParcelable("BROWSE")
        }
        binding = BrowseMusicFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar)
        binding.browsingCollapseToolabar.title = browse.name
        Picasso.get().load(browse.imagePath?.replace("localhost" , "192.168.43.166")).placeholder(R.drawable.backimage)
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

        var artistInfo= RecyclerViewHelper("ARTIST" , null , artistInteractionLIstener , viewLifecycleOwner)
        var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel
        binding.artistInfo = artistInfo
        binding.playlistInfo = playlistInfo

        viewModel.getBrowseResult(browse)

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

}