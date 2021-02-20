package com.zomatunes.zomatunes.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.data.model.*

import com.zomatunes.zomatunes.databinding.HomeFragmentBinding
import com.zomatunes.zomatunes.ui.album.AlbumListViewModel
import com.zomatunes.zomatunes.ui.album.adapter.AlbumViewPagerAdapter
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.ui.song.SongListFragment
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.adaper.PlaylistAdapter
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error_page.*

@AndroidEntryPoint
class HomeFragment : Fragment()  {


    lateinit var binding : HomeFragmentBinding
    lateinit var adapter: AlbumViewPagerAdapter

    val homeViewmodel: HomeViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    val rStatemanager : RecyclerviewStateManager<Artist<String, String>> by lazy {
        RecyclerviewStateManager<Artist<String,String>>()
    }

    var showHistory = true



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        showHistory = PreferenceHelper.getInstance(requireContext()).getBoolean("history" , true)
        binding = HomeFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeViewmodel = homeViewmodel
        binding.showingHistory = showHistory

        var artistInteractionLIstener = object : IRecyclerViewInteractionListener<Artist<String, String>> {
            override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToArtist(data._id)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var playlistInteractionLIstener = object : IRecyclerViewInteractionListener<Playlist<String>> {
            override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
                if(data.type == "CHART") (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST , data._id!! , null , false)
                else (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST_SONGS , null , data , false)
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

        var artistInfo= RecyclerViewHelper("ARTIST" , rStatemanager , artistInteractionLIstener , viewLifecycleOwner)
        var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM ,
           layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL )

        var chartInfo = RecyclerViewHelper("CHART" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL )

        var radioLIstInfo = RecyclerViewHelper("RADIO" , interactionListener = radioINteractionList , owner = viewLifecycleOwner)
        binding.artistInfo = artistInfo
        binding.playlistInfo = playlistInfo
        binding.chartInfo = chartInfo
        binding.radioInfo = radioLIstInfo


        binding.albumArtistNameTextview.setOnClickListener {
            findNavController().navigate(R.id.authorFragment)
        }

        binding.moreChartsTextview.setOnClickListener {
            findNavController().navigate(R.id.chartListFragment)
        }


//        binding.moreNewAlbumTextview.setOnClickListener{
//            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_NEW_SONG , null)
//        }
//        binding.moreNewAlbumTextview.setOnClickListener {
//            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_POPULAR_SONG , null)
//        }

        binding.moreNewAlbumTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToAlbumListFragment("New Albums" , AlbumListViewModel.LOAD_NEW_ALBUM)
        }
//        binding.moreSecularAlbumTextview.setOnClickListener {
//            (requireActivity() as MainActivity).moveToAlbumListFragment("P Albums" , AlbumListViewModel.LOAD_POPULAR_SECULAR_ALBUM)
//        }

//        binding.artistPlaylist.setOnClickListener {
//            (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_ARTIST_COLLECTION , null , false)
//        }

        binding.moreRadioStationsTextview.setOnClickListener {
            findNavController().navigate(R.id.radioHomeFragment)
        }

//        homeViewmodel.errorHandler()
        homeViewmodel.error.observe(viewLifecycleOwner , Observer{ error ->
            binding.homeLoadingProgressbar.visibility = View.GONE
            error.handleError(requireContext()){
                binding.errorView.visibility = View.VISIBLE
                error_textview.text = "Something went wrong. Please try again or listen your downloads "
                goto_download_btn.setOnClickListener {
                    findNavController().navigate(R.id.downloadListFragment)
                }
                tryagain_btn.setOnClickListener {
                    findNavController().navigate(findNavController().graph.startDestination)
                }
            }
        })

    }


}
