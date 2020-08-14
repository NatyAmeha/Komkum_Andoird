package com.example.ethiomusic

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.data.model.*
import com.example.ethiomusic.databinding.ActivityMainBinding
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

class MainActivity : ControllerActivity() , IMainActivity {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var viewmodelFactory : MainActivityViewmodelFactory
    val mainViewmodel : MainActivityViewmodel by viewModels { GenericSavedStateViewmmodelFactory(viewmodelFactory , this) }

    @Inject
    lateinit var downloadTracker: DownloadTracker

    lateinit var binding: ActivityMainBinding

//    val bottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat> by lazy {
//        var player = player_bsheet as LinearLayoutCompat
//         BottomSheetBehavior.from(player)
//    }

    var isBottomsheetClosed = true

    fun gettoken(){
        try {
            val info = packageManager.getPackageInfo(
                "com.example.ethiomusic",
                PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.i("Keyhash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("Keyhashe:", e.message)
        } catch (e: NoSuchAlgorithmException) {
            Log.i("Keyhashee:", e.message)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as EthioMusicApplication).appComponent.inject(this)
        setTheme(R.style.AppthemeDark)
//        gettoken()
        super.onCreate(savedInstanceState)

        if(isValidUser(context)){
           binding =  DataBindingUtil.setContentView(this , R.layout.activity_main)
            var navController = findNavController(R.id.nav_host_fragment)
            binding.bottomNavView.setupWithNavController(navController)
            binding.lifecycleOwner = this
            binding.viewmodel = mainViewmodel

            mainViewmodel.connectToService()

            mainViewmodel.playlistQueue.observe(this , Observer{
                if (it.isNullOrEmpty()){
                    hideplayer()
                }
                else showBottomView()
            })

            intent.getBooleanExtra("OPENPLAYER" , false).let { value ->
//                showPlayer()
//               if(value){
//                   bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//                   mainViewmodel.queueitem.observe(this , Observer { queue ->
//                       queue?.let {
//                           mainViewmodel.playlistQueue.value =  it.toSongList()
//                       }
//                   })
//               }
            }

            mainViewmodel.playlistQueue.observe(this , Observer { playlist ->
//                if(playlist.isNullOrEmpty()) hidePlayer()
//                else showPlayer()
            })


            binding.playerCard.setOnClickListener {
                hideBottomView()
               findNavController(R.id.nav_host_fragment).navigate(R.id.playerFragment)
            }


            mainViewmodel.playbackDuration.observe(this , Observer { duration ->
                binding.bottonsheetSongProgressbar.max  = duration.toInt()
            })

            mainViewmodel.playbackPosition.observe(this , Observer { position ->
                binding.bottonsheetSongProgressbar.progress  = position.toInt()
            })

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

    override fun onBackPressed() {
        super.onBackPressed()
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


    override fun movefrombaseFragmentToAlbumFragment(albumId : String , loadFromCache : Boolean , isNavigationFromDownload : Boolean) {
        var bundle = bundleOf("ALBUMID" to albumId , "LOAD_FROM_CACHE" to loadFromCache , "IS_NAV_FROM_DOWNLOAD" to isNavigationFromDownload)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment , bundle)
    }

    override fun moveToAlbumListFragment(fragmentHeaderInfo : String, dataType : Int) {
        var bundle = bundleOf("TOOLBAR_TITTLE" to fragmentHeaderInfo , "DATA_TYPE" to dataType)
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

    override fun moveToArtistList() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistListFragment)
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

    override fun movetoPlaylist(playlistDataType: Int, playlistId: String? , playlistData : Playlist<Song<String,String>>? , loadFromCache: Boolean) {
        var bundle = bundleOf("PLAYLIST_DATA_TYPE" to playlistDataType , "PLAYLIST_ID" to playlistId ,
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

    override fun movetoSingleRadioStation(genre : String? , radioData : Radio?) {
        var bundle = bundleOf("RADIO_GENRE" to genre , "RADIO_DATA" to radioData)
        findNavController(R.id.nav_host_fragment).navigate(R.id.radioFragment , bundle)
    }
}
