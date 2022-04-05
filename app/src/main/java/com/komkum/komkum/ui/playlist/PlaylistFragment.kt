package com.komkum.komkum.ui.playlist

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.*
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper

import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.PlaylistFragmentBinding
import com.komkum.komkum.media.ServiceConnector
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.google.android.exoplayer2.offline.Download
import com.google.android.material.appbar.AppBarLayout
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.launch
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment() , IRecyclerViewInteractionListener<Song<String,String>> {

    companion object{
       const val LOAD_ARTIST_COLLECTION = 0
        const val  LOAD_LIKED_SONG = 1
        const val  LOAD_PLAYLIST = 2
        const val LOAD_FROM_PREV_REQUEST = 3
        const val LOAD_PLAYLIST_SONGS = 4
        const val LOAD_CHART = 5
    }


    val playlistViewmodel: PlaylistViewModel by viewModels()
    val downloadViewmodel: DownloadViewmodel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : PlaylistFragmentBinding

    var playlistDataType : Int? = null
    var loadFromCache : Boolean = false
    var isPlaylistPublic = false
    var isPlaylistSongsStartPlaying = false     // boolean value for wheather the playlist songs start playing
    var isPlaylistInLibrary = false
    var plylistCreatorId : String? = null
    var playlistCreateorName : String? = null
    var isFromAddtoFragment = false
    var isLikedPlaylist = false
    var isPlaylistChart = false

    var isErrorOccure = false   // house keeping boolean value when there is error in playlist

    var selectedSong : Song<String , String>? = null
    lateinit var playlistInfo : Playlist<String>
    lateinit var info : RecyclerViewHelper<Song<String , String>>
//    lateinit var info : RecyclerViewHelper<Song<String , String>>


    init {
        lifecycleScope.launch {
            whenCreated {
                arguments?.let {
                    playlistDataType = it.getInt("PLAYLIST_DATA_TYPE")
                    loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
                    mainActivityViewmodel.isLoadFromCache = loadFromCache
                    isFromAddtoFragment = it.getBoolean("FROM_ADD_TO_FRAGMENT")
                    requireActivity().invalidateOptionsMenu()

                    when(playlistDataType){
                        LOAD_ARTIST_COLLECTION ->{ playlistViewmodel.recommendedSongByArtist() }
                        LOAD_LIKED_SONG -> {
                            isLikedPlaylist = true
                            playlistViewmodel.getLikedSong()
                        }

                        LOAD_CHART -> {
                            isPlaylistChart = true
                            Log.i("chart" , isPlaylistChart.toString())
                            arguments?.getString("PLAYLIST_ID")?.let {
                                playlistViewmodel.getPlaylist(it , loadFromCache)
                                var result = playlistViewmodel.isPlaylistInLibrary(it)
                                isPlaylistInLibrary = result ?: false
                                requireActivity().invalidateOptionsMenu()
                            }
                        }

                        LOAD_PLAYLIST ->{ arguments?.getString("PLAYLIST_ID")?.let {
                            playlistViewmodel.getPlaylist(it , loadFromCache)
                            isPlaylistChart = true     // to flag playist is created by user so the remove bottom menu not disabled
                            var result = playlistViewmodel.isPlaylistInLibrary(it)
                               isPlaylistInLibrary = result ?: false
                               requireActivity().invalidateOptionsMenu()
                        }}
                        LOAD_FROM_PREV_REQUEST -> {
                            var playlist = arguments?.getParcelable<Playlist<Song<String,String>>>("PLAYLIST_DATA")
                            playlistViewmodel.playlistData.value = playlist
                        }
                        LOAD_PLAYLIST_SONGS ->{
                            playlistInfo = arguments?.getParcelable("PLAYLIST_INFO")!!
                            var playlistSongs =  playlistViewmodel.getPlaylistSongs(playlistInfo.songs!!)
                            playlistSongs?.let {
                                var sortedSongs =  it.sortedBy { song -> playlistInfo.songs!!.indexOf(song._id) }
                                var playlist = Playlist(_id = playlistInfo._id , name = playlistInfo.name , creatorId = playlistInfo.creatorId ,  songs = sortedSongs)
                                playlistViewmodel.playlistData.value = playlist
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }


    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(loadFromCache){
            mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
            mainActivityViewmodel.songrecyclerviewStateManeger._downloadingItems.value = playlistViewmodel.downloadTracker.getAllCurrentlyDownloadedItems()
        }
        else  mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())

        binding = PlaylistFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).showBottomViewBeta()
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[AccountState.IS_REDIRECTION] = true

        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 -> {
                    binding.playlistToolbar.title = ""
//                    binding.mainContainer.setBackgroundColor(resources.getColor(R.color.light_secondaryDarkColor))
                }
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.customPlaylistImageview.visibility = View.INVISIBLE
                    binding.playlistToolbar.title = binding.playlistNameTextview.text.toString()
//                    binding.mainContainer.background = resources.getDrawable(R.drawable.item_selected)
                }
                verticalOffset > -appBarLayout.totalScrollRange -> binding.customPlaylistImageview.visibility = View.VISIBLE
            }
        })
        var playlistType = if(loadFromCache) "USER_PLAYLIST" else "PLAYLIST"
        info = RecyclerViewHelper(playlistType , mainActivityViewmodel.songrecyclerviewStateManeger ,
            this , viewLifecycleOwner , playlistViewmodel.downloadTracker)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = playlistViewmodel
        binding.listInfo = info
        binding.isFromCache = loadFromCache
        binding.isLikedSongPlaylist = isLikedPlaylist


        configureActionBar(binding.playlistToolbar)


        playlistViewmodel.playlistData.observe(viewLifecycleOwner , Observer{playlist ->
            playlist?.let {
                it.creatorId?.let { plylistCreatorId = it }
                it.creatorName?.let { playlistCreateorName = it }
                if(it.songs.isNullOrEmpty()){
                    binding.playlistErrorTextview.text = "No song found \n Your favorite songs goes here"
                    binding.playlistErrorTextview.isVisible = true
                } else binding.playlistErrorTextview.isVisible = false

                isPlaylistPublic = it.isPublic ?: false
                requireActivity().invalidateOptionsMenu()
                var subtitle = "${it.songs?.size ?:"0"} songs span By ${it.creatorName ?: "Komkum"}"
                var spanner = Spanner(subtitle).span("span" , Spans.custom { RadioSpan(binding.root.context , R.drawable.tab_item_not_active , resources.getInteger(R.integer.bullet_margin)) })
                binding.playlistSongSizeTextview.text = spanner
                var imageUris = it.songs?.distinctBy { song -> song.thumbnailPath }?.take(4)
                    ?.map { song -> song.thumbnailPath!! }
                    ?.map { image -> image }
                imageUris?.let { it1 -> binding.customPlaylistImageview.loadImage(it1)}

                mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer{metadata ->
                    var songId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                    songId?.let { it ->
                        mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                            var a = it::class.qualifiedName
                            when(a){
                                "com.komkum.komkum.data.model.Song" ->{
//                            playlistViewmodel.recyclerviewStateManager.selectedItem.value = it as Song<String, String>
                                    selectedSong = it as Song<String, String>
                                    mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = selectedSong
                                }
                            }
                        }
                    }
                })
            }
        })


        // observer for added song from add song fragment
        mainActivityViewmodel.newAddedSongToPlaylist.observe(viewLifecycleOwner){
            Log.i("songlistsongfrag" , it.size.toString())
            var prevSongList = playlistViewmodel.playlistData.value?.songs?.toMutableList()
            prevSongList?.addAll(0 , it)
            playlistViewmodel.playlistData.value?.apply { songs = prevSongList }.also { playlistViewmodel.playlistData.value = it }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            if (playlistViewmodel.recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            if (mainActivityViewmodel.songrecyclerviewStateManeger.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
//            if(isFromAddtoFragment) findNavController().
            else  findNavController().popBackStack()
        }


        //multiselection menu buttons
        binding.selectionMenu.favIcon.setOnClickListener {
            binding.playlistSongProgressbar.isVisible = true
//            var songIds = playlistViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id }
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds)).observe(viewLifecycleOwner){
                binding.playlistSongProgressbar.isVisible = false
                if(it == true) Toast.makeText(view.context, getString(R.string.song_added_to_favorite), Toast.LENGTH_SHORT).show()

            }
            deactivateMultiSelectionModel()
        }

        binding.selectionMenu.addIcon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                var songIds = it.map { song -> song._id}
                (requireActivity() as MainActivity).moveToAddtoFragment(songIds, LibraryService.CONTENT_TYPE_SONG , it.toMutableList())

            }
            deactivateMultiSelectionModel()
        }

        binding.selectionMenu.playIcon.setOnClickListener {
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            playlistViewmodel.playlistData.value?.let {playlist ->
                deactivateMultiSelectionModel()
                playlist.songs?.filter { song -> songIds.contains(song._id) }?.let {
                     mainActivityViewmodel.prepareAndPlaySongsBeta(it , PlayerState.PlaylistState() , null , false , loadFromCache)
                     mainActivityViewmodel.playedFrom.value = "${playlist.name} \n Playlist"
                 }
            }
        }

        binding.selectionMenu.downloadIcon.setOnClickListener {
            var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
            deactivateMultiSelectionModel()
            if(isSubscriptionValid){
               mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let { selectedItems ->
                if(selectedItems.isNotEmpty()){
                    playlistViewmodel.downloadPlaylistSongs(selectedItems)
                    playlistViewmodel.playlistData.value?.let {
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_downloads) , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 0)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                        playlistViewmodel.downloadTracker.addDownloads(selectedItems , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                    }
                }
               }
            }
            else findNavController().navigate(R.id.subscriptionFragment2)

        }

