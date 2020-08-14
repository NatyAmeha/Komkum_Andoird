package com.example.ethiomusic.ui.playlist

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.*
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper

import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.PlaylistFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.ui.download.DownloadViewmodel
import com.example.ethiomusic.ui.download.DownloadViewmodelFactory
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleListDataObservation
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import kotlinx.android.synthetic.main.album_fragment.selection_menu
import kotlinx.android.synthetic.main.multi_selection_menu.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlaylistFragment : Fragment() , IRecyclerViewInteractionListener<Song<String,String>> {

    companion object{
       const val LOAD_ARTIST_COLLECTION = 0
        const val  LOAD_SYS_GENERATED_PLAYLIST = 1
        const val  LOAD_PLAYLIST = 2
        const val LOAD_FROM_PREV_REQUEST = 3
        const val LOAD_PLAYLIST_SONGS = 4
    }

    @Inject
    lateinit var downloadTracker : DownloadTracker

    @Inject 
    lateinit var playlistFactory : PlaylistViewModelFactory
    val playlistViewmodel: PlaylistViewModel by viewModels{ GenericSavedStateViewmmodelFactory(playlistFactory , this) }

    @Inject
    lateinit var downloadFacotory : DownloadViewmodelFactory
    val downloadViewmodel: DownloadViewmodel by viewModels { GenericSavedStateViewmmodelFactory(downloadFacotory , this) }

    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : PlaylistFragmentBinding

    var playlistDataType : Int? = null
    var loadFromCache : Boolean = false
    var isNavigationFromDownload = false
    lateinit var playlistInfo : Playlist<String>


    init {
        lifecycleScope.launch {
            whenCreated {
                arguments?.let {
                    playlistDataType = it.getInt("PLAYLIST_DATA_TYPE")
                    loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
                    isNavigationFromDownload = it.getBoolean("IS_NAV_FROM_DOWNLOAD")
                    requireActivity().invalidateOptionsMenu()

                    if(loadFromCache) playlistViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.DownloadState())
                }
            }
        }


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
        var application = requireActivity().application as EthioMusicApplication
        application.appComponent.inject(this)
    }
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PlaylistFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper("PLAYLIST" , playlistViewmodel.recyclerviewStateManager , this , viewLifecycleOwner)

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner , Observer{metadata ->
            var songId : String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    playlistViewmodel.recyclerviewStateManager.selectedItem.value = it
                }
            }
        })

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = playlistViewmodel
        binding.listInfo = info
        binding.isFromCache = loadFromCache

        configureActionBar(binding.playlistToolbar)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (playlistViewmodel.recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            else  findNavController().popBackStack()
        }

        when(playlistDataType){
            LOAD_ARTIST_COLLECTION ->{ playlistViewmodel.recommendedSongByArtist() }

            LOAD_PLAYLIST ->{ arguments?.getString("PLAYLIST_ID")?.let {
                playlistViewmodel.getPlaylist(it , loadFromCache)
            }}
            LOAD_FROM_PREV_REQUEST -> {
                var playlist = arguments?.getParcelable<Playlist<Song<String,String>>>("PLAYLIST_DATA")
                playlistViewmodel.playlistData.value = playlist
            }
            LOAD_PLAYLIST_SONGS ->{
                playlistInfo = arguments?.getParcelable("PLAYLIST_INFO")!!
                playlistViewmodel.getPlaylistSongs(playlistInfo.songs!!).handleListDataObservation(viewLifecycleOwner){songs ->
                    var sortedSongs =  songs.sortedBy { song -> playlistInfo.songs!!.indexOf(song._id) }
                    var playlist = Playlist(name = playlistInfo.name , creatorId = playlistInfo.creatorId ,  songs = sortedSongs)
                    playlistViewmodel.playlistData.value = playlist
                }
            }

        }

        //multiselection menu buttons

        fav_icon.setOnClickListener {
            var songIds = playlistViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds))
            Toast.makeText(view.context, "songs added to favorite", Toast.LENGTH_SHORT).show()
            deactivateMultiSelectionModel()
        }

        add_icon.setOnClickListener {
            var data = playlistViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song ->
                BaseModel(baseId = song._id  , baseSubTittle = song.tittle ,  baseImagePath = song.thumbnailPath ,
                    baseDescription = song.mpdPath , baseListOfInfo = song.artists as List<String>) }
            deactivateMultiSelectionModel()
            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_SONG)
        }

        play_icon.setOnClickListener {
            var songIds = playlistViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id!! }
            playlistViewmodel.playlistData.value?.let {
                var songs = it.songs?.filter { song -> songIds.contains(song._id) }
                mainActivityViewmodel.playlistQueue.value = songs
                mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE)
                deactivateMultiSelectionModel()
            }
        }

        download_icon.setOnClickListener {
            playlistViewmodel.recyclerviewStateManager.multiselectedItems.value?.let { selectedItems ->
                if(selectedItems.isNotEmpty()){
                    playlistViewmodel.downloadPlaylistSongs(selectedItems)
                    playlistViewmodel.playlistData.value?.let {
                        (requireActivity() as MainActivity).showSnacbar("Added to Downloads" , "View"){
                            findNavController().navigate(R.id.downloadListFragment)
                        }
                        downloadTracker.addDownloads(selectedItems , viewLifecycleOwner)
                        deactivateMultiSelectionModel()
                    }
                }
            }
        }

