package com.komkum.komkum.ui.album

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
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.*
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.Chapter

import com.komkum.komkum.data.model.RecyclerViewHelper

import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.AlbumFragmentBinding
import com.komkum.komkum.media.ServiceConnector
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RadioSpan
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.google.android.exoplayer2.offline.Download
import com.google.android.material.appbar.AppBarLayout
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.util.extensions.*
import dagger.hilt.android.AndroidEntryPoint

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

    var isPlayerPrepared = false
    var isErrorOccure = false   // house keeping boolean value when there is error in album


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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AlbumFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var preference = PreferenceHelper.getInstance(requireContext())
        preference[AccountState.IS_REDIRECTION] = true

        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            when {
                verticalOffset == 0 -> {
                    binding.albumToolbar.title = ""
//                    binding.albumContainer.setBackgroundColor(resources.getColor(R.color.light_secondaryDarkColor))

                }
                verticalOffset == -appBarLayout.totalScrollRange -> {
                    binding.customPlaylistImageview.visibility = View.INVISIBLE
                    binding.albumToolbar.title = binding.albumNameTextview.text.toString()
//                    binding.albumContainer.background = resources.getDrawable(R.drawable.item_selected)

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
            else  findNavController().navigateUp()
        }




        albumViewmodel.album.observe(viewLifecycleOwner , Observer{
            var albumInfo = "${it.artistsName?.joinToString(", ") ?: "artists name"} span ${it.songs?.size ?: 0} songs"
            var spanner = Spanner(albumInfo).span("span" , Spans.custom { RadioSpan(requireContext() , R.drawable.tab_item_not_active , resources.getInteger(R.integer.bullet_margin)) })
            binding.albumArtistNameTextview.text = spanner
            var coverImage = it.albumCoverPath!!.replace("localhost" , AdapterDiffUtil.URL)
            binding.customPlaylistImageview.loadImage(listOf(coverImage))
            showAlbumBackground(coverImage)

            mainActivityViewmodel.nowPlaying.observe(viewLifecycleOwner, Observer { metadata ->
                var songId: String? = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                songId?.let {
                    mainActivityViewmodel.getSelectedSongInfo(it)?.let {
                        var a = it::class.qualifiedName
                        when(a){
                            "com.komkum.komkum.data.model.Song"  ->{
                                mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = it as Song<String, String>}
                        }
                    }
                }
            })

        })

//        albumViewmodel.error.observe(viewLifecycleOwner, Observer { error ->
//            error.handleError(view.context)
//        })




        binding.selectionMenu.selectAllCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = albumViewmodel.albumSongs
            else mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
        }

        binding.selectionMenu.favIcon.setOnClickListener {
            binding.albumLoadingProgressbar.isVisible = true
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            mainActivityViewmodel.addToFavoriteList(LibraryService.CONTENT_TYPE_SONG , songIds).observe(viewLifecycleOwner){
                binding.albumLoadingProgressbar.isVisible = false
                if(it == true) Toast.makeText(view.context, getString(R.string.song_added_to_favorite), Toast.LENGTH_SHORT).show()
            }
            deactivateMultiSelectionModel()

        }
        binding.selectionMenu.addIcon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                var songIds = it.map { song -> song._id}
                var bundle = bundleOf("SONG_IDS" to songIds , "DATA_TYPE" to LibraryService.CONTENT_TYPE_SONG , "SONG_LISTS" to it.toMutableList())
                findNavController().navigate(R.id.addToFragment , bundle)
            }
            deactivateMultiSelectionModel()
        }
        binding.selectionMenu.playIcon.setOnClickListener {
            var songIds = mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value!!.map { song -> song._id!! }
            var filteredSongs =  albumViewmodel.albumSongs.filter { song -> songIds.contains(song._id) }
            mainActivityViewmodel.prepareAndPlaySongsBeta(filteredSongs , PlayerState.PlaylistState() , null , false , loadFromCache)
            mainActivityViewmodel.playedFrom.value = "${albumViewmodel.album.value?.name} \n Album"
            deactivateMultiSelectionModel()

        }

        binding.selectionMenu.downloadIcon.setOnClickListener {
            var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
            if(isSubscriptionValid){
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
            else{
                deactivateMultiSelectionModel()
                findNavController().navigate(R.id.subscriptionFragment2)
            }

        }

        binding.selectionMenu.deleteIcon.setOnClickListener {
            mainActivityViewmodel.songrecyclerviewStateManeger.multiselectedItems.value?.let {
                if(loadFromCache){
                    var songIds = it.map { song -> song._id }
                    if(it.size == albumViewmodel.albumSongs.size){
                        downloadViewmodel.removeDownloadedAlbum(albumViewmodel.album.value!!._id)
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download))
                        findNavController().popBackStack()
                    }
                    else{
                        downloadViewmodel.deleteAlbumSongs(songIds , albumViewmodel.album.value!!._id)
                        albumViewmodel.albumSongs.removeAll(it)
                        deactivateMultiSelectionModel()
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download))
                        (binding.albumSongRecyclerview.adapter as SongAdapter).submitList(albumViewmodel.albumSongs)
                    }

                    (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download))

                }
            }
        }

        binding.albumShuffleBtn.setOnClickListener {
            mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs , PlayerState.PlaylistState() , null ,  true)
            mainActivityViewmodel.playedFrom.value = "${albumViewmodel.album.value?.name} | Album"
        }


        albumViewmodel.getError().observe(viewLifecycleOwner){}
        albumViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.albumLoadingProgressbar.isVisible = false
            Toast.makeText(requireContext() , error , Toast.LENGTH_SHORT).show()
            error?.handleError(requireContext() , onUnAuthorizedError = {albumViewmodel.removeOldErrors()}){
                if(!loadFromCache){
                    binding.albumErrorTextview.visibility = View.VISIBLE
                    isErrorOccure = true
                    requireActivity().invalidateOptionsMenu()
                }
            }
        }

        mainActivityViewmodel.getError().observe(viewLifecycleOwner){}
        mainActivityViewmodel.error.observe(viewLifecycleOwner){error ->
            error?.handleError(requireContext() , onUnAuthorizedError = { mainActivityViewmodel.removeOldErrors()}){
                Toast.makeText(requireContext() , it , Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isErrorOccure){
            menu.clear()
            return
        }
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
                binding.albumLoadingProgressbar.isVisible = true
                albumViewmodel.addToFav(albumId!!).observe(viewLifecycleOwner , Observer {
                    binding.albumLoadingProgressbar.isVisible = false
                    isAlbumInfavorite = it ?: false
                    (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_library))
                    requireActivity().invalidateOptionsMenu()
                })
                true
            }
            R.id.album_share_menu_item ->{
                var albumName = albumViewmodel.album.value?.name ?: ""
                var albumLink = "https://komkum.com/album/${albumId}"
                requireActivity().showShareMenu(albumName, "Listen $albumName album on Komkum.\n" +
                        "$albumLink")
                true
            }
            R.id.remove_fav_album_menu_item -> {
                binding.albumLoadingProgressbar.isVisible = true
                albumViewmodel.removeFromFav(albumId!!).observe(viewLifecycleOwner , Observer {
                    binding.albumLoadingProgressbar.isVisible = false
                    isAlbumInfavorite = it
                    (requireActivity() as MainActivity).showSnacbar(getString(R.string.removed_from_library))
                    requireActivity().invalidateOptionsMenu()
                })
                true
            }
            R.id.album_edit_menu_item -> {
                mainActivityViewmodel.songrecyclerviewStateManeger._multiselectedItems.value = emptyList()
                this.activiateMultiSelectionMode()
                true
            }
            R.id.download_menu_item ->{

                var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                if(isSubscriptionValid){
                    albumViewmodel.downloadAlbum(albumViewmodel.album.value!!)
                    albumViewmodel.album.value?.let {
                        (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_downloads) , "View"){
                            findNavController().navigate(R.id.downloadListFragment)
                        }
                        albumViewmodel.downloadTracker.addDownloads(it.songs!! , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                        isAlbumDownloaded = false
                        requireActivity().invalidateOptionsMenu()
                    }
                }
                else findNavController().navigate(R.id.subscriptionFragment2)

                true
            }
            R.id.remove_download_menu_item ->{
                downloadViewmodel.removeDownloadedAlbum(albumId!!)
                (requireActivity() as MainActivity).showSnacbar(getString(R.string.deleted_from_download) , "Undo"){}
                isAlbumDownloaded = true
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.go_to_artist_menu_item ->{
                if(albumViewmodel.album.value?.artists!!.size > 1){
                    albumViewmodel.album.value?.artistsName?.showListDialog(requireContext() , getString(R.string.choose_artist)){ index, value ->
                        albumViewmodel.album.value?.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[index]) }
                    }
                }
                else albumViewmodel.album.value?.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mainActivityViewmodel.isAudioPrepared = false
        binding.albumSongRecyclerview.adapter = null
        super.onDestroyView()
    }





    override fun activiateMultiSelectionMode() {
        mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.MultiSelectionState())
        (requireActivity() as MainActivity).hideBottomView()
        binding.selectionMenu.root.visibility= View.VISIBLE
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
                        mainActivityViewmodel.playedFrom.value = "${albumViewmodel.album.value?.name} | Album"
                        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                    }
                    SongAdapter.PLAY_OPTION ->{
                        if(loadFromCache) play(data , position)
                        else {
                            mainActivityViewmodel.prepareAndPlaySongsBeta(listOf(data) , PlayerState.PlaylistState() , null)
                            mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
                            mainActivityViewmodel.playedFrom.value = "${albumViewmodel.album.value?.name} | Album"
                        }

                    }
                    SongAdapter.ADD_TO_OPTION ->{
                        (requireActivity() as MainActivity).moveToAddtoFragment(mutableListOf(data._id), LibraryService.CONTENT_TYPE_SONG , mutableListOf(data))
                    }
                    SongAdapter.DOWNLOAD_OPTION ->{
                        var isSubscriptionValid = mainActivityViewmodel.userRepo.userManager.hasValidSubscription()
                        if(isSubscriptionValid){
                            albumViewmodel.downloadAlbumSongs(listOf(data))
                            albumViewmodel.downloadTracker.addDownloads(listOf(data) , viewLifecycleOwner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
                            (requireActivity() as MainActivity).showSnacbar(getString(R.string.added_to_downloads) , "view"){
                                findNavController().navigate(R.id.downloadListFragment)
                            }
                            deactivateMultiSelectionModel()
                        }
                        else findNavController().navigate(R.id.subscriptionFragment2)
                    }

                    SongAdapter.SONG_RADIO_OPTION -> {
                        (requireActivity() as MainActivity).movetoSingleRadioStation(songId = data._id)
                    }

                    SongAdapter.ARTIST_RADIO_OPTION ->{
                        if(data.artists!!.size > 1){
                            MaterialDialog(requireContext()).show {
                                var items = listItems(items = data.artists){dialog, index, text ->
                                    (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![index])
                                }
                            }
                        }
                        else (requireActivity() as MainActivity).movetoSingleRadioStation(artistId = data.artists!![0])
                    }

                    SongAdapter.GO_TO_ARTIST_OPTION -> {
                        if(data.artists!!.size > 1){
                            data.artistsName?.showListDialog(requireContext() , getString(R.string.choose_artist)){index, value ->
                                (requireActivity() as MainActivity).moveToArtist(data.artists!![index])
                            }
                        }
                        else data.artists?.let { (requireActivity() as MainActivity).moveToArtist(it[0]) }
                    }
                    SongAdapter.GO_TO_ALBUM_OPTION -> {
                        if(data.album != null){
                            (requireActivity() as MainActivity).movefrombaseFragmentToAlbumFragment(data.album!!, false , false)
                        }
                        else Toast.makeText(requireContext() , "${data.album} This song is single" , Toast.LENGTH_SHORT).show()
                    }

                    SongAdapter.SONG_INFO_OPTION ->{
                        mainActivityViewmodel.showSongOption(data)
                    }
                }
            }
            is RecyclerviewState.DownloadState ->{
                when(option){
                    SongAdapter.REMOVE_DOWNLOAD_OPTION ->{
                        Toast.makeText(requireContext() , "Cannnot remove songs from album" , Toast.LENGTH_LONG).show()
                    }
                    else -> play(data , position)
                }


            }
        }

    }


    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}

    fun play(data : Song<String , String> , position : Int){
        albumViewmodel.downloadTracker.getCurrentDownloadItem(data._id)?.let {
            Toast.makeText(context , getString(R.string.download_not_completed) , Toast.LENGTH_SHORT).show()
            return
        }
        albumViewmodel.downloadTracker.getFailedDownloads().value?.let { downloads ->
            var songUri = Uri.parse(data.mpdPath!!)
            var isFailed = downloads.map { download -> download.request.uri }.contains(songUri)
            if(isFailed){
                Toast.makeText(context , "Download Failed " , Toast.LENGTH_SHORT).show()
                return
            }
        }

        mainActivityViewmodel.prepareAndPlaySongsBeta(albumViewmodel.albumSongs , PlayerState.PlaylistState() , position , false , loadFromCache)
        mainActivityViewmodel.songrecyclerviewStateManeger.selectedItem.value = data
        mainActivityViewmodel.playedFrom.value = "${albumViewmodel.album.value?.name} | Album"
    }

    fun deactivateMultiSelectionModel() {
        if(loadFromCache) mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.DownloadState())
        else mainActivityViewmodel.songrecyclerviewStateManeger.changeState(RecyclerviewState.SingleSelectionState())
        var animation = android.view.animation.AnimationUtils.loadAnimation(requireContext() , android.R.anim.fade_out)
        binding.selectionMenu.root.animation = animation
        binding.selectionMenu.root.visibility = View.GONE
        (requireActivity() as MainActivity).showBottomViewBeta()
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

//                               binding.queueSongImageViewpager.setBackgroundResource(R.drawable.item_selected)
                            binding.appBar.background = grDrawable
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                requireActivity().window.statusBarColor = it.rgb
                            }
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
