package com.zomatunes.zomatunes.ui.album

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.zomatunes.zomatunes.*
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.Chapter

import com.zomatunes.zomatunes.data.model.RecyclerViewHelper

import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.AlbumFragmentBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.ui.download.DownloadViewmodel
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.Resource
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.DownloadAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.extensions.toControllerActivity
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RadioSpan
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import com.google.android.exoplayer2.offline.Download
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.album_fragment.*
import kotlinx.android.synthetic.main.multi_selection_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class AlbumFragment : Fragment(), IRecyclerViewInteractionListener<Song<String,String>> {

    private val albumViewmodel: AlbumViewModel by viewModels()
    val downloadViewmodel: DownloadViewmodel by viewModels()
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()


    lateinit var binding: AlbumFragmentBinding
    lateinit var songAdapter: SongAdapter
    var albumId: String? = null
    var isAlbumInfavorite = false
    var isAlbumDownloaded = false
    var loadFromCache : Boolean = false
    var isNavigationFromDownload = false


    init {
        lifecycleScope.launchWhenCreated {
            whenCreated {
                arguments?.let {
                    albumId = it.getString("ALBUMID")
                    loadFromCache = it.getBoolean("LOAD_FROM_CACHE")
                    mainActivityViewmodel.isLoadFromCache = loadFromCache
                    isNavigationFromDownload = it.getBoolean("IS_NAV_FROM_DOWNLOAD")
                }
                if(isNavigationFromDownload and loadFromCache){
                    mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
                    mainActivityViewmodel.songrecyclerviewStateManeger._downloadingItems.value = albumViewmodel.downloadTracker.getAllCurrentlyDownloadedItems()
                }
                else mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())

                albumId?.let {
                    albumViewmodel.getalbumB(it , loadFromCache)
                }

                isAlbumInfavorite =  albumViewmodel.isAlbumInfavorite(albumId!!)
                isAlbumDownloaded = albumViewmodel.isAlbumDownloaded(albumId!!)

                requireActivity().invalidateOptionsMenu()

            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AlbumFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 -> binding.albumToolbar.title = ""
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.customPlaylistImageview.visibility = View.INVISIBLE
                    binding.albumToolbar.title = binding.albumNameTextview.text.toString()
                }
                verticalOffset > -appBarLayout.totalScrollRange -> binding.customPlaylistImageview.visibility = View.VISIBLE
            }
        })



        var info = RecyclerViewHelper("ALBUM" , mainActivityViewmodel.songrecyclerviewStateManeger , this  ,
            viewLifecycleOwner , albumViewmodel.downloadTracker)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.listInfo = info
        binding.viewmodel = albumViewmodel
        binding.isFromCache = loadFromCache

        configureActionBar(binding.albumToolbar)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (mainActivityViewmodel.songrecyclerviewStateManeger.state.value is RecyclerviewState.MultiSelectionState) deactivateMultiSelectionModel()
            else  findNavController().popBackStack()
        }

        mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
            var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            songId?.let {
                mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                    var a = it::class.qualifiedName
                    Log.i("typemediainfo" , "song")
                    when(a){
                        "com.example.com.zomatunes.zomatunes.data.model.Song"  ->{
                            Log.i("typemedia" , it.tittle)
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                    }
                }
            }
        })


        albumViewmodel.album.observe(viewLifecycleOwner , Observer{
            var albumInfo = "${it.genre} span ${it.songs?.size ?: 0} songs"
            var spanner = Spanner(albumInfo).span("span" , Spans.custom { RadioSpan(requireContext() , R.drawable.tab_item_not_active) })
            binding.albumArtistNameTextview.text = spanner
            var coverImage = it.albumCoverPath!!.replace("localhost" , AdapterDiffUtil.URL)
            binding.customPlaylistImageview.loadImage(listOf(coverImage))
            showAlbumBackground(coverImage)
        })

//        albumViewmodel.error.observe(viewLifecycleOwner, Observer { error ->
//            error.handleError(view.context)
//        })




        select_all_checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = albumViewmodel.albumSongs
            else mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
        }

        fav_icon.setOnClickListener {
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            (mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds) as LiveData<Resource<Boolean>>)
                .handleSingleDataObservation(viewLifecycleOwner){
                if(it){
                    Toast.makeText(view.context, "songs added to favorite", Toast.LENGTH_SHORT).show()
                    deactivateMultiSelectionModel()
                }else deactivateMultiSelectionModel()
            }
        }
        add_icon.setOnClickListener {
            var data = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song ->
                BaseModel(baseId = song._id , baseTittle = albumViewmodel.album.value!!._id , baseSubTittle = song.tittle ,  baseImagePath = song.thumbnailPath ,
                    baseDescription = song.mpdPath , baseListOfInfo = albumViewmodel.album.value!!.artists as List<String>) }
            deactivateMultiSelectionModel()
            (requireActivity() as MainActivity).moveToAddtoFragment(data , LibraryService.CONTENT_TYPE_SONG)
        }
        play_icon.setOnClickListener {
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            var filteredSongs =  albumViewmodel.albumSongs.filter { song -> songIds.contains(song._id) }
            mainActivityViewmodel.playlistQueue.value = filteredSongs
            mainActivityViewmodel.prepareAndPlaySongsBeta(filteredSongs , PlayerState.PlaylistState() , null , false , loadFromCache)
            deactivateMultiSelectionModel()
        }

        download_icon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.observe(viewLifecycleOwner , Observer { selectedItems ->
                if(selectedItems.isNotEmpty()){
                    albumViewmodel.downloadAlbumSongs(selectedItems)
                    albumViewmodel.album.value?.let {
                        (requireActivity() as MainActivity).showSnacbar("${selectedItems.size} songs added to download" , "View"){
                            var bundle = bundleOf("SELECTED_PAGE" to 0)
                            findNavController().navigate(R.id.downloadListFragment , bundle)
                        }
                        albumViewmodel.downloadTracker.addDownloads(selectedItems , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG )
                        deactivateMultiSelectionModel()
                    }
                }
            })
        }

        delete_icon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
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
            mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs , PlayerState.PlaylistState() , null ,  true)
        }


