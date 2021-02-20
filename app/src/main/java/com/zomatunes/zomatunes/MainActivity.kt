package com.zomatunes.zomatunes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.ActivityMainBinding
import com.zomatunes.zomatunes.media.ServiceConnector
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.account.accountservice.PaymentManager
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.ui.player.PlayerFragment
import com.zomatunes.zomatunes.ui.player.QueueListAdapter
import com.zomatunes.zomatunes.util.LibraryService
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.MenuAdapter
import com.zomatunes.zomatunes.util.extensions.*
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.ItemTouchHelperCallback
import com.zomatunes.zomatunes.util.viewhelper.PlayerState
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject


class MainActivity : ControllerActivity() , IMainActivity , IRecyclerViewInteractionListener<Song<String, String>> {

    val mainViewmodel : MainActivityViewmodel by viewModels()

    @Inject
    lateinit var downloadTracker: DownloadTracker

    lateinit var binding: ActivityMainBinding
    lateinit var queueAdapter :  SongAdapter


    var selectedMediaId : String? = null
    var selectedSongIndex : Int = 0
    var playerState : Int = -1
    var isPlayerViewVisible = false

    var playerTitle : String? = null
    var playerSubtittle : String? = null
    var imagelist = listOf<String>()
    var openPlayerFromNotification = false

//    val bottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat> by lazy {
//        var player = player_bsheet as LinearLayoutCompat
//         BottomSheetBehavior.from(player)
//    }

    var isBottomsheetClosed = true





    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppthemeDark)
