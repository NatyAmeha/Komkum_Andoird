package com.zomatunes.zomatunes.ui.playlist

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
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
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Playlist
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper

import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.PlaylistFragmentBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.ui.download.DownloadViewmodel
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.extensions.*
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import com.google.android.exoplayer2.offline.Download
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.album_fragment.selection_menu
import kotlinx.android.synthetic.main.multi_selection_menu.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaylistFragment : Fragment() , IRecyclerViewInteractionListener<Song<String,String>> {

    companion object{
       const val LOAD_ARTIST_COLLECTION = 0
        const val  LOAD_LIKED_SONG = 1
        const val  LOAD_PLAYLIST = 2
        const val LOAD_FROM_PREV_REQUEST = 3
        const val LOAD_PLAYLIST_SONGS = 4
    }



    val playlistViewmodel: PlaylistViewModel by viewModels()
    val downloadViewmodel: DownloadViewmodel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : PlaylistFragmentBinding

    var playlistDataType : Int? = null
    var loadFromCache : Boolean = false
    var isPlaylistPublic = false
    var isPlaylistInLibrary = false
    var plylistCreatorId : String? = null
    var isFromAddtoFragment = false
    var isLikedPlaylist = false

    var selectedSong : Song<String , String>? = null
    lateinit var playlistInfo : Playlist<String>
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
                    if(loadFromCache){
                        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
                        mainActivityViewmodel.songrecyclerviewStateManeger._downloadingItems.value = playlistViewmodel.downloadTracker.getAllCurrentlyDownloadedItems()
                    }
                    else  mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())

                    when(playlistDataType){
                        LOAD_ARTIST_COLLECTION ->{ playlistViewmodel.recommendedSongByArtist() }

                        LOAD_LIKED_SONG -> {
                            isLikedPlaylist = true
                            playlistViewmodel.getLikedSong()
                        }

                        LOAD_PLAYLIST ->{ arguments?.getString("PLAYLIST_ID")?.let {
                            playlistViewmodel.getPlaylist(it , loadFromCache)
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
        binding = PlaylistFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 -> binding.playlistToolbar.title = ""
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.customPlaylistImageview.visibility = View.INVISIBLE
                    binding.playlistToolbar.title = binding.playlistNameTextview.text.toString()
                }
                verticalOffset > -appBarLayout.totalScrollRange -> binding.customPlaylistImageview.visibility = View.VISIBLE
            }
        })
        var info = RecyclerViewHelper("PLAYLIST" , mainActivityViewmodel.songrecyclerviewStateManeger ,
            this , viewLifecycleOwner , playlistViewmodel.downloadTracker)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = playlistViewmodel
        binding.listInfo = info
        binding.isFromCache = loadFromCache
        binding.isLikedSongPlaylist = isLikedPlaylist

        configureActionBar(binding.playlistToolbar)
        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer{metadata ->
            var songId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let { it ->
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    var a = it::class.qualifiedName
                    when(a){
                         "com.example.com.zomatunes.zomatunes.data.model.Song" ->{
//                            playlistViewmodel.recyclerviewStateManager.selectedItem.value = it as Song<String, String>
                             selectedSong = it as Song<String, String>
                             Log.i("infoo" , selectedSong?.tittle)
//                             info.stateManager?.selectedItem?.value = selectedSong
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = selectedSong
                        }
                    }
                }
            }
        })



        playlistViewmodel.playlistData.observe(viewLifecycleOwner , Observer{
            plylistCreatorId = it.creatorId
            isPlaylistPublic = it.isPublic ?: false
            requireActivity().invalidateOptionsMenu()
            var imageUris = it.songs?.distinctBy { song -> song.thumbnailPath }?.take(4)
                ?.map { song -> song.thumbnailPath!! }
                ?.map { image -> image.replace("localhost" , AdapterDiffUtil.URL) }
            imageUris?.let { it1 -> binding.customPlaylistImageview.loadImage(it1)}
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            if (playlistViewmodel.recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            if (mainActivityViewmodel.songrecyclerviewStateManeger.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
//            if(isFromAddtoFragment) findNavController().
            else  findNavController().popBackStack()
        }





        //multiselection menu buttons
        fav_icon.setOnClickListener {
//            var songIds = playlistViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id }
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds))
            Toast.makeText(view.context, "songs added to favorite", Toast.LENGTH_SHORT).show()
            deactivateMultiSelectionModel()
        }

        add_icon.setOnClickListener {
            var data = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song ->
                BaseModel(baseId = song._id  , baseSubTittle = song.tittle ,  baseImagePath = song.thumbnailPath ,
                    baseDescription = song.mpdPath , baseListOfInfo = song.artists as List<String>) }
            deactivateMultiSelectionModel()
            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_SONG)
        }

        play_icon.setOnClickListener {
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            playlistViewmodel.playlistData.value?.let {
                var songs = it.songs?.filter { song -> songIds.contains(song._id) }
                mainActivityViewmodel.playlistQueue.value = songs
                mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_TYPE_MEDIA)
                deactivateMultiSelectionModel()
            }
        }

        download_icon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let { selectedItems ->
                if(selectedItems.isNotEmpty()){
                    playlistViewmodel.downloadPlaylistSongs(selectedItems)
                    playlistViewmodel.playlistData.value?.let {
                        (requireActivity() as MainActivity).showSnacbar("Added to Downloads" , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 0)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                        playlistViewmodel.downloadTracker.addDownloads(selectedItems , viewLifecycleOwner)
                        deactivateMultiSelectionModel()
                    }
                }
            }
        }

//        delete_icon.isEnabled = false
        delete_icon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let { selectedPlaylistSongs ->
                if(loadFromCache){
                    var songIds = selectedPlaylistSongs.map { song -> song._id }
                    if(selectedPlaylistSongs.size == playlistViewmodel.playlistData.value!!.songs!!.size){
                        downloadViewmodel.removeDownloadedPlaylist(playlistViewmodel.playlistData.value!!._id!!)
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads" , "Undo"){}
                        findNavController().popBackStack()
                    }
                    else{
                        downloadViewmodel.removePlaylistSongs(songIds , playlistViewmodel.playlistData.value!!._id!!)
                       var updatedSongList =  playlistViewmodel.playlistData.value!!.songs?.toMutableList()
                        updatedSongList?.removeAll(selectedPlaylistSongs)
                        deactivateMultiSelectionModel()
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads")
                        var updatedPlaylist = playlistViewmodel.playlistData.value?.apply {
                            songs = updatedSongList
                        }
                        playlistViewmodel.playlistData.value = updatedPlaylist
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads")
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
                        (requireActivity() as MainActivity).showSnacbar("Song removed from Liked songs")

                    }
                    else{

                        var playlist = Playlist(_id = playlistViewmodel.playlistData.value!!._id , songs = songs)
                        playlistViewmodel.removePlaylistSongs(playlist).handleSingleDataObservation(viewLifecycleOwner){
                            if(it){
                                if(selectedPlaylistSongs.size == playlist.songs!!.size){
                                    (requireActivity() as MainActivity).showSnacbar("Playlist removed from Library")
                                    findNavController().popBackStack()
                                }
                                else (requireActivity() as MainActivity).showSnacbar("Song removed from playlist")
                            }
                            else Toast.makeText(context, "error occured", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }

        select_all_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = playlistViewmodel.playlistData.value?.songs
            else mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
        }



        binding.shufflePlaylistBtn.setOnClickListener{
            playlistViewmodel.playlistData.value?.songs?.let {
                prepareAndPlaySongs(it , null , true)
            }

            playlistViewmodel.playlistData.value?.songs?.let{plsongs ->
                if(loadFromCache){
                    downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
                        var ids = it.map { download -> download.request.id }
                        if(!ids.isNullOrEmpty()){
                            var songs = plsongs.filter { song -> ids.contains(song._id) }

                            mainActivityViewmodel.prepareAndPlaySongsBeta(songs , PlayerState.PlaylistState() , null , true , loadFromCache)
                        }else Toast.makeText(requireContext(), "No song downloaded" , Toast.LENGTH_LONG).show()
                    }
                }
                else mainActivityViewmodel.prepareAndPlaySongsBeta(plsongs , PlayerState.PlaylistState() , null , true , loadFromCache)


            }


        }

        playlistViewmodel.getError().observe(viewLifecycleOwner){}
        playlistViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext()){
                binding.playlistErrorTextview.isVisible = true
                binding.playlistSongProgressbar.isVisible = false
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
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
        if(!userId.isNullOrBlank() && userId != plylistCreatorId){
            menu.removeItem(R.id.add_song_menu_item)
            menu.removeItem(R.id.playlist_delete_menu_item)
            menu.removeItem(R.id.make_playlist_public_menu_item)
            menu.removeItem(R.id.remove_public_playlist)
        }else{
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

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.playlist_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId){
            R.id.playlist_download_menu_item ->{
                playlistViewmodel.playlistData.value?.let {
                    playlistViewmodel.downloadPlaylist(it)
                    (requireActivity() as MainActivity).showSnacbar("Added to Downloads" , "View"){
                        findNavController().navigate(R.id.downloadListFragment)
                    }
                    playlistViewmodel.downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner)}
                true
            }
            R.id.add_song_menu_item ->{
                playlistViewmodel.playlistData.value?.let {
                    (requireActivity() as MainActivity).movetoAddSongFragment(it._id)
                }
                true
            }
           R.id.make_playlist_public_menu_item ->{
               playlistViewmodel.playlistData.value?.let {
                   playlistViewmodel.makePlaylistPublic(it._id!!).observe(viewLifecycleOwner , Observer { ispublic ->
                        isPlaylistPublic = ispublic
                       requireActivity().invalidateOptionsMenu()
                   })
               }

               true
           }
           R.id.playlist_edit_menu_item ->{
               activiateMultiSelectionMode()
               true
           }
           R.id.playlist_add_to_fav_menu_item -> {
               playlistViewmodel.playlistData.value?.let {
                   playlistViewmodel.addPlaylisttoFav(it._id!!).handleSingleDataObservation(viewLifecycleOwner) {
                           if (it) {
                               (requireActivity() as MainActivity).showSnacbar("Playlist added to Library")
                               isPlaylistInLibrary = true
                               requireActivity().invalidateOptionsMenu()
                           }
                       }
               }
               true
           }
           R.id.playlist_delete_menu_item ->{
               playlistViewmodel.playlistData.value?.let {
                   if(loadFromCache){
                       downloadViewmodel.removeDownloadedPlaylist(it._id!!)
                       (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads" , "Undo"){}
                       findNavController().popBackStack()
                   }
                   else{
                       playlistViewmodel.deletePlaylist(it._id!!).handleSingleDataObservation(viewLifecycleOwner){
                           (requireActivity() as MainActivity).showSnacbar("Playlist Deleted" , "Undo"){}
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
        super.onDestroyView()
    }
    


    override fun activiateMultiSelectionMode() {
//        playlistViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.MultiSelectionState())
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.MultiSelectionState())
        this.toControllerActivity().hideBottomView()
        selection_menu.visibility = View.VISIBLE
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
                        mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
//                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                    }

                    SongAdapter.ADD_TO_OPTION ->{
                        var songInfo = BaseModel(baseId = data._id, baseTittle = data.album, baseSubTittle = data.tittle, baseImagePath = data.thumbnailPath, baseDescription = data.mpdPath, baseListOfInfo = data.artists)
                        (requireActivity() as MainActivity).moveToAddtoFragment(listOf(songInfo), LibraryService.CONTENT_TYPE_SONG)
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        playlistViewmodel.downloadPlaylistSongs(listOf(data))
                        playlistViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner)
                        (requireActivity() as MainActivity).showSnacbar("Song added to download" , "View"){
                            findNavController().navigate(R.id.downloadListFragment)
                        }
                        deactivateMultiSelectionModel()
                    }

                    SongAdapter.SONG_RADIO_OPTION -> {
                        (requireActivity() as MainActivity).movetoSingleRadioStation(songId = data._id)
                    }

                    SongAdapter.ARTIST_RADIO_OPTION ->{
                        if(data.artists!!.size > 1){
                            MaterialDialog(requireContext()).show {
                                listItems(items = data.artists){dialog, index, text ->
                                    (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![index])
                                }
                            }
                        }
                        else (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![0])
                    }

                    SongAdapter.GO_TO_ARTIST_OPTION ->  (requireActivity() as MainActivity).moveToArtist(data.artists!![0])
                    SongAdapter.GO_TO_ALBUM_OPTION -> data.album?.let {
                        (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(it, false , false)
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
                    }
                    SongAdapter.RESUME_DOWNLOAD_OPTION ->{

                    }
                    else ->{
                        playlistViewmodel.downloadTracker.getCurrentDownloadItem(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))?.let {
                            Toast.makeText(context , "song not downloaeded yet" , Toast.LENGTH_SHORT).show()
                            return
                        }
                        playlistViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
                            var songUri = Uri.parse(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))
                            var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
                            if(isFailed){
                                Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                                return
                            }
                        }

                        playlistViewmodel.playlistData.value?.songs?.let{plsongs ->
                            if(loadFromCache){
                                downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
                                    var ids = it.map { download -> download.request.id }
                                    if(!ids.isNullOrEmpty()){
                                        var songs = plsongs.filter { song -> ids.contains(song._id) }
                                        var selectedSongPosition = songs.indexOf(data)
                                        Log.i("select index" , "${songs.size} $selectedSongPosition")
                                        mainActivityViewmodel.prepareAndPlaySongsBeta(songs , PlayerState.PlaylistState() , selectedSongPosition , false , loadFromCache)
                                    }else Toast.makeText(requireContext(), "No song downloaded" , Toast.LENGTH_LONG).show()
                                }
                            }
                            else {
                                mainActivityViewmodel.prepareAndPlaySongsBeta(plsongs , PlayerState.PlaylistState() , position , false , loadFromCache)
                            }
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
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
        mainActivityViewmodel.playlistQueue.value = songs
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
        selection_menu.animation = animation
        selection_menu.visibility = View.GONE
        (requireActivity() as MainActivity).showBottomViewBeta()
    }



}
