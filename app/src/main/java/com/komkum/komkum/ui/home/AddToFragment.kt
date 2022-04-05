package com.komkum.komkum.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.komkum.komkum.*

import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.AddToFragmentBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.library.LibraryViewModel
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.adaper.DownloadStyleListItemAdapter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddToFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> , View.OnClickListener {


    val libraryViewmodel : LibraryViewModel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()

    lateinit var binding : AddToFragmentBinding

    var selectedData :  List<String>? = null
    var selectedDataType = -1
    var songLists : List<Song<String,String>>? = null   // used to store list of songs from selected songs or songs from album to add to queue


    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                selectedData = it.getStringArrayList("SONG_IDS")
//                songLists =  it.getParcelableArrayList("SONG_LIST")
                songLists =  it.getParcelableArrayList<Song<String,String>>("SONG_LISTS")
                selectedDataType = it.getInt("DATA_TYPE")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddToFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        libraryViewmodel.getUserPlaylist()
        inflateView(selectedDataType)
        libraryViewmodel.playlistData.observe(viewLifecycleOwner , Observer { playlists ->
            binding.playlistLoadingProgressbar.isVisible = false
            if(playlists.isNullOrEmpty()) binding.noPlaylistErrorTextview.isVisible = true
            else{
                var pref = PreferenceHelper.getInstance(requireContext())
                var userId = pref.getString(AccountState.USER_ID , "")
                var info = RecyclerViewHelper(interactionListener = this ,  type = "PLAYLIST")
                var adapter = DownloadStyleListItemAdapter(info)
                binding.addRecyclerview.layoutManager = LinearLayoutManager(view.context)
                binding.addRecyclerview.adapter = adapter
                var pl = playlists.filterNot { pl -> pl.type == "CHART" }
                    .filter { pl -> pl.creatorId == userId }.map { playlist -> BaseModel(baseId = playlist._id , baseTittle = playlist.name , baseSubTittle =  "${playlist.songs?.size} songs") }
                adapter.submitList(pl)
            }

        })


        mainActivityViewmodel.getError().observe(viewLifecycleOwner){}
        mainActivityViewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext() , {mainActivityViewmodel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun inflateView(dataType : Int){
        var playlistItem = binding.createPlaylistItem
        var favoriteSongItem = binding.favoriteSongItem
        var favoriteAlbumItem = binding.favoriteAlbumItem
        var queueItem = binding.favoriteQueueItem

        when(dataType){
            LibraryService.CONTENT_TYPE_SONG -> favoriteAlbumItem.root.isVisible = false
            LibraryService.CONTENT_TYPE_ALBUM -> {
                favoriteSongItem.root.isVisible  = false
                queueItem.root.isVisible = false
            }
        }

        playlistItem.libraryItemImageView.setImageResource(R.drawable.ic_baseline_add_24)
        playlistItem.libraryItemTextview.text = getString(R.string.create_playlist)
        playlistItem.root.setOnClickListener(this)

        favoriteSongItem.libraryItemImageView.setImageResource(R.drawable.ic_round_favorite_border_24)
        favoriteSongItem.libraryItemTextview.text = getString(R.string.favorite_music)
        favoriteSongItem.root.setOnClickListener(this)

        favoriteAlbumItem.libraryItemImageView.setImageResource(R.drawable.ic_outline_album_24)
        favoriteAlbumItem.libraryItemTextview.text = getString(R.string.favorite_album)
        favoriteAlbumItem.root.setOnClickListener(this)

        queueItem.libraryItemImageView.setImageResource(R.drawable.ic_outline_queue_music_24)
        queueItem.libraryItemTextview.text = getString(R.string.add_to_queue)
        queueItem.root.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v?.id){
            R.id.favorite_song_item ->{
                if(selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    binding.playlistLoadingProgressbar.isVisible = true
                    selectedData?.toList()?.let {
                        mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , it).observe(viewLifecycleOwner){
                            binding.playlistLoadingProgressbar.isVisible = false
                            Toast.makeText(requireContext(), getString(R.string.song_added_to_favorite), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }

                }
                else if(selectedDataType == LibraryService.CONTENT_TYPE_ALBUM){
                    Toast.makeText(v?.context, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }

            }
            R.id.favorite_album_item ->{
                if(selectedDataType == LibraryService.CONTENT_TYPE_ALBUM){
                    binding.playlistLoadingProgressbar.isVisible = true
                    selectedData?.toList()?.let {
                        mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_ALBUM , it).observe(viewLifecycleOwner){
                            binding.playlistLoadingProgressbar.isVisible = false
                            Toast.makeText(v.context, getString(R.string.album_added_favorite), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                    }

                }
                else if (selectedDataType == LibraryService.CONTENT_TYPE_SONG){
                    Toast.makeText(v?.context, getString(R.string.error_message), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()

                }
                else if(selectedDataType == LibraryService.CONTENT_TYPE_ARTIST){

                }
            }
            R.id.favorite_queue_item -> {
                songLists?.toMutableList()?.let { mainActivityViewmodel.addToQueue(it) }
                findNavController().popBackStack()
            }

            R.id.create_playlist_item ->{
                var preference = PreferenceHelper.getInstance(requireContext())
                var creatorName = preference.get(AccountState.USERNAME , "")
                 MaterialDialog(requireContext()).show {
                    title(text = getString(R.string.create_playlist))
                    cornerRadius(14f)
                    var dialoginput = input(hint = getString(R.string.enter_playlist_name) , waitForPositiveButton = true,  allowEmpty = false){ materialDialog: MaterialDialog, charSequence: CharSequence ->
                        val playlistName = charSequence.toString()
                        if(!playlistName.isBlank()){
                            if(songLists.isNullOrEmpty()){
                                Toast.makeText(requireContext() , "No song found or selected" , Toast.LENGTH_LONG).show()
                                findNavController().navigateUp()
                                return@input
                            }
                            var playlistSongIds = songLists?.map { song -> song._id }
                            var playlist = Playlist(_id = null ,  name = playlistName , songs = playlistSongIds , creatorName = creatorName)
                            libraryViewmodel.createPlaylist(playlist).handleSingleDataObservation(viewLifecycleOwner){
                                (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , it._id , null , false , true)
                            }
                        }
                    }
                    positiveButton(text = getString(R.string.create))
                    negativeButton(text = getString(R.string.cancel))
                }
            }
        }
    }




    override fun onItemClick(selectedPlaylist: BaseModel, position: Int, option: Int?) {
        Log.i("songplaylistselec" , selectedPlaylist.toString())
        var selectedSongIds = songLists?.map { song -> song._id }
        mainActivityViewmodel.addToPlaylist(selectedPlaylist.baseId!! , selectedSongIds)
            .handleSingleDataObservation(viewLifecycleOwner){
            if(it){
                Toast.makeText(context, "added to ${selectedPlaylist.baseTittle}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }else {
                Toast.makeText(context, "faild to add data", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun activiateMultiSelectionMode(){}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}
