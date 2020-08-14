package com.example.ethiomusic.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.*
import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song

import com.example.ethiomusic.databinding.HomeFragmentBinding
import com.example.ethiomusic.ui.album.AlbumListViewModel
import com.example.ethiomusic.ui.album.adapter.AlbumViewPagerAdapter
import com.example.ethiomusic.ui.playlist.PlaylistFragment
import com.example.ethiomusic.ui.song.SongListFragment
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.adaper.PlaylistAdapter
import com.example.ethiomusic.util.extensions.handleError
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import kotlinx.android.synthetic.main.error_page.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class HomeFragment : Fragment()  {


    lateinit var binding : HomeFragmentBinding
    lateinit var adapter: AlbumViewPagerAdapter

    @Inject lateinit var homeFactory : HomeViewmodelFactory
    val homeViewmodel: HomeViewModel by viewModels{GenericSavedStateViewmmodelFactory(homeFactory , this)}
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    val rStatemanager : RecyclerviewStateManager<Artist<String, String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    var showHistory = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        showHistory = PreferenceHelper.getInstance(requireContext()).getBoolean("history" , false)
        binding = HomeFragmentBinding.inflate(inflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeViewmodel = homeViewmodel
        binding.showingHistory = showHistory

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

        var artistInfo= RecyclerViewHelper("ARTIST" , rStatemanager , artistInteractionLIstener , viewLifecycleOwner)
        var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM )
        binding.artistInfo = artistInfo
        binding.playlistInfo = playlistInfo



        binding.showNewsongTextview.setOnClickListener{
            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_NEW_SONG , null)
        }
        binding.morePopularSongTextview.setOnClickListener {
            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_POPULAR_SONG , null)
        }

        binding.moreGospelAlbumTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToAlbumListFragment("Gospel Albums" , AlbumListViewModel.LOAD_POPULR_GOSPEL_ALBUM)
        }
        binding.moreSecularAlbumTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToAlbumListFragment("Secular Albums" , AlbumListViewModel.LOAD_POPULAR_SECULAR_ALBUM)
        }

//        binding.artistPlaylist.setOnClickListener {
//            (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_ARTIST_COLLECTION , null , false)
//        }

        binding.moreRadioStationsTextview.setOnClickListener {
            findNavController().navigate(R.id.radioHomeFragment)
        }

        homeViewmodel.error.observe(viewLifecycleOwner , Observer { error ->
            binding.homeLoadingProgressbar.visibility = View.GONE
            error.handleError(requireContext()){
                binding.errorView.visibility = View.VISIBLE
                error_textview.text = "Something went wrong. Please try again or listen your downloads "
                goto_download_btn.setOnClickListener {
                    findNavController().navigate(R.id.downloadListFragment)
                }
                tryagain_btn.setOnClickListener {
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        })


    }


}
