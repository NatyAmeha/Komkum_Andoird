package com.zomatunes.zomatunes.ui.song

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.FragmentAddSongBinding
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.adaper.AddSongAdapter
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddSongFragment : Fragment() , IRecyclerViewInteractionListener<Song<String , String>> {


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
            playlistRecommendation.value = songViewmodel.getPlaylistREcommendationsToAddInPlaylist().data
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

       binding = FragmentAddSongBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.toControllerActivity().hideBottomView()



//            binding.songViewpager.offscreenPageLimit = 3
            playlistRecommendation.observe(viewLifecycleOwner){
                val recyclerInfo = RecyclerViewHelper<Playlist<String>>(owner = viewLifecycleOwner)

//                var finalPlaylist = it.filterNot { playlist -> playlist.songs.isNullOrEmpty() }
                addSongAdapter = AddSongAdapter(it)
                binding.songViewpager.adapter = addSongAdapter
                TabLayoutMediator(binding.tabLayout , binding.songViewpager){ tab, position -> }.attach()
            }



        var info = RecyclerViewHelper(type = "SONGSEARH" , interactionListener = this)
        searchResultAdapter = SongAdapter(info)
        binding.songResultRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.songResultRecyclerview.adapter = searchResultAdapter

        binding.searchSongEditText.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                binding.songViewpager.visibility = View.INVISIBLE
                binding.songResultRecyclerview.visibility = View.VISIBLE
            }
        }

        binding.searchSongEditText.doOnTextChanged { text, start, before, count ->
            songViewmodel.searchSong(text.toString()).observe(viewLifecycleOwner , Observer { songs ->
                songs?.let {searchResultAdapter.submitList(it)}
            })
        }

    }

    override fun onPause() {
        selectedSongs.addAll(addSongAdapter.selectedSongs)
        if(::addSongAdapter.isInitialized && selectedSongs.isNotEmpty()){
            Toast.makeText(requireContext() , selectedSongs.size.toString() + " Refresh the page", Toast.LENGTH_SHORT).show()
            playlistId?.let {
                var selectedSongsId = selectedSongs.map { song -> song._id }.toSet()
                mainActivityViewmodel.addToPlaylist(it , selectedSongsId.toList()).observe(viewLifecycleOwner , Observer{
                })}
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


}