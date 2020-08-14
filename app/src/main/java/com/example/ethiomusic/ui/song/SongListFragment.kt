package com.example.ethiomusic.ui.song

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.EthioMusicApplication
import com.example.ethiomusic.GenericSavedStateViewmmodelFactory
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.MainActivityViewmodel
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.RecyclerViewHelper

import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.SongListFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import com.google.android.exoplayer2.C
import kotlinx.android.synthetic.main.album_fragment.*
import kotlinx.android.synthetic.main.multi_selection_menu.*
import kotlinx.android.synthetic.main.song_list_fragment.*
import kotlinx.android.synthetic.main.song_list_fragment.selection_menu
import javax.inject.Inject

class SongListFragment : Fragment()  , IRecyclerViewInteractionListener<Song<String,String>>{

    companion object{
        const val LOAD_NEW_SONG = 0
        const val LOAD_POPULAR_SONG = 1
        const val LOAD_LIKED_SONG = 2
    }

    val recyclerviewStateManager : RecyclerviewStateManager<Song<String,String>> by lazy {
        RecyclerviewStateManager<Song<String,String>>()
    }
    @Inject
    lateinit var songViewModelFactory: SongViewmodelFactory
    val songViewModel : SongViewModel by viewModels { GenericSavedStateViewmmodelFactory(songViewModelFactory , this) }

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var songListType : Int = -1
    var isRequireApiRequst = true
    var artistSingleSongs : List<Song<String,String>> = emptyList()

    lateinit var binding : SongListFragmentBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let{
            it.getParcelableArrayList<Song<String,String>>("SONGLIST")?.let {
                isRequireApiRequst = false
                songViewModel.songLists.value = it.toList()
            }
            it.getInt("SONG_LIST_TYPE")?.let {
                songListType = it
            }

        }
        binding = SongListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper<Song<String,String>>("ARTIST" , recyclerviewStateManager , this  , viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listInfo = info
        binding.viewmodel = songViewModel
        binding.isFromCache = false

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            else  findNavController().popBackStack()
        }

        if(!isRequireApiRequst){
            binding.songListProgressbar.visibility = View.GONE
            configureActionBar(binding.songListToolbar , "Singles" , "")
        }

        else{
            when(songListType){
                LOAD_LIKED_SONG -> {
                    songViewModel.getLikedSong()
                    configureActionBar(binding.songListToolbar , "Liked Songs")
                }
                LOAD_NEW_SONG ->{
                    songViewModel.getNewSongs()
                    configureActionBar(binding.songListToolbar , "New Songs")
                }
                LOAD_POPULAR_SONG ->{
                    songViewModel.getPopularSongs()
                    configureActionBar(binding.songListToolbar , "Popular Songs")
                }
            }
        }

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer{ metadata ->
            var songId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    recyclerviewStateManager.selectedItem.value = it
                }
            }
        })


        //multi selection onclick listners

        play_icon.setOnClickListener {
            recyclerviewStateManager.multiselectedItems.value?.let {
                mainActivityViewmodel.prepareAndPlaySongsBeta(it , PlayerState.PlaylistState() , null)
            }
        }
        add_icon.setOnClickListener {
            recyclerviewStateManager.multiselectedItems.value?.let {
              var data =   it.map { song ->
                    BaseModel(baseId = song._id  , baseSubTittle = song.tittle ,  baseImagePath = song.thumbnailPath ,
                        baseDescription = song.mpdPath , baseListOfInfo = song.artists as List<String>)}
                deactivateMultiSelectionModel()
                (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_SONG)
            }
        }
        download_icon.setOnClickListener{
            recyclerviewStateManager.multiselectedItems.value?.let {
                if(it.isNotEmpty()){
                    songViewModel.downloadSongs(it , viewLifecycleOwner)
                    Toast.makeText(context, "songs added to download", Toast.LENGTH_SHORT).show()
                    deactivateMultiSelectionModel()
                }
            }
        }
        delete_icon.setOnClickListener{
            recyclerviewStateManager.multiselectedItems.value?.let {
                var remainingSong = songViewModel.songLists.value?.filter { song -> !it.contains(song) }
                var songId = it.map { song -> song._id }
                (binding.songListRecyclerview.adapter as SongAdapter).submitList(remainingSong)
                deactivateMultiSelectionModel()
                (requireActivity() as MainActivity).showSnacbar("Song removed from favorites")
                mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG , songId)
            }
        }
    }


    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        if(recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState){
            recyclerviewStateManager.addOrRemoveItem(data)
        }else{
            when(option){
                SongAdapter.NO_OPTION -> {
                    songViewModel.songLists.value?.let {
                        mainActivityViewmodel.prepareAndPlaySongsBeta(it, PlayerState.PlaylistState() , position)
                    }
                }
                SongAdapter.PLAY_OPTION -> mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
                SongAdapter.ADD_TO_OPTION -> {}
                SongAdapter.DOWNLOAD_OPTION ->{
                    songViewModel.downloadSongs(listOf(data) , viewLifecycleOwner)
                    (requireActivity() as MainActivity).showSnacbar("Song added to download")
                    deactivateMultiSelectionModel()
                }
                SongAdapter.GO_TO_ARTIST_OPTION -> data.artists?.let {
                    (requireActivity() as MainActivity).moveToArtist(it[0])
                }
                SongAdapter.GO_TO_ALBUM_OPTION -> data.album?.let {
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(
                        it, false , false)
                }
            }
        }
    }

    override fun activiateMultiSelectionMode() {
        recyclerviewStateManager.changeState(RecyclerviewState.MultiSelectionState())
        this.toControllerActivity().hideBottomView()
        selection_menu.visibility = View.VISIBLE
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }



    fun deactivateMultiSelectionModel() {
        recyclerviewStateManager.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        selection_menu.animation = animation
        selection_menu.visibility = View.GONE
        this.toControllerActivity().showBottomView()
    }




}