//        delete_icon.isEnabled = false
        delete_icon.setOnClickListener {
            playlistViewmodel.recyclerviewStateManager.multiselectedItems.value?.let {
                if(loadFromCache){
                    var songIds = it.map { song -> song._id }
                    if(it.size == playlistViewmodel.playlistData.value!!.songs!!.size){
                        downloadViewmodel.removeDownloadedPlaylist(playlistViewmodel.playlistData.value!!._id!!)
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads" , "Undo"){}
                        findNavController().popBackStack()
                    }
                    else{
                        downloadViewmodel.removePlaylistSongs(songIds , playlistViewmodel.playlistData.value!!._id!!)
                       var updatedSongList =  playlistViewmodel.playlistData.value!!.songs?.toMutableList()
                        updatedSongList?.removeAll(it)
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
                    var songs = it.map { song -> song._id }
                    var playlist = Playlist(_id = playlistViewmodel.playlistData.value!!._id , songs = songs)

                    var modifiedSong =   playlistViewmodel.playlistData.value!!.songs!!.toMutableList()
                    modifiedSong.removeAll { song -> songs.contains(song._id) }
                    playlistViewmodel.playlistData.value!!.songs = modifiedSong

                    (binding.playlistSongRecyclerview.adapter as SongAdapter).submitList(modifiedSong)
                    deactivateMultiSelectionModel()
                    (requireActivity() as MainActivity).showSnacbar("Song removed from playlist")
                    findNavController().popBackStack()
                    playlistViewmodel.removePlaylistSongs(playlist).handleSingleDataObservation(viewLifecycleOwner){
                        if(it)
                        else Toast.makeText(context, "error occured", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        select_all_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) playlistViewmodel.recyclerviewStateManager._multiselectedItems.value = playlistViewmodel.playlistData.value?.songs
            else playlistViewmodel.recyclerviewStateManager._multiselectedItems.value = emptyList()
        }

        binding.addSongFab.setOnClickListener {
            playlistViewmodel.playlistData.value?.let {
                (requireActivity() as MainActivity).movetoAddSongFragment(it._id)
            }
        }

        binding.shufflePlaylistBtn.setOnClickListener{
            playlistViewmodel.playlistData.value?.songs?.let {
                prepareAndPlaySongs(it , null , true)
            }

        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(loadFromCache){
            menu.removeItem(R.id.playlist_download_menu_item)
            menu.removeItem(R.id.add_song_menu_item)
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
                    downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner)}
                true
            }
            R.id.add_song_menu_item ->{
                playlistViewmodel.playlistData.value?.let {
                    (requireActivity() as MainActivity).movetoAddSongFragment(it._id)
                }
                true
            }
           R.id.playlist_edit_menu_item ->{
               activiateMultiSelectionMode()
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
        playlistViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.MultiSelectionState())
        this.toControllerActivity().hideBottomView()
        selection_menu.visibility = View.VISIBLE
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        when (playlistViewmodel.recyclerviewStateManager.state.value) {
            is RecyclerviewState.MultiSelectionState -> {
                var size =  playlistViewmodel.recyclerviewStateManager.addOrRemoveItem(data)
            }
            is RecyclerviewState.SingleSelectionState -> {
                when(option){
                    SongAdapter.NO_OPTION ->{
                        playlistViewmodel.playlistData.value?.songs?.let {
                            mainActivityViewmodel.prepareAndPlaySongsBeta(it, PlayerState.PlaylistState() , position , false , loadFromCache)
                            playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                        }
                    }
                    SongAdapter.PLAY_OPTION ->{
                        mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                    }

                    SongAdapter.ADD_TO_OPTION ->{
                        var songInfo = BaseModel(baseId = data._id, baseTittle = data.album, baseSubTittle = data.tittle, baseImagePath = data.thumbnailPath, baseDescription = data.mpdPath, baseListOfInfo = data.artists)
                        (requireActivity() as MainActivity).moveToAddtoFragment(listOf(songInfo), LibraryService.CONTENT_TYPE_SONG)
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        playlistViewmodel.downloadPlaylistSongs(listOf(data))
                        downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner)
                        (requireActivity() as MainActivity).showSnacbar("Song added to download")
                        deactivateMultiSelectionModel()
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
                        downloadTracker.getCurrentDownloadItem(data.songFilePath!!.replace("localhost" , "192.168.43.166"))?.let {
                            Toast.makeText(context , "song not downloaeded yet" , Toast.LENGTH_SHORT).show()
                            return
                        }
                        playlistViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
                            var songUri = Uri.parse(data.songFilePath!!.replace("localhost" , "192.168.43.166"))
                            var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
                            if(isFailed){
                                Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                        playlistViewmodel.playlistData.value?.songs?.let { prepareAndPlaySongs(it, position) }
                        playlistViewmodel.recyclerviewStateManager.selectedItem.value = data
                    }
                }


            }
        }
    }

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {
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
        if(loadFromCache) mainActivityViewmodel.preparePlayer(ServiceConnector.DOWNLOADED_MEDIA_TYPE , isShuffled)
        else mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE , isShuffled)

        mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
        if (position != null)  mainActivityViewmodel.play(position.toLong())
        else  mainActivityViewmodel.play()
    }


    fun deactivateMultiSelectionModel() {
        playlistViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        selection_menu.animation = animation
        selection_menu.visibility = View.GONE
        this.toControllerActivity().showBottomView()
    }



}
