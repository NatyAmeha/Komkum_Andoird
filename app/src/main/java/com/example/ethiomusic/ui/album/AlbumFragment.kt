package com.example.ethiomusic.ui.album

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.example.ethiomusic.*
import com.example.ethiomusic.data.model.BaseModel

import com.example.ethiomusic.data.model.RecyclerViewHelper

import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.AlbumFragmentBinding
import com.example.ethiomusic.media.ServiceConnector
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.example.ethiomusic.ui.download.DownloadViewmodel
import com.example.ethiomusic.ui.download.DownloadViewmodelFactory
import com.example.ethiomusic.util.LibraryService
import com.example.ethiomusic.util.Resource
import com.example.ethiomusic.util.extensions.configureActionBar
import com.example.ethiomusic.util.extensions.handleError
import com.example.ethiomusic.util.extensions.handleSingleDataObservation
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.PlayerState
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import com.google.android.exoplayer2.offline.Download
import kotlinx.android.synthetic.main.album_fragment.*
import kotlinx.android.synthetic.main.multi_selection_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class AlbumFragment : Fragment(), IRecyclerViewInteractionListener<Song<String,String>> {


    @Inject
    lateinit var albumFactory: AlbumViewmodelFactory
    private val albumViewmodel: AlbumViewModel by viewModels { GenericSavedStateViewmmodelFactory(albumFactory, this) }

    @Inject
    lateinit var downloadFacotory : DownloadViewmodelFactory
    val downloadViewmodel: DownloadViewmodel by viewModels { GenericSavedStateViewmmodelFactory(downloadFacotory , this) }

    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()


    lateinit var binding: AlbumFragmentBinding
    lateinit var songAdapter: SongAdapter

    var albumId: String? = null
    var isAlbumInfavorite = false
    var isAlbumDownloaded = false
    var loadFromCache : Boolean = false
    var isNavigationFromDownload = false


    init {
        lifecycleScope.launch {
            whenCreated {
                arguments?.let {
                    albumId = it.getString("ALBUMID")
                    loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
                    isNavigationFromDownload = it.getBoolean("IS_NAV_FROM_DOWNLOAD")
                }
                if(isNavigationFromDownload and loadFromCache) albumViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.DownloadState())

                isAlbumInfavorite =  albumViewmodel.isAlbumInfavorite(albumId!!)
                isAlbumDownloaded = albumViewmodel.isAlbumDownloaded(albumId!!)
                requireActivity().invalidateOptionsMenu()
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
        binding = AlbumFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var info = RecyclerViewHelper("ALBUM" , albumViewmodel.recyclerviewStateManager , this  ,
            viewLifecycleOwner , albumViewmodel.downloadTracker)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listInfo = info
        binding.viewmodel = albumViewmodel

        binding.isFromCache = loadFromCache

        configureActionBar(binding.albumToolbar)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (albumViewmodel.recyclerviewStateManager.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            else  findNavController().popBackStack()
        }

        albumId?.let {
            albumViewmodel.getalbumB(it , loadFromCache)
                albumViewmodel.album.observe(viewLifecycleOwner , Observer{
                    var uri =  it.albumCoverPath!!.replace("localhost" , "192.168.43.166")
                    var url = URL(uri)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                            Palette.from(bitmap).generate {
                                it?.darkMutedSwatch?.let {
                                    var grDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000))
//                               binding.queueSongImageViewpager.setBackgroundResource(R.drawable.item_selected)
                                    binding.cToolbarLayout.background = grDrawable
                                    requireActivity().window.statusBarColor = it.rgb
                                }
                            }
                        }catch (ex : Exception){

                        }
                    }

                })

        }

        albumViewmodel.error.observe(viewLifecycleOwner, Observer { error ->
            error.handleError(view.context)
        })


        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
            var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    albumViewmodel.recyclerviewStateManager.selectedItem.value = it
                }
            }
        })

        select_all_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) albumViewmodel.recyclerviewStateManager._multiselectedItems.value = albumViewmodel.albumSongs
            else albumViewmodel.recyclerviewStateManager._multiselectedItems.value = emptyList()
        }

        fav_icon.setOnClickListener {
            var songIds = albumViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id!! }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds) as LiveData<Resource<Boolean>>)
                .handleSingleDataObservation(viewLifecycleOwner){
                if(it){
                    Toast.makeText(view.context, "songs added to favorite", Toast.LENGTH_SHORT).show()
                    deactivateMultiSelectionModel()
                }else deactivateMultiSelectionModel()
            }
        }
        add_icon.setOnClickListener {
            var data = albumViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song ->
                BaseModel(baseId = song._id , baseTittle = albumViewmodel.album.value!!._id , baseSubTittle = song.tittle ,  baseImagePath = song.thumbnailPath ,
                    baseDescription = song.mpdPath , baseListOfInfo = albumViewmodel.album.value!!.artists as List<String>) }
            deactivateMultiSelectionModel()
            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_SONG)
        }
        play_icon.setOnClickListener {
            var songIds = albumViewmodel.recyclerviewStateManager.multiselectedItems.value!!.map { song -> song._id!! }
            var filteredSongs =  albumViewmodel.albumSongs.filter { song -> songIds.contains(song._id) }
            mainActivityViewmodel.playlistQueue.value = filteredSongs
            mainActivityViewmodel.preparePlayer(ServiceConnector.STREAM_MEDIA_TYPE)
            deactivateMultiSelectionModel()
        }

        download_icon.setOnClickListener {
            albumViewmodel.recyclerviewStateManager.multiselectedItems.observe(viewLifecycleOwner , Observer { selectedItems ->
                if(selectedItems.isNotEmpty()){
                    albumViewmodel.downloadAlbumSongs(selectedItems)
                    albumViewmodel.album.value?.let {
                        (requireActivity() as MainActivity).showSnacbar("${selectedItems.size} songs added to download" , "View"){
                            findNavController().navigate(R.id.downloadListFragment)
                        }
                        albumViewmodel.downloadTracker.addDownloads(selectedItems , viewLifecycleOwner)
                        deactivateMultiSelectionModel()
                    }
                }
            })
        }

        delete_icon.setOnClickListener {
            albumViewmodel.recyclerviewStateManager.multiselectedItems.value?.let {
                if(loadFromCache){
                    var songIds = it.map { song -> song._id }
                    if(it.size == albumViewmodel.albumSongs.size){
                        downloadViewmodel.removeDownloadedAlbum(albumViewmodel.album.value!!._id)
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads")
                        findNavController().popBackStack()
                    }
                    else{
                        downloadViewmodel.deleteAlbumSongs(songIds , albumViewmodel.album.value!!._id)
                        albumViewmodel.albumSongs.removeAll(it)
                        deactivateMultiSelectionModel()
                        (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads")
                        (binding.albumSongRecyclerview.adapter as SongAdapter).submitList(albumViewmodel.albumSongs)
                    }

                    (requireActivity() as MainActivity).showSnacbar("Deleted from Downloads")

                }
            }
        }

        binding.albumShuffleBtn.setOnClickListener {
            prepareAndPlaySongs(albumViewmodel.albumSongs , null , true)
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isAlbumInfavorite) menu.removeItem(R.id.addto_fav_album_menu_item)
        else menu.removeItem(R.id.remove_fav_album_menu_item)

        if(isAlbumDownloaded) menu.removeItem(R.id.download_menu_item)
        else menu.removeItem(R.id.remove_download_menu_item)

        if(loadFromCache){
            menu.removeItem(R.id.go_to_artist_menu_item)
            menu.removeItem(R.id.addto_fav_album_menu_item)
            menu.removeItem(R.id.remove_fav_album_menu_item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.album_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addto_fav_album_menu_item -> {
                albumViewmodel.addToFav(albumId!!).observe(viewLifecycleOwner , Observer {
                    isAlbumInfavorite = it!!
                    (requireActivity() as MainActivity).showSnacbar("Album added to favorite")
                    requireActivity().invalidateOptionsMenu()
                })
                true
            }
            R.id.remove_fav_album_menu_item -> {
                albumViewmodel.removeFromFav(albumId!!).observe(viewLifecycleOwner , Observer {
                    isAlbumInfavorite = it
                    (requireActivity() as MainActivity).showSnacbar("Album removed from favorite")
                    requireActivity().invalidateOptionsMenu()
                })
                true
            }
            R.id.album_edit_menu_item -> {
                this.activiateMultiSelectionMode()
                true
            }
            R.id.download_menu_item ->{
                 albumViewmodel.downloadAlbum(albumViewmodel.album.value!!)
                albumViewmodel.album.value?.let {
                    (requireActivity() as MainActivity).showSnacbar("Added to Downloads" , "View"){
                        findNavController().navigate(R.id.downloadListFragment)
                    }
                    albumViewmodel.downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner)
                }

                (requireActivity() as MainActivity).handleNotification()
                isAlbumDownloaded = false
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.remove_download_menu_item ->{
                downloadViewmodel.removeDownloadedAlbum(albumId!!)
                (requireActivity() as MainActivity).showSnacbar("Removed from Downloads" , "Undo"){
                   Toast.makeText(requireContext() , "undone action pending" , Toast.LENGTH_SHORT).show()
                }
                isAlbumDownloaded = true
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.go_to_artist_menu_item ->{
                if(albumViewmodel.album.value!!.artists!!.size > 1){}
                else{
                    (requireActivity() as MainActivity).moveToArtist(albumViewmodel.album.value!!.artists!!.first())
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
        albumViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.MultiSelectionState())
        this.toControllerActivity().hideBottomView()
        selection_menu.visibility = View.VISIBLE
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        when(albumViewmodel.recyclerviewStateManager.state.value){
            is RecyclerviewState.MultiSelectionState ->{
                 albumViewmodel.recyclerviewStateManager.addOrRemoveItem(data)
            }
            is RecyclerviewState.SingleSelectionState ->{
                when(option){
                    SongAdapter.NO_OPTION ->{
                        mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs, PlayerState.PlaylistState() , position)
                        albumViewmodel.recyclerviewStateManager.selectedItem.value = data
                    }
                    SongAdapter.PLAY_OPTION ->{
                        mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
                        albumViewmodel.recyclerviewStateManager.selectedItem.value = data
                    }
                    SongAdapter.ADD_TO_OPTION ->{
                        var songInfo = BaseModel(baseId = data._id, baseTittle = data.album, baseSubTittle = data.tittle, baseImagePath = data.thumbnailPath, baseDescription = data.mpdPath, baseListOfInfo = data.artists)
                        (requireActivity() as MainActivity).moveToAddtoFragment(listOf(songInfo), LibraryService.CONTENT_TYPE_SONG)
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        albumViewmodel.downloadAlbumSongs(listOf(data))
                        (requireActivity() as MainActivity).showSnacbar("Song added to download")
                        albumViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner)
                        deactivateMultiSelectionModel()
                    }
                    SongAdapter.GO_TO_ARTIST_OPTION ->  (requireActivity() as MainActivity).moveToArtist(data.artists!![0])
                    SongAdapter.GO_TO_ALBUM_OPTION -> data.album?.let {
                        (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(it, false , false)
                    }
                }
            }
            is RecyclerviewState.DownloadState ->{
               albumViewmodel.downloadTracker.getCurrentDownloadItem(data.songFilePath!!.replace("localhost" , "192.168.43.166"))?.let {
                   Toast.makeText(context , "song not downloaeded yet" , Toast.LENGTH_SHORT).show()
                   return
               }
                albumViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
                    var songUri = Uri.parse(data.songFilePath!!.replace("localhost" , "192.168.43.166"))
                  var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
                    if(isFailed){
                        Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                prepareAndPlaySongs(albumViewmodel.albumSongs , position)
                albumViewmodel.recyclerviewStateManager.selectedItem.value = data
            }
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
        if(loadFromCache) albumViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.DownloadState())
        else albumViewmodel.recyclerviewStateManager.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        selection_menu.animation = animation
        selection_menu.visibility = View.GONE
        this.toControllerActivity().showBottomView()
    }

    override fun onSwiped(position: Int) {

    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {

    }

    fun setAnimation(view : View){
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_in)
        view.animation = animation
    }


}