//        delete_icon.isEnabled = false
        binding.selectionMenu.deleteIcon.setOnClickListener {
            deactivateMultiSelectionModel()
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let { selectedPlaylistSongs ->
                if(loadFromCache){
                    var songIds = selectedPlaylistSongs.map { song -> song._id }
                    if(selectedPlaylistSongs.size == playlistViewmodel.playlistData.value!!.songs!!.size){
                        downloadViewmodel.removeDownloadedPlaylist(playlistViewmodel.playlistData.value!!._id!!)
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download)){}
                        findNavController().popBackStack()
                    }
                    else{
                        downloadViewmodel.removePlaylistSongs(songIds , playlistViewmodel.playlistData.value!!._id!!)

                        if(!mainActivityViewmodel.playlistQueue.value.isNullOrEmpty()){
                            selectedPlaylistSongs.forEach { song ->
                                var songs = mainActivityViewmodel.playlistQueue.value as List<Song<String,String>>
                                songs.indexOf(song).let { mainActivityViewmodel.removeQueueItem(it) }
                            }
                        }


                       var updatedSongList =  playlistViewmodel.playlistData.value!!.songs?.toMutableList()
                        updatedSongList?.removeAll(selectedPlaylistSongs)
                        deactivateMultiSelectionModel()
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download))
                        var updatedPlaylist = playlistViewmodel.playlistData.value?.apply {
                            songs = updatedSongList
                        }
                        playlistViewmodel.playlistData.value = updatedPlaylist
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download))
                    }
                }
                else{
                    var songs = selectedPlaylistSongs.map { song -> song._id }

                    var modifiedSong =   playlistViewmodel.playlistData.value!!.songs!!.toMutableList()
                    modifiedSong.removeAll { song -> songs.contains(song._id) }
                    playlistViewmodel.playlistData.value!!.songs = modifiedSong

                    (binding.playlistSongRecyclerview.adapter as SongAdapter).submitList(modifiedSong)
                    deactivateMultiSelectionModel()
                    if(isLikedPlaylist){
                        mainActivityViewmodel.removeFromFavorite(LibraryService.CONTENT_TYPE_SONG, songs)
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.removed_from_library))

                    }
                    else{

                        var playlist = Playlist(_id = playlistViewmodel.playlistData.value!!._id , songs = songs)
                        playlistViewmodel.removePlaylistSongs(playlist).handleSingleDataObservation(viewLifecycleOwner){
                            if(it){
                                if(selectedPlaylistSongs.size == playlist.songs!!.size){
                                    (requireActivity() as MainActivity).showSnacbar(getString(R.string.removed_from_library))
                                    findNavController().popBackStack()
                                }
                                else (requireActivity() as MainActivity).showSnacbar(getString(R.string.song_removed_from_playlist))
                            }
                            else Toast.makeText(context, "error occured", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }

       binding.selectionMenu.selectAllCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = playlistViewmodel.playlistData.value?.songs
            else mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
        }



        binding.shufflePlaylistBtn.setOnClickListener{
            playlistViewmodel.playlistData.value?.let{playlist ->
                playlist.songs?.let{
                    mainActivityViewmodel.prepareAndPlaySongsBeta(it , PlayerState.PlaylistState() , null , true , loadFromCache)
                    mainActivityViewmodel.playedFrom.value = "${playlist.name} | Playlist"
                }
            }
        }

        playlistViewmodel.getError().observe(viewLifecycleOwner){}
        playlistViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.playlistSongProgressbar.isVisible = false
            error?.handleError(requireContext() , {
                mainActivityViewmodel.removeOldErrors()
                playlistViewmodel.removeOldErrors() }){
               if(!loadFromCache){
                   binding.playlistErrorTextview.isVisible = true
                   isErrorOccure = true
                   requireActivity().invalidateOptionsMenu()
               }
                binding.playlistSongProgressbar.isVisible = false
            }
        }

        mainActivityViewmodel.getError().observe(viewLifecycleOwner){}
        mainActivityViewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext() , { mainActivityViewmodel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isErrorOccure){
            menu.clear()
            return
        }

        if(loadFromCache){
            menu.removeItem(R.id.playlist_download_menu_item)
            menu.removeItem(R.id.make_playlist_public_menu_item)
            menu.removeItem(R.id.add_song_menu_item)
            menu.removeItem(R.id.playlist_remove_from_fav_menu_item)
            menu.removeItem(R.id.playlist_add_to_fav_menu_item)
            menu.removeItem(R.id.make_playlist_public_menu_item)
            menu.removeItem(R.id.remove_public_playlist)
        }
        
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.getString(AccountState.USER_ID , "")
        if(userId.isNullOrBlank() || userId != plylistCreatorId){
            binding.removeFlag = false
            menu.removeItem(R.id.add_song_menu_item)
            menu.removeItem(R.id.playlist_delete_menu_item)
            menu.removeItem(R.id.make_playlist_public_menu_item)
            menu.removeItem(R.id.remove_public_playlist)
        }else{
            info.type = "USER_PLAYLIST"
            binding.removeFlag = true
            menu.removeItem(R.id.playlist_remove_from_fav_menu_item)
            menu.removeItem(R.id.playlist_add_to_fav_menu_item)
            if(isPlaylistPublic) menu.removeItem(R.id.make_playlist_public_menu_item)
            else menu.removeItem(R.id.remove_public_playlist)
        }

        if(isPlaylistInLibrary) menu.removeItem(R.id.playlist_add_to_fav_menu_item)
        else menu.removeItem(R.id.playlist_remove_from_fav_menu_item)

        if(plylistCreatorId.isNullOrEmpty()){
            menu.removeItem(R.id.playlist_add_to_fav_menu_item)
            menu.removeItem(R.id.playlist_remove_from_fav_menu_item)
        }

        if(playlistCreateorName == "ZomaTunes" || playlistCreateorName == "Komkum"){
            menu.removeItem(R.id.playlist_remove_from_fav_menu_item)
            menu.removeItem(R.id.add_song_menu_item)
            menu.removeItem(R.id.make_playlist_public_menu_item)
            menu.removeItem(R.id.remove_public_playlist)
            menu.removeItem(R.id.playlist_delete_menu_item)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.playlist_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
           android.R.id.home -> {
               findNavController().navigateUp()
               true
           }

           R.id.playlist_download_menu_item ->{
               var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
               if(isSubscriptionValid){
                   playlistViewmodel.playlistData.value?.let {
                       playlistViewmodel.downloadPlaylist(it)
                       (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_downloads) , "View"){
                           findNavController().navigate(R.id.downloadListFragment)
                       }
                       playlistViewmodel.downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)}
               }
               else {
                   findNavController().navigate(R.id.subscriptionFragment2)
               }

                true
            }
            R.id.add_song_menu_item ->{
                playlistViewmodel.playlistData.value?.let {
                    (requireActivity() as MainActivity).movetoAddSongFragment(it._id)
                }
                true
            }
           R.id.make_playlist_public_menu_item ->{
               binding.playlistSongProgressbar.isVisible = true
               playlistViewmodel.playlistData.value?.let {
                   playlistViewmodel.makePlaylistPublic(it._id!!).observe(viewLifecycleOwner , Observer { ispublic ->
                       binding.playlistSongProgressbar.isVisible = false
                       isPlaylistPublic = ispublic
                       requireActivity().invalidateOptionsMenu()
                       Toast.makeText(requireContext() , getString(R.string.playlist_access_by_others) , Toast.LENGTH_LONG).show()
                   })
               }

               true
           }
           R.id.playlist_edit_menu_item ->{
               mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
               activiateMultiSelectionMode()
               true
           }
           R.id.playlist_add_to_fav_menu_item -> {
               binding.playlistSongProgressbar.isVisible = true
               playlistViewmodel.playlistData.value?.let {
                   playlistViewmodel.addPlaylisttoFav(it._id!!).handleSingleDataObservation(viewLifecycleOwner) {
                       binding.playlistSongProgressbar.isVisible = false
                       if (it) {
                               (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_library))
                               isPlaylistInLibrary = true
                               requireActivity().invalidateOptionsMenu()
                           }
                       }
               }
               true
           }

           R.id.playlist_remove_from_fav_menu_item ->{
               binding.playlistSongProgressbar.isVisible = true
               playlistViewmodel.playlistData.value?.let {
                   playlistViewmodel.removePlaylistFromFav(it._id!!).handleSingleDataObservation(viewLifecycleOwner) {
                       binding.playlistSongProgressbar.isVisible = false
                       if (it) {
                           (requireActivity() as MainActivity).showSnacbar(getString(R.string.removed_from_library))
                           isPlaylistInLibrary = !it
                           requireActivity().invalidateOptionsMenu()
                       }
                   }
               }
               true
           }

           R.id.playlist_delete_menu_item ->{
               playlistViewmodel.playlistData.value?.let {
                   binding.playlistSongProgressbar.isVisible = true
                   if(loadFromCache){
                       downloadViewmodel.removeDownloadedPlaylist(it._id!!)
                       binding.playlistSongProgressbar.isVisible = false
                       (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download) , "Undo"){}
                       findNavController().popBackStack()
                   }
                   else{
                       playlistViewmodel.deletePlaylist(it._id!!).handleSingleDataObservation(viewLifecycleOwner){
                           binding.playlistSongProgressbar.isVisible = false
                           (requireActivity() as MainActivity).showSnacbar(getString(R.string.playlist_deleted) , "Undo"){}
                           findNavController().popBackStack()
                       }
                   }
               }

               true
           }
           else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mainActivityViewmodel.isAudioPrepared = false
        binding.playlistSongRecyclerview.adapter = null
        super.onDestroyView()
    }

    


    override fun activiateMultiSelectionMode() {
//        playlistViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.MultiSelectionState())
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.MultiSelectionState())
        (requireActivity() as MainActivity).hideBottomView()
        binding.selectionMenu.root.visibility = View.VISIBLE
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        when (mainActivityViewmodel.songrecyclerviewStateManeger.state.value) {
            is RecyclerviewState.MultiSelectionState -> {
                var size =  mainActivityViewmodel.songrecyclerviewStateManeger.addOrRemoveItem(data)
            }
            is RecyclerviewState.SingleSelectionState -> {
                when(option){
                    SongAdapter.NO_OPTION ->{
                        playlistViewmodel.playlistData.value?.songs?.let {
                            mainActivityViewmodel.prepareAndPlaySongsBeta(it, PlayerState.PlaylistState() , position , false , loadFromCache , 1)
//                            playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                        }
                    }
                    SongAdapter.PLAY_OPTION ->{
                        mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null , false , loadFromCache)
//                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                        mainActivityViewmodel.playedFrom.value = "${playlistViewmodel.playlistData.value?.name} | Playlist"
                    }

                    SongAdapter.ADD_TO_OPTION ->{
                        (requireActivity() as MainActivity).moveToAddtoFragment(mutableListOf(data._id), LibraryService.CONTENT_TYPE_SONG , mutableListOf(data))
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        deactivateMultiSelectionModel()
                        var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                        if(isSubscriptionValid){
                            playlistViewmodel.downloadPlaylistSongs(listOf(data))
                            playlistViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                            (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_downloads) , "View"){
                                findNavController().navigate(R.id.downloadListFragment)
                            }
                        }
                        else findNavController().navigate(R.id.subscriptionFragment2)
                    }

                    SongAdapter.SONG_RADIO_OPTION -> {
                        (requireActivity() as MainActivity).movetoSingleRadioStation(songId = data._id)
                    }

                    SongAdapter.ARTIST_RADIO_OPTION ->{
                        if(data.artists!!.size > 1){
                            MaterialDialog(requireContext()).show {
                               var items =  listItems(items = data.artists){dialog, index, text ->
                                    (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![index])
                                }
                            }
                        }
                        else (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![0])
                    }

                    SongAdapter.GO_TO_ARTIST_OPTION ->{
                        if(data.artists!!.size > 1){
                            data.artistsName?.showListDialog(requireContext() , getString(R.string.choose_artist)){ index, value ->
                                (requireActivity() as MainActivity).moveToArtist(data.artists!![index])
                            }
                        }
                        else data.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }

                    }
                    SongAdapter.GO_TO_ALBUM_OPTION -> {
                        if(data.album != null){
                            (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.album!!, false , false)
                        }
                        else Toast.makeText(requireContext() , "${data.albumName} This song is single" , Toast.LENGTH_SHORT).show()
                    }
                    SongAdapter.SONG_INFO_OPTION ->{
                        mainActivityViewmodel.showSongOption(data)
                    }
                }
            }
            is RecyclerviewState.DownloadState ->{
                when(option){
                    SongAdapter.REMOVE_DOWNLOAD_OPTION ->{
                            downloadViewmodel.removePlaylistSongs(listOf(data._id) , playlistViewmodel.playlistData.value!!._id!!)
                            var updatedSongList =  playlistViewmodel.playlistData.value!!.songs?.toMutableList()
                            updatedSongList?.removeAll(listOf(data))
                            var updatedPlaylist = playlistViewmodel.playlistData.value?.apply {
                                songs = updatedSongList
                            }

                        playlistViewmodel.playlistData.value = updatedPlaylist
                        if(!mainActivityViewmodel.playlistQueue.value.isNullOrEmpty()){
                            var songs = mainActivityViewmodel.playlistQueue.value as List<Song<String,String>>
                            songs.indexOf(data).let { mainActivityViewmodel.removeQueueItem(it) }
                        }

                    }

                    else ->{
                        playlistViewmodel.downloadTracker.getCurrentDownloadItem(data._id)?.let {
                            Toast.makeText(context , getString(R.string.download_not_completed) , Toast.LENGTH_SHORT).show()
                            return
                        }
                        playlistViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
                            var songUri = Uri.parse(data.mpdPath!!)
                            var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
                            if(isFailed){
                                Toast.makeText(context ,  getString(R.string.download_failed) , Toast.LENGTH_SHORT).show()
                                return
                            }
                        }

                        playlistViewmodel.playlistData.value?.songs?.let{plsongs ->
//                            if(loadFromCache){
//                                downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
//                                    var ids = it.map { download -> download.request.id }
//                                    if(!ids.isNullOrEmpty()){
//                                        var songs = plsongs.filter { song -> ids.contains(song._id) }
//                                        var selectedSongPosition = songs.indexOf(data)
//                                        Log.i("select index" , "${songs.size} $selectedSongPosition")
//                                        mainActivityViewmodel.prepareAndPlaySongsBeta(songs , PlayerState.PlaylistState() , selectedSongPosition , false , loadFromCache)
//                                    }else Toast.makeText(requireContext(), "No song downloaded" , Toast.LENGTH_LONG).show()
//                                }
//                            }
                            mainActivityViewmodel.prepareAndPlaySongsBeta(plsongs , PlayerState.PlaylistState() , position , false , loadFromCache)
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                            mainActivityViewmodel.playedFrom.value = "${playlistViewmodel.playlistData.value?.name} | Playlist"
                        }
                    }
                }
            }
        }
    }


    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        Log.i("moveplaylist" , newPosition.toString())
        playlistViewmodel.playlistData.value?.let {
            var songs =  it.songs!!.toMutableList()
            var removedSong = songs.removeAt(prevPosition)
            songs.add(newPosition , removedSong)
            it.songs = songs
            var songIds = songs.map { song -> song._id }
            var newPlaylist = Playlist(_id = it._id , songs = songIds , creatorId = it.creatorId , name = it.name , sid = it.sid , date = it.date)
            playlistViewmodel.updatePlaylist(newPlaylist).handleSingleDataObservation(viewLifecycleOwner){}
        }
    }


    fun prepareAndPlaySongs(songs : List<Song<String,String>> ,  position: Int? , isShuffled : Boolean? = false){
        mainActivityViewmodel.playlistQueue.value = songs.toMutableList()
        if(loadFromCache) mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_DOWNLOAD , isShuffled)
        else mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA , isShuffled)

        mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
        if (position != null)  mainActivityViewmodel.play(position.toLong())
        else  mainActivityViewmodel.play()
    }


    fun deactivateMultiSelectionModel() {
        if(loadFromCache) mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
        else mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        binding.selectionMenu.root.animation = animation
        binding.selectionMenu.root.visibility = View.GONE
        (requireActivity() as MainActivity).showBottomViewBeta()
    }



}