//        gettoken()
        super.onCreate(savedInstanceState)

        if(isValidUser(this)){
           binding =  DataBindingUtil.setContentView(this , R.layout.activity_main)
            viewContainer = binding.mainactivityContainer



            var navController = findNavController(R.id.nav_host_fragment)
            binding.bottomNavView.setupWithNavController(navController)
            binding.lifecycleOwner = this
            binding.viewmodel = mainViewmodel

            // connect to payback service
            mainViewmodel.connectToService()


            // register fcm to token to receive push notification
            var pref = PreferenceHelper.getInstance(this)
            var isTokenNew = pref.get(PreferenceHelper.IS_NEW_FCM_TOKEN , false)
            if(isTokenNew) {
                var token = pref.get(PreferenceHelper.FCM_TOKEN , "")
                if(token.isNotBlank()){
                    mainViewmodel.updateFcmToken(token).observe(this , Observer {
                        it.data?.let {
                            if(it) pref[PreferenceHelper.IS_NEW_FCM_TOKEN] = false
                        }
                    })
                }
            }

            intent.getBooleanExtra("OPENPLAYER" , false).let { value ->
                if(value){
                    playerState =  intent.getIntExtra("PLAYBACK_STATE" , -1)
                    openPlayerFromNotification = true
                    when(playerState){
                        1 ->  mainViewmodel.playerState.value = PlayerState.PlaylistState()
                        2 ->  mainViewmodel.playerState.value = PlayerState.AudiobookState()
                        3 ->  mainViewmodel.playerState.value = PlayerState.RadioState()
                    }

                    mainViewmodel.queueitem.observe(this , Observer { queue ->
                        queue?.let {
                            mainViewmodel.playlistQueue.value =  it.toSongList()
                        }
                    })
                }
            }




            mainViewmodel.nowPlaying.observe(this, Observer { metadata ->
                playerTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//                if(playerTitle.isNullOrEmpty()) hideplayer()


                playerSubtittle = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

                metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let { index ->
                    mainViewmodel.playlistSelectedIndex.value = index.toInt()
//                    selectedSongIndex = index.toInt()
                }

            })



//






//            // show player when opened from notification
//            intent.getBooleanExtra("OPENPLAYER" , false).let { value ->
//                if(value){
//                    playerState =  intent.getInt("PLAYBACK_STATE" , -1)
//                    when(playerState){
//                        1 ->  mainActivityViewmodel.playerState.value = PlayerState.PlaylistState()
//                        2 ->  mainActivityViewmodel.playerState.value = PlayerState.AudiobookState()
//                        3 ->  mainActivityViewmodel.playerState.value = PlayerState.RadioState()
//                    }
//
//                    mainActivityViewmodel.queueitem.observe(viewLifecycleOwner , Observer { queue ->
//                        queue?.let {
//                            mainActivityViewmodel.playlistQueue.value =  it.toSongList()
//                            this.toControllerActivity().hideBottomView()
//                        }
//                    })
//                }
//            }



            mainViewmodel.error.observe(this , Observer {
                Log.i("mainerror" , it)
                Toast.makeText(baseContext , it , Toast.LENGTH_LONG).show()
            })

            binding.playerCard.setOnClickListener {
                hideBottomView()
                var extra = FragmentNavigatorExtras(bsheet_imageView to "imageView")
               findNavController(R.id.nav_host_fragment).navigate(R.id.playerFragment , null , null , extra)
            }


            mainViewmodel.playbackDuration.observe(this , Observer { duration ->
                binding.bottonsheetSongProgressbar.max  = duration.toInt()
            })

            mainViewmodel.playbackPosition.observe(this , Observer { position ->
                binding.bottonsheetSongProgressbar.progress  = position.toInt()
            })

            // handle navigation from payment activity
            var yenepayPaymentFor = intent.getIntExtra("PAYMENT_FOR" , -1)
            when(yenepayPaymentFor){
                PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE ->{
                    var bookId = intent.getStringExtra("BOOK_ID")
                    var bundle = bundleOf("BOOK_ID" to bookId , "LOAD_FROM_CACHE" to false , "FROM_PAYMENT" to true )
                    findNavController(R.id.nav_host_fragment).navigate(R.id.audiobookSellFragment , bundle)
                }
                PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE -> {
                    var bookId = intent.getStringExtra("BOOK_ID")
                    var bundle = bundleOf("BOOK_ID" to bookId , "LOAD_FROM_CACHE" to false  , "FROM_PAYMENT" to true )
                    findNavController(R.id.nav_host_fragment).navigate(R.id.EBookSellFragment , bundle)
                }
            }

//            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                    if(slideOffset > 0.5f){
//                        player_toolbar.visibility = View.VISIBLE
//                        player_card.visibility = View.GONE
//                    }
//                    else{
//                        player_toolbar.visibility = View.GONE
//                        player_card.visibility = View.VISIBLE
//                    }
//                }
//
//                override fun onStateChanged(bottomSheet: View, newState: Int) {
//                    when(newState){
//                        BottomSheetBehavior.STATE_COLLAPSED -> isBottomsheetClosed = true
//                        BottomSheetBehavior.STATE_EXPANDED -> isBottomsheetClosed = false
//                    }
//               }
//            })


        }
        else{
            sendIntent(OnboardingActivity::class.java)
        }
    }




    fun showBottomViewBeta(){
        mainViewmodel.playlistQueue.value?.let {
            if(it.isNullOrEmpty()){
                player_card.visibility = View.GONE
            }
            else player_card.visibility = View.VISIBLE
        }
        bottom_nav_view.visibility = View.VISIBLE
    }

    fun showPlayer(){
        binding.playerCard.visibility = View.VISIBLE
    }


    fun handleNotification(){
        mainViewmodel.appDb.downloadDao.getDownloads().observe(this , Observer{downloadList ->
            if(downloadList.isNotEmpty()){
                mainViewmodel.handleDownloadNotification(this , this , downloadList.last())
            }

        })
    }

