package com.komkum.komkum.ui.song

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.GenericSavedStateViewmmodelFactory
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.RecyclerViewHelper

import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.SongListFragmentBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.showListDialog
import com.komkum.komkum.util.extensions.toControllerActivity
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@AndroidEntryPoint
class SongListFragment : Fragment()  , IRecyclerViewInteractionListener<Song<String,String>>{

    companion object{
        const val LOAD_NEW_SONG = 0
        const val LOAD_POPULAR_SONG = 1
        const val LOAD_LIKED_SONG = 2
    }

    @Inject
    lateinit var songViewModelFactory: SongViewmodelFactory
    val songViewModel : SongViewModel by viewModels { GenericSavedStateViewmmodelFactory(songViewModelFactory , this) }

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var songListType : Int = -1
    var toolbarTitle : String? = null
    var isRequireApiRequst = true
    var artistSingleSongs : List<Song<String,String>> = emptyList()

    lateinit var binding : SongListFragmentBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
//        var application = requireActivity().application as ZomaTunesApplication
//        application.appComponent.inject(this)
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
            toolbarTitle = it.getString("TOOLBAR_TITLE") ?: "Single"

        }
        binding = SongListFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper("ARTIST" , mainActivityViewmodel.songrecyclerviewStateManeger , this  , viewLifecycleOwner)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listInfo = info
        binding.viewmodel = songViewModel
        binding.isFromCache = false

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (mainActivityViewmodel.songrecyclerviewStateManeger.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            else  findNavController().popBackStack()
        }

        if(!isRequireApiRequst){
            binding.songListProgressbar.visibility = View.GONE
            configureActionBar(binding.songListToolbar , toolbarTitle , "")
        }

        else{
            when(songListType){
                LOAD_LIKED_SONG -> {
//                    songViewModel.getLikedSong()
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

                else ->  configureActionBar(binding.songListToolbar , toolbarTitle)
            }
        }

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer{ metadata ->
            var songId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    var a = it::class.qualifiedName
                    when(a){
                        "com.komkum.komkum.data.model.Song" ->{
//                            playlistViewmodel.recyclerviewStateManager.selectedItem.value = it as Song<String, String>
                            var selectedSong = it as Song<String, String>
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = selectedSong
                        }
                    }
                }
            }
        })


        //multi selection onclick listners

        binding.selectionMenu.playIcon.setOnClickListener {

            var songs = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!
            mainActivityViewmodel.prepareAndPlaySongsBeta(songs , PlayerState.PlaylistState() , null , false)
            mainActivityViewmodel.playedFrom.value = "Playlist"
            deactivateMultiSelectionModel()


        }
        binding.selectionMenu.addIcon.setOnClickListener {
            deactivateMultiSelectionModel()
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                var songIds = it.map { song -> song._id }
                (requireActivity() as MainActivity).moveToAddtoFragment(songIds , LibraryService.CONTENT_TYPE_SONG , it)
            }
        }
        binding.selectionMenu.downloadIcon.setOnClickListener{
            var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
            if(isSubscriptionValid){
                mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                    if(it.isNotEmpty()){
                        songViewModel.downloadTracker.addDownloads(it , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                        Toast.makeText(context, "songs added to download", Toast.LENGTH_SHORT).show()
                        deactivateMultiSelectionModel()
                    }
                }
            }
            else findNavController().navigate(R.id.subscriptionFragment2)

        }
        binding.selectionMenu.deleteIcon.setOnClickListener{
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                var remainingSong = songViewModel.songLists.value?.filter { song -> !it.contains(song) }
                var songId = it.map { song -> song._id }
                (binding.songListRecyclerview.adapter as SongAdapter).submitList(remainingSong)
                deactivateMultiSelectionModel()
                (requireActivity() as MainActivity).showSnacbar("Song removed from favorites")
                mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG , songId)
            }
        }
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

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        if(mainActivityViewmodel.songrecyclerviewStateManeger.state.value is RecyclerviewState.MultiSelectionState){
            mainActivityViewmodel.songrecyclerviewStateManeger.addOrRemoveItem(data)
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
                    var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                    if(isSubscriptionValid){
                        songViewModel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                        (requireActivity() as MainActivity).showSnacbar("Song added to download")
                        deactivateMultiSelectionModel()
                    }
                    else findNavController().navigate(R.id.subscriptionFragment2)
                }
                SongAdapter.GO_TO_ARTIST_OPTION -> data.artists?.let {
                    if(data.artists!!.size > 1){
                        data.artistsName?.showListDialog(requireContext() , "Choose Artist"){index, value ->
                            (requireActivity() as MainActivity).moveToArtist(data.artists!![index])
                        }
                    }
                    else data.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }

                }
                SongAdapter.GO_TO_ALBUM_OPTION -> data.album?.let {
                    (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(
                        it, false , false)
                }
            }
        }
    }

    override fun activiateMultiSelectionMode() {
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.MultiSelectionState())
        (requireActivity() as MainActivity).hideBottomView()

        binding.selectionMenu.root.visibility = View.VISIBLE
    }

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}



    fun deactivateMultiSelectionModel() {
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        binding.selectionMenu.root.animation = animation
        binding.selectionMenu.root.visibility = View.GONE
        (requireActivity() as MainActivity).showBottomViewBeta()
    }




}