//        albumViewmodel.getError().observe(viewLifecycleOwner){}
//        albumViewmodel.error.observe(viewLifecycleOwner){error ->
//            error.handleError(requireContext()){
////                binding.albumErrorTextview?.visibility = View.VISIBLE
//                binding.albumLoadingProgressbar.isVisible = false
//            }
//        }

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
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
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
                    albumViewmodel.downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
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
                if(albumViewmodel.album.value!!.artists!!.size > 1){
                    MaterialDialog(requireContext()).show {
                        listItems(items = albumViewmodel.album.value?.artists){dialog, index, text ->
                            (requireActivity() as MainActivity).moveToArtist(albumViewmodel.album.value!!.artists!![index])
                        }
                    }
                }
                else (requireActivity() as MainActivity).moveToArtist(albumViewmodel.album.value!!.artists!!.first())
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
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.MultiSelectionState())
        this.toControllerActivity().hideBottomView()
        selection_menu.visibility = View.VISIBLE
    }

    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {
        when(mainActivityViewmodel.songrecyclerviewStateManeger.state.value){
            is RecyclerviewState.MultiSelectionState ->{
                mainActivityViewmodel.songrecyclerviewStateManeger.addOrRemoveItem(data)
            }
            is RecyclerviewState.SingleSelectionState ->{
                when(option){
                    SongAdapter.NO_OPTION ->{
                        mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs, PlayerState.PlaylistState() , position)
                        mainActivityViewmodel.playedFrom.value = "Playing from ${albumViewmodel.album.value?.name}(Album)"
                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                    }
                    SongAdapter.PLAY_OPTION ->{
                        if(loadFromCache) play(data , position)
                        else {
                            mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                        }

                    }
                    SongAdapter.ADD_TO_OPTION ->{
                        var songInfo = BaseModel(baseId = data._id, baseTittle = data.album, baseSubTittle = data.tittle, baseImagePath = data.thumbnailPath, baseDescription = data.mpdPath, baseListOfInfo = data.artists)
                        (requireActivity() as MainActivity).moveToAddtoFragment(listOf(songInfo), LibraryService.CONTENT_TYPE_SONG)
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        albumViewmodel.downloadAlbumSongs(listOf(data))
                        (requireActivity() as MainActivity).showSnacbar("Song added to download")
                        albumViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
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
                        Toast.makeText(requireContext() , "Cannnot remove songs from album" , Toast.LENGTH_LONG)
                    }
                    SongAdapter.RESUME_DOWNLOAD_OPTION ->{

                    }
                    else -> play(data , position)
                }


            }
        }

    }


    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    fun play(data : Song<String , String> , position : Int){
        albumViewmodel.downloadTracker.getCurrentDownloadItem(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))?.let {
            Toast.makeText(context , "song not downloaeded yet" , Toast.LENGTH_SHORT).show()
            return
        }
        albumViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
            var songUri = Uri.parse(data.mpdPath!!.replace("localhost" , AdapterDiffUtil.URL))
            var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
            if(isFailed){
                Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                return
            }
        }
        if(loadFromCache){
            downloadViewmodel.getDownloads(Download.STATE_COMPLETED).observe(viewLifecycleOwner){
                var ids = it.map { download -> download.request.id }
                if(!ids.isNullOrEmpty()){
                    var songs = albumViewmodel.albumSongs.filter { song -> ids.contains(song._id) }
                    mainActivityViewmodel.prepareAndPlaySongsBeta(songs , PlayerState.PlaylistState() , position , false , loadFromCache)
                }else Toast.makeText(requireContext(), "No song downloaded" , Toast.LENGTH_LONG).show()
            }
        }
        else {
            mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs , PlayerState.PlaylistState() , position , false , loadFromCache)
        }

        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
    }

    fun deactivateMultiSelectionModel() {
        if(loadFromCache) mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
        else mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        selection_menu.animation = animation
        selection_menu.visibility = View.GONE
        this.toControllerActivity().showBottomView()
    }

    fun setAnimation(view : View){
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_in)
        view.animation = animation
    }
    fun showAlbumBackground(imageUrl : String){
        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    Palette.from(it).generate {
                        it?.darkMutedSwatch?.let {
                            var grDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000))
                            var grDrawable2 = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000 , 0x00000000))
                            binding.albumContainer.background = grDrawable2
//                               binding.queueSongImageViewpager.setBackgroundResource(R.drawable.item_selected)
                            binding.cToolbarLayout.background = grDrawable
                            requireActivity().window.statusBarColor = it.rgb
                        }
                    }
                }

            }
            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        Picasso.get().load(imageUrl).into(target)
    }
}