//    fun handleBottomsheet(){
//        if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//        else bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }
//
//    fun closeBottomSheet(){
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//    }


    fun showSnacbar(message : String , commandString: String? = null , action : (() -> Unit)? = null){
        Snackbar.make(binding.mainactivityContainer , message , Snackbar.LENGTH_LONG)
            .setAction(commandString){
                if (action != null)  action()
            }
            .show()
    }


    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {
        Log.i("activity called" , "called")
        var newQueueList = mainViewmodel.playlistQueue.value!!.toMutableList()
        var a = newQueueList.removeAt(position)
        mainViewmodel.playlistQueue.value = newQueueList
        mainViewmodel.removeQueueItem(position)
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        mainViewmodel.playlistQueue.value?.toMutableList()?.let{
            var item =  it.removeAt(prevPosition)
            it.add(newPosition , item)
            mainViewmodel.playlistQueue.value = it
            mainViewmodel.moveQueueItem(prevPosition , newPosition)
        }
    }


    override fun movefrombaseFragmentToAlbumFragment(albumId : String , loadFromCache : Boolean , isNavigationFromDownload : Boolean) {
        var bundle = bundleOf("ALBUMID" to albumId , "LOAD_FROM_CACHE" to loadFromCache , "IS_NAV_FROM_DOWNLOAD" to isNavigationFromDownload)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment , bundle)
    }

    override fun moveToAlbumListFragment(toolbarTitle : String, dataType : Int? , albumList : List<Album<String , String>>?) {
        var bundle = bundleOf("TOOLBAR_TITTLE" to toolbarTitle , "DATA_TYPE" to dataType , "ALBUM_LIST" to albumList)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumListFragment , bundle)
    }

    override fun movetoSongListFragment(songlistType: Int? , songList : List<Song<String, String>>?) {
        var bundle = bundleOf("SONG_LIST_TYPE" to songlistType , "SONGLIST" to songList)
        findNavController(R.id.nav_host_fragment).navigate(R.id.songListFragment , bundle)
    }

    override fun moveToArtist(artistId: String) {
        var bundle = bundleOf("ARTISTID" to artistId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistFragment , bundle)
    }

    override fun moveToArtistList(toolbarTitle: String , dataType: Int? , artistList : List<Artist<String , String>>? ) {
        var bundle = bundleOf("TOOLBAR_TITLE" to toolbarTitle , "DATA_TYPE" to dataType , "ARTIST_LIST" to artistList)
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistListFragment , bundle)
    }

    override fun movetoArtistBio(metadata: ArtistMetaData) {
        var bundle = bundleOf("ARTIST_METADATA" to metadata)
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistInfoFragment , bundle)
    }

    override fun movefromArtisttoAlbum(albumId: String) {
        var bundle = bundleOf("ALBUMID" to albumId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment , bundle)
    }

    override fun moveFromAlbumListtoAlbumFragment(albumId: String) {
        var bundle = bundleOf("ALBUMID" to albumId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment , bundle)
    }

    override fun movetoPlaylist(playlistDataType: Int, playlistId: String? , playlistData : Playlist<Song<String,String>>? , loadFromCache: Boolean , fromAddtoFragment : Boolean?) {
        var bundle = bundleOf("PLAYLIST_DATA_TYPE" to playlistDataType , "PLAYLIST_ID" to playlistId , "FROM_ADD_TO_FRAGMENT" to fromAddtoFragment,
            "PLAYLIST_DATA" to playlistData ,  "LOAD_FROM_CACHE" to loadFromCache)
        findNavController(R.id.nav_host_fragment).navigate(R.id.playlistFragment , bundle)
    }

    override fun movetoPlaylistBeta(playlistDataType: Int , playlistId: String?, playlistInfo: Playlist<String>?, loadFromCache: Boolean?) {
        var bundle = bundleOf("PLAYLIST_DATA_TYPE" to playlistDataType , "PLAYLIST_ID" to playlistId , "PLAYLIST_INFO" to playlistInfo , "LOAD_FROM_CACHE" to loadFromCache)
        findNavController(R.id.nav_host_fragment).navigate(R.id.playlistFragment , bundle)
    }

    override fun movetoAddSongFragment(playlistId: String?) {
        var bundle = bundleOf("PLAYLIST_ID" to playlistId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.addSongFragment , bundle)
    }

    override fun  moveToAddtoFragment(data : List<BaseModel> ,  mainContentType: Int){
        var bundle = bundleOf("DATA" to data , "DATA_TYPE" to mainContentType)
        findNavController(R.id.nav_host_fragment).navigate(R.id.addToFragment , bundle)
    }

    override fun moveToRadioList(name : String , category : String? , isUserStation : Boolean?){
        var bundle = bundleOf("NAME" to name , "USER_STATION" to isUserStation , "CATEGORY" to category)
        findNavController(R.id.nav_host_fragment).navigate(R.id.radioListFragment , bundle)
    }

    override fun movetoSingleRadioStation(genre : String? , radioData : Radio? , songId : String? , artistId: String?) {
        var bundle = bundleOf("RADIO_GENRE" to genre , "RADIO_DATA" to radioData , "SONG_ID" to songId , "ARTIST_ID" to artistId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.radioFragment , bundle)
    }

    override fun movetoAuthor(authorId: String) {
        var bundle = bundleOf("AUTHOR_ID" to authorId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.authorFragment , bundle)
    }

    override fun showBookList(bookList: List<Book<String>>, title : String ,  showFilter: Boolean) {
        var bundle = bundleOf("BOOK_LIST" to bookList , "TITLE" to title , "SHOW_FILTER" to showFilter)
        findNavController(R.id.nav_host_fragment).navigate(R.id.bookListFragment , bundle)
    }


}
