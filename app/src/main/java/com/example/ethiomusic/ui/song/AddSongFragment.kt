package com.example.ethiomusic.ui.song

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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ethiomusic.*
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.FragmentAddSongBinding
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.adaper.AddSongAdapter
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddSongFragment : Fragment() , IRecyclerViewInteractionListener<Song<String , String>> {

    @Inject
    lateinit var viewmodelFactory : SongViewmodelFactory
    lateinit var binding: FragmentAddSongBinding

    lateinit var addSongAdapter : AddSongAdapter
    lateinit var searchResultAdapter : SongAdapter

    val songViewmodel : SongViewModel by viewModels { GenericSavedStateViewmmodelFactory(viewmodelFactory , this) }
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var playlistId : String? = null
    var selectedSongs = mutableListOf<Song<String,String>>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            playlistId = it.getString("PLAYLIST_ID")
        }
       binding = FragmentAddSongBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.toControllerActivity().hideBottomView()
        CoroutineScope(Dispatchers.Main).launch {
            var newSong = songViewmodel.getsongToAddInPlaylist("newsong")
            var popularSong = songViewmodel.getsongToAddInPlaylist("popularsong")
            var newPSongPlaylist =  Playlist(name = "New Songs" , songs = newSong)
            var popularSongPlaylist = Playlist(name = "Popular Songs" , songs = popularSong)

            val recyclerInfo = RecyclerViewHelper<Playlist<String>>(owner = viewLifecycleOwner)
            addSongAdapter = AddSongAdapter(recyclerInfo)
            binding.songViewpager.adapter = addSongAdapter

//            binding.songViewpager.offscreenPageLimit = 3
            addSongAdapter.submitList(mutableListOf(newPSongPlaylist , popularSongPlaylist))

            TabLayoutMediator(binding.tabLayout , binding.songViewpager){ tab, position ->
            }.attach()
        }

        var info = RecyclerViewHelper(type = "SONGSEARH" , interactionListener = this , owner = viewLifecycleOwner)
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
            Toast.makeText(requireContext() , selectedSongs.size.toString(), Toast.LENGTH_SHORT).show()
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