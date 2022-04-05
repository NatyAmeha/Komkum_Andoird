package com.komkum.komkum.ui.song

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.komkum.komkum.*
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.FragmentAddSongBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.adaper.AddSongAdapter
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import com.mancj.materialsearchbar.MaterialSearchBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddSongFragment : Fragment() ,  MaterialSearchBar.OnSearchActionListener , IRecyclerViewInteractionListener<Song<String , String>> {


    lateinit var binding: FragmentAddSongBinding
    lateinit var addSongAdapter : AddSongAdapter
    lateinit var searchResultAdapter : SongAdapter

    val songViewmodel : SongViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var playlistId : String? = null
    var selectedSongs = mutableListOf<Song<String,String>>()
    var playlistRecommendation  =  MutableLiveData<List<Playlist<Song<String , String>>>>()

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                playlistId = it.getString("PLAYLIST_ID")
            }
            var result= songViewmodel.getPlaylistREcommendationsToAddInPlaylist().data
            if(!result.isNullOrEmpty()){
                playlistRecommendation.value = result!!
            }
        }
    }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

       binding = FragmentAddSongBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).hideBottomView()

//            binding.songViewpager.offscreenPageLimit = 3
        playlistRecommendation.observe(viewLifecycleOwner){
            binding.progressBar6.isVisible = false
            val recyclerInfo = RecyclerViewHelper<Playlist<String>>(owner = viewLifecycleOwner)

//                var finalPlaylist = it.filterNot { playlist -> playlist.songs.isNullOrEmpty() }
            addSongAdapter = AddSongAdapter(it)
            binding.songViewpager.adapter = addSongAdapter
            TabLayoutMediator(binding.tabLayout , binding.songViewpager){ tab, position -> }.attach()

            binding.songViewpager.apply {
                clipToPadding = false
                offscreenPageLimit = 1
                val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
                val currentItemHorizontalMarginPx = 16
                val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
                val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                    page.translationX = -pageTranslationX * position
                }
                setPageTransformer(pageTransformer)
            }
        }



        var info = RecyclerViewHelper(type = "SONGSEARH" , interactionListener = this)
        searchResultAdapter = SongAdapter(info)
        binding.songResultRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.songResultRecyclerview.adapter = searchResultAdapter

        binding.searchSongTextInput.setOnSearchActionListener(this)
    }

    override fun onPause() {
        selectedSongs.addAll(addSongAdapter.selectedSongs)
        if(::addSongAdapter.isInitialized && selectedSongs.isNotEmpty()){
            mainActivityViewmodel.newAddedSongToPlaylist.value = selectedSongs
            Toast.makeText(requireContext() , "Adding ${selectedSongs.size} songs to playlist", Toast.LENGTH_SHORT).show()
            playlistId?.let {
                var selectedSongsId = selectedSongs.map { song -> song._id }.toSet()
                mainActivityViewmodel.addToPlaylist(it , selectedSongsId.toList()).observe(viewLifecycleOwner){

                }
            }
        }
        super.onPause()
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        selectedSongs.add(data)
        var newList = searchResultAdapter.currentList.toMutableList()
        newList.remove(data)
        searchResultAdapter.submitList(newList)
        (requireActivity() as MainActivity).showSnacbar("Song added to Playlist")
    }

    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}
    override fun onSearchStateChanged(enabled: Boolean) {
        if(enabled){
            binding.songViewpager.visibility = View.INVISIBLE
            binding.songResultRecyclerview.visibility = View.VISIBLE
        }
    }

    override fun onSearchConfirmed(text: CharSequence?) {
        binding.progressBar6.isVisible = true
        songViewmodel.searchSong(text.toString()).observe(viewLifecycleOwner , Observer { songs ->
            binding.progressBar6.isVisible = false
            songs?.let {searchResultAdapter.submitList(it)}
        })
    }

    override fun onButtonClicked(buttonCode: Int) {
        TODO("Not yet implemented")
    }


}