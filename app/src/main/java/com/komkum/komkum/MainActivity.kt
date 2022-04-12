package com.komkum.komkum

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.ui.setupWithNavController

import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.ActivityMainBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.*
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.ktx.Firebase
import com.skydoves.balloon.*
import com.komkum.komkum.ui.customview.CustomPlaylistImageview
import com.komkum.komkum.util.notification.FcmService

import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.concurrent.timer
import kotlin.math.roundToInt


class MainActivity : ControllerActivity(), IMainActivity,
    IRecyclerViewInteractionListener<Song<String, String>>  {

    val mainViewmodel: MainActivityViewmodel by viewModels()

    @Inject
    lateinit var downloadTracker: DownloadTracker

    lateinit var binding: ActivityMainBinding
    lateinit var queueAdapter: SongAdapter
    lateinit var timer : TimerTask


    var selectedMediaId: String? = null
    var selectedSongIndex: Int = 0
    var playerState: Int = -1
    var isPlayerViewVisible = false



    var playerTitle: String? = null
    var playerSubtittle: String? = null
    var imagelist = listOf<String>()
    var openPlayerFromNotification = false


    val indexToPage = mapOf(
        0 to R.id.homeFragment, 1 to R.id.bookHomeFragment, 2 to R.id.podcastHomeFragment,
        3 to R.id.searchFragment, 4 to R.id.libraryFragment
    )
    private val backStack = Stack<Int>()

//    val bottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat> by lazy {
//        var player = player_bsheet as LinearLayoutCompat
//         BottomSheetBehavior.from(player)
//    }

    var isBottomsheetClosed = true



    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppthemeLight)
//        gettoken()
        super.onCreate(savedInstanceState)


        // check if user visit the app  for the first time
        var pref = PreferenceHelper.getInstance(this)
        var isFirstTimeUser = pref.get(PreferenceHelper.FIRST_TIME_USER , true)
        if(isFirstTimeUser){
            Firebase.dynamicLinks.getDynamicLink(intent).addOnSuccessListener { pendingDLink ->
                if(pendingDLink == null){
                    // user opens the app from installed icon
                    this.sendIntent(OnboardingActivity::class.java)
                    return@addOnSuccessListener
                }
            }
                .addOnFailureListener {
                this.sendIntent(OnboardingActivity::class.java)
                return@addOnFailureListener
            }
        }


        // identify where users came from
        Firebase.dynamicLinks.getDynamicLink(intent).addOnSuccessListener { pendingDLink ->
            if(pendingDLink != null){
                var dLink = pendingDLink.link.toString()
                var urlsegments = dLink.split("/")
                var param = urlsegments.getOrNull(urlsegments.size -1)
                Log.i("link1" , "${param}  ${urlsegments.toString()}  ${dLink}")
                mainViewmodel.signUpSource = when(param.toString()){
                    "promo" -> FcmService.F_EPV_COUPON
                    "telegram_promo" -> FcmService.F_EPV_AD
                    else -> FcmService.F_EPV_TEAM_REFERRAL
                }
                return@addOnSuccessListener
            }
        }.addOnFailureListener {
                this.sendIntent(OnboardingActivity::class.java)
                return@addOnFailureListener
            }





        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewContainer = binding.mainactivityContainer

        var isAuthenticated = mainViewmodel.userRepo.userManager.isLoggedIn()

        var navController = findNavController(R.id.nav_host_fragment)
        binding.bottomNavView.setupWithNavController(navController)

        binding.bottomNavView.setOnItemReselectedListener {
            navController.popBackStack(it.itemId , false)
        }


        binding.lifecycleOwner = this
        binding.viewmodel = mainViewmodel


        // connect to payback service
//        mainViewmodel.connectToService()


        //check subscription is valid
        mainViewmodel.isSubscriptionValid().observe(this) {
            if (it == true) {
                pref[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
            }
            else{
                pref[AccountState.SUBSCRIPTON_PREFERENCE] = AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
                mainViewmodel.firebaseAnalytics.setUserProperty(FcmService.MEMBERSHIP_PLAN , FcmService.FREE_PLAN)
            }
        }


        // register fcm to token to receive push notification
        var isTokenNew = pref.get(PreferenceHelper.IS_NEW_FCM_TOKEN, false)
        if (isTokenNew && isAuthenticated) {
//                Toast.makeText(this , "token called" , Toast.LENGTH_LONG).show()
            var token = pref.get(PreferenceHelper.FCM_TOKEN, "")
            if (token.isNotBlank()) {
                mainViewmodel.updateFcmToken(token).observe(this, Observer {
                    it.data?.let {
                        if (it) {
//                            Toast.makeText(this, "update token ", Toast.LENGTH_LONG).show()
                            pref[PreferenceHelper.IS_NEW_FCM_TOKEN] = false
                        }
                    }
                })
            }
        }


        binding.bsheetSongTittleTextview.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.bsheetSongTittleTextview.marqueeRepeatLimit = -1
        binding.bsheetSongTittleTextview.isSelected = true

        intent.getBooleanExtra("OPENPLAYER", false).let { value ->
            if (value) {
                playerState = intent.getIntExtra("PLAYBACK_STATE", -1)
                when (playerState) {
                    1 -> mainViewmodel.playerState.value = PlayerState.PlaylistState()
                    2 -> mainViewmodel.playerState.value = PlayerState.AudiobookState()
                    3 -> mainViewmodel.playerState.value = PlayerState.RadioState()
                    4 -> mainViewmodel.playerState.value = PlayerState.PodcastState()
                }

                mainViewmodel.queueitem.observe(this, Observer { queue ->
                    queue?.let {
                        mainViewmodel.playlistQueue.value = it.toStreamable()
                        showBottomViewBeta()
                        findNavController(R.id.nav_host_fragment).navigate(R.id.playerFragment)
                    }
                })
            }
        }


        mainViewmodel.nowPlaying.observe(this, Observer { metadata ->
//                Log.i("playererror" ,  "triggered")
            playerTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
//                if(playerTitle.isNullOrEmpty()) hideplayer()


            playerSubtittle = metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE)

            metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)?.let { index ->
                mainViewmodel.playlistSelectedIndex.value = index.toInt()
//                    selectedSongIndex = index.toInt()
            }

        })

        mainViewmodel.playbackState.observe(this){
            when(it.state){
                PlaybackStateCompat.STATE_NONE -> {
                    mainViewmodel.playlistQueue.value = null
                    hidePlayerCard()
                }
            }
        }

        if(!isFinishing){
            showMainInfoInDialog(pref)
        }




        binding.playerCard.setOnClickListener {
            var extra = FragmentNavigatorExtras(
                binding.bsheetImageView to "imageView",
                binding.bsheetSongTittleTextview to "title"
            )
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.playerFragment,
                null,
                null,
                extra
            )
        }


        mainViewmodel.playbackDuration.observe(this, Observer { duration ->
            binding.bottonsheetSongProgressbar.max = duration.toInt()
        })

        mainViewmodel.playbackPosition.observe(this, Observer { position ->
            binding.bottonsheetSongProgressbar.progress = position.toInt()
        })


        // handle navigation from payment activity
        var yenepayPaymentFor = intent.getIntExtra("PAYMENT_FOR", -1)
        when (yenepayPaymentFor) {
            PaymentManager.PAYMENT_FOR_SUBSCRIPTION -> {
                var preference = PreferenceHelper.getInstance(this)
                var userId = preference.get(AccountState.USER_ID, "")
                var subscriptionId = preference.get("SUB_SUBSCRIPTION_ID", "")
                var verifyPaymentInfo =
                    VerifyPayment(userId, subscriptionId, VerifyPayment.PAYMENT_METHOD_YENEPAY)
                mainViewmodel.verifySubscriptionPayment(verifyPaymentInfo)
                    .observe(this, Observer { isValidSubscription ->
                        if (isValidSubscription) {
                            preference[AccountState.SUBSCRIPTON_PREFERENCE] =
                                AccountState.VALID_SUBSCRIPTION_PREFENRECE_VALUE
                            preference[AccountState.USED_AUDIOBOOK_CREDIT] = 0
                            sendIntent(MainActivity::class.java)
                        } else {
                            preference[AccountState.SUBSCRIPTON_PREFERENCE] =
                                AccountState.INVALID_SUBSCRIPTION_PREFENRECE_VALUE
                            Toast.makeText(this, "Invalid subscription", Toast.LENGTH_LONG).show()

                        }

                    })
            }
            PaymentManager.PAYMENT_FOR_AUDIOBOOK_PURCHASE -> {
                var bookId = intent.getStringExtra("BOOK_ID")
                var bundle = bundleOf(
                    "BOOK_ID" to bookId,
                    "LOAD_FROM_CACHE" to false,
                    "FROM_PAYMENT" to true
                )
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.audiobookSellFragment,
                    bundle
                )
            }
            PaymentManager.PAYMENT_FOR_EBOOK_PURCHASE -> {
                var bookId = intent.getStringExtra("BOOK_ID")
                var bundle = bundleOf(
                    "BOOK_ID" to bookId,
                    "LOAD_FROM_CACHE" to false,
                    "FROM_PAYMENT" to true
                )
                findNavController(R.id.nav_host_fragment).navigate(R.id.EBookSellFragment, bundle)
            }

            PaymentManager.PAYMENT_FOR_WALLET_RECHARGE -> {
                var rechargeAmount = intent.getIntExtra("RECHARGE_AMOUNT", 0)
                var paymentIdentifier = intent.getStringExtra("PAYMENT_IDENTIFIER")
//                    var receiverName = intent.getStringExtra("RECEIVER_NAME")
//                    var donationType = intent.getIntExtra("DONATION_TYPE" , -1)
//                    moveToDonation(donationType , receiverId , receiverName , true)
                if (rechargeAmount > 0 && paymentIdentifier != null) {
                    mainViewmodel
                        .rechargeWallet(
                            rechargeAmount,
                            paymentIdentifier,
                            Payment.PAYMENT_METHOD_YENEPAY
                        )
                        .observe(this) {
                            this.showSnacbar("Recharge successfull", "view") {
                                findNavController(R.id.nav_host_fragment).navigate(R.id.accountFragment)
                            }
//                            Toast.makeText(this, "Recharge response ${it}", Toast.LENGTH_LONG).show()
                        }
//                        mainViewmodel.verifyYenepayPayment(rechargeAmount , Payment.PAYMENT_METHOD_YENEPAY).observe(this){
//                            findNavController(R.id.nav_host_fragment).navigate(R.id.accountFragment)
//                        }
                } else Toast.makeText(this, "the amount must be greater than 1", Toast.LENGTH_LONG).show()

            }
        }


    }

    override fun onNewIntent(intent: Intent?) {
        intent?.getBooleanExtra("OPENPLAYER", false)?.let { value ->
            if (value) {
                playerState = intent.getIntExtra("PLAYBACK_STATE", -1)
                openPlayerFromNotification = true
                when (playerState) {
                    1 -> mainViewmodel.playerState.value = PlayerState.PlaylistState()
                    2 -> mainViewmodel.playerState.value = PlayerState.AudiobookState()
                    3 -> mainViewmodel.playerState.value = PlayerState.RadioState()
                    4 -> mainViewmodel.playerState.value = PlayerState.PodcastState()
                }
                findNavController(R.id.nav_host_fragment).navigate(R.id.playerFragment)


//                mainViewmodel.queueitem.observe(this , Observer { queue ->
//                    queue?.let {
//                        mainViewmodel.playlistQueue.value =  it.toStreamable()
//                    }
//                })
            }
        }
        super.onNewIntent(intent)
    }

//    override fun onBackPressed() {
//        if (backStack.size > 1) {
//            // remove current position from stack
//            backStack.pop()
//            // set the next item in stack as current
//            var cPosition = backStack.peek()
//            binding.mainContainerViewpager.currentItem = cPosition
//
//        } else super.onBackPressed()
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        if(::timer.isInitialized) timer.cancel()
        super.onDestroy()
    }

    // this fun shows main information like user commission, game reward and wallet status
    fun showMainInfoInDialog(pref: SharedPreferences) {
        // show language picker dialog if not shown in onboarding activity
         timer = Timer().schedule(15000){
            runOnUiThread {
                showLanguageSelelectDialog(true , 5000)
            }
        }


//        // USER COMMISSION DIALOG
        mainViewmodel.getCommission().observe(this) { commission ->
            if (commission != null && commission >= 50) {
                this.showDialog(
                    "${getString(R.string.birr)} ${commission.roundToInt()} ${getString(R.string.commission)}",
                    "${getString(R.string.commission_reward_msg)} ${getString(R.string.birr)} ${commission.roundToInt()} ${getString(R.string.commission_reward_msg_2)}",
                    getString(R.string.go_to_reward),
                    showNegative = true,
                    autoDismiss = false,
                    isBottomSheet = true
                ) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.rewardDashboardFragment)
//                    binding.progressBar4.isVisible = true
//                    mainViewmodel.transferCommissionToWallet(commission)
//                        .observe(this@MainActivity) {
//                            binding.progressBar4.isVisible = false
//                            if (it == true) showSnacbar("Commission transferred to your wallet")
//                            else Toast.makeText(
//                                this@MainActivity,
//                                "Unable to transfer commission to wallet",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
                }
            }
        }




        //USER GAME REWARD DIALOG
//        mainViewmodel.getTotalGameReward()
//        mainViewmodel.allRewards.observe(this) { reward ->
//            if (!reward.isNullOrEmpty()) {
//                mainViewmodel.allRewards.removeObservers(this)
//                this.showDialog(
//                    getString(R.string.get_your_rewards),
//                    getString(R.string.get_your_reward_description),
//                    getString(R.string.go_to_reward), showNegative = true, autoDismiss = false, isBottomSheet = true
//                ) {
////                    moveToTeamList(TeamGridListFragment.LOAD_USER_TEAMS)
//
//                    findNavController(R.id.nav_host_fragment).navigate(R.id.rewardDashboardFragment)
////                    binding.progressBar4.isVisible = true
////                    mainViewmodel.transferGamerewardToWallet(reward).observe(this){
////                        binding.progressBar4.isVisible = false
////                        if(it == true) showSnacbar("ETB ${reward} transferred to your wallet" , "View"){
////                            findNavController(R.id.nav_host_fragment).navigate(R.id.accountFragment)
////                        }
////                        else Toast.makeText(this@MainActivity , "Unable to transfer Game rewards to wallet" , Toast.LENGTH_LONG).show()
////
////                    }
//                }
//            }
//        }
    }




    // methods called in other pages
    fun showBottomViewBeta() {
        mainViewmodel.playlistQueue.value?.let {
            if (it.isNullOrEmpty()) {
                binding.playerCard.visibility = View.GONE
            } else binding.playerCard.visibility = View.VISIBLE
        }
        binding.bottomNavView.visibility = View.VISIBLE
    }

    fun showPlayer() {
        binding.playerCard.visibility = View.VISIBLE
    }

    fun hideBottomView() {
        binding.playerCard.visibility = View.GONE
        binding.bottomNavView.visibility = View.GONE
    }

    fun hidePlayerCard() {
        binding.playerCard.visibility = View.GONE
    }


//    fun handleNotification(){
//        mainViewmodel.appDb.downloadDao.getDownloads().observe(this , Observer{downloadList ->
//            if(downloadList.isNotEmpty()){
//                mainViewmodel.handleDownloadNotification(this , this , downloadList.last())
//            }
//
//        })
//    }

//    fun handleBottomsheet(){
//        if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//        else bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }
//
//    fun closeBottomSheet(){
//        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//    }


    fun showSnacbar(message: String, commandString: String? = null, action: (() -> Unit)? = null) {
        Snackbar.make(binding.mainactivityContainer, message, Snackbar.LENGTH_LONG)
            .setAction(commandString) {
                if (action != null) action()
            }
            .show()
    }


    fun showErrorSnacbar(message: String, commandString: String? = null, action: (() -> Unit)?) {
        val snack =
            Snackbar.make(binding.mainactivityContainer, message, Snackbar.LENGTH_INDEFINITE)
                .apply {
                    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines =
                        10
                    setAction(commandString) {
                        if (action != null) action()
                        dismiss()
                    }
                }

        snack.show()
    }




    override fun onItemClick(data: Song<String, String>, position: Int, option: Int?) {}
    override fun activiateMultiSelectionMode() {}

    override fun onSwiped(position: Int) {}

    override fun onMoved(prevPosition: Int, newPosition: Int) {}


    override fun movefrombaseFragmentToAlbumFragment(
        albumId: String,
        loadFromCache: Boolean,
        isNavigationFromDownload: Boolean
    ) {
        var bundle = bundleOf(
            "ALBUMID" to albumId,
            "LOAD_FROM_CACHE" to loadFromCache,
            "IS_NAV_FROM_DOWNLOAD" to isNavigationFromDownload
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment, bundle)
    }

    override fun moveToAlbumListFragment(
        toolbarTitle: String,
        dataType: Int?,
        albumList: List<Album<String, String>>?
    ) {
        var bundle = bundleOf(
            "TOOLBAR_TITTLE" to toolbarTitle,
            "DATA_TYPE" to dataType,
            "ALBUM_LIST" to albumList
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumListFragment, bundle)
    }

    override fun movetoSongListFragment(
        songlistType: Int?,
        songList: List<Song<String, String>>?,
        toolbarTitle: String?
    ) {
        var bundle = bundleOf(
            "SONG_LIST_TYPE" to songlistType,
            "SONGLIST" to songList,
            "TOOLBAR_TITLE" to toolbarTitle
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.songListFragment, bundle)
    }

    override fun moveToArtist(artistId: String) {
        var bundle = bundleOf("ARTISTID" to artistId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistFragment, bundle)
    }

    override fun moveToArtistList(
        toolbarTitle: String,
        dataType: Int?,
        artistList: List<Artist<String, String>>?
    ) {
        var bundle = bundleOf(
            "TOOLBAR_TITLE" to toolbarTitle,
            "DATA_TYPE" to dataType,
            "ARTIST_LIST" to artistList
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistListFragment, bundle)
    }

    override fun movetoArtistBio(metadata: ArtistMetaData) {
        var bundle = bundleOf("ARTIST_METADATA" to metadata)
        findNavController(R.id.nav_host_fragment).navigate(R.id.artistInfoFragment, bundle)
    }

    override fun movefromArtisttoAlbum(albumId: String) {
        var bundle = bundleOf("ALBUMID" to albumId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment, bundle)
    }

    override fun moveFromAlbumListtoAlbumFragment(albumId: String) {
        var bundle = bundleOf("ALBUMID" to albumId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.albumFragment, bundle)
    }

    override fun movetoPlaylist(
        playlistDataType: Int,
        playlistId: String?,
        playlistData: Playlist<Song<String, String>>?,
        loadFromCache: Boolean,
        fromAddtoFragment: Boolean?
    ) {
        var bundle = bundleOf(
            "PLAYLIST_DATA_TYPE" to playlistDataType,
            "PLAYLIST_ID" to playlistId,
            "FROM_ADD_TO_FRAGMENT" to fromAddtoFragment,
            "PLAYLIST_DATA" to playlistData,
            "LOAD_FROM_CACHE" to loadFromCache
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.playlistFragment, bundle)
    }

    override fun movetoPlaylistBeta(
        playlistDataType: Int,
        playlistId: String?,
        playlistInfo: Playlist<String>?,
        loadFromCache: Boolean?,
        view: CustomPlaylistImageview?
    ) {
        var bundle = bundleOf(
            "PLAYLIST_DATA_TYPE" to playlistDataType,
            "PLAYLIST_ID" to playlistId,
            "PLAYLIST_INFO" to playlistInfo,
            "LOAD_FROM_CACHE" to loadFromCache
        )

        if (view != null) {
            var extra = FragmentNavigatorExtras(view to "imageview")
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.playlistFragment,
                bundle,
                null,
                extra
            )
        } else findNavController(R.id.nav_host_fragment).navigate(R.id.playlistFragment, bundle)

    }

    override fun movetoAddSongFragment(playlistId: String?) {
        var bundle = bundleOf("PLAYLIST_ID" to playlistId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.addSongFragment, bundle)
    }

    override fun moveToAddtoFragment(
        ids: List<String>,
        mainContentType: Int,
        songList: List<Song<String, String>>
    ) {
        var bundle =
            bundleOf("SONG_IDS" to ids, "DATA_TYPE" to mainContentType, "SONG_LISTS" to songList)
        Log.i("songlistselected", songList.size.toString() + " " + ids.size.toString())
        findNavController(R.id.nav_host_fragment).navigate(R.id.addToFragment, bundle)
    }

    override fun moveToRadioList(name: String, category: String?, isUserStation: Boolean?) {
        var bundle =
            bundleOf("NAME" to name, "USER_STATION" to isUserStation, "CATEGORY" to category)
        findNavController(R.id.nav_host_fragment).navigate(R.id.radioListFragment, bundle)
    }

    override fun movetoSingleRadioStation(
        genre: String?,
        radioData: Radio?,
        songId: String?,
        artistId: String?
    ) {
        var bundle = bundleOf(
            "RADIO_GENRE" to genre,
            "RADIO_DATA" to radioData,
            "SONG_ID" to songId,
            "ARTIST_ID" to artistId
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.radioFragment, bundle)
    }

    override fun movetoAuthor(authorId: String) {
        var bundle = bundleOf("AUTHOR_ID" to authorId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.authorFragment, bundle)
    }

    fun movetoAuthorsList(listType: Int, authorsList: List<Author<String, String>>? = null) {
        var bundle = bundleOf("AUTHOR_LIST" to authorsList, "AUTHOR_LIST_TYPE" to listType)
        findNavController(R.id.nav_host_fragment).navigate(R.id.authorListFragment, bundle)
    }

    override fun showBookList(bookList: List<Book<String>>, title: String, showFilter: Boolean) {
        var bundle =
            bundleOf("BOOK_LIST" to bookList, "TITLE" to title, "SHOW_FILTER" to showFilter)
        findNavController(R.id.nav_host_fragment).navigate(R.id.bookListFragment, bundle)
    }


    fun moveToPodcast(podcastId: String) {
        var bundle = bundleOf("PODCAST_ID" to podcastId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.podcastFragment, bundle)
    }

    fun moveToPodcastList(loadType: Int, category: String? = null) {
        var bundle = bundleOf("LOAD_TYPE" to loadType, "CATEGORY" to category)
        findNavController(R.id.nav_host_fragment).navigate(R.id.podcastListFragment, bundle)
    }

    fun moveToEpisode(episode: PodcastEpisode, loadFromCache: Boolean? = false) {
        var bundle = bundleOf("EPISODE_INFO" to episode, "LOAD_FROM_CACHE" to loadFromCache)
        findNavController(R.id.nav_host_fragment).navigate(R.id.podcastEpisodeFragment, bundle)
    }

    fun moveToPodcastPublisher(publisherId: String) {
        var bundle = bundleOf("PUBLISHER_ID" to publisherId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.podcastPublisherFragment, bundle)
    }

    fun moveToDonation(
        donationtype: Int? = null,
        receiverId: String? = null,
        receiverName: String? = null,
        creatorId: String,
        creatorImage: String
    ) {
        var bundle = bundleOf(
            "RECEIVER_ID" to receiverId,
            "RECEIVER_NAME" to receiverName,
            "DONATION_TYPE" to donationtype,
            "CREATOR_ID" to creatorId,
            "CREATOR_IMAGE" to creatorImage
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.donationFragment, bundle)
    }

    fun moveToReviewList(id: String, reviewType: Int, ratingFilter: String? = "all") {
        var bundle =
            bundleOf("ID" to id, "REVIEW_TYPE" to reviewType, "RATING_FILTER" to ratingFilter)
        findNavController(R.id.nav_host_fragment).navigate(R.id.reviewListFragment, bundle)
    }

    fun moveToProductCategories() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.productCategoryListFragment)
    }


    fun moveToBrowseProductByDepartment(department: String) {
        var bundle = bundleOf("DEPARTMENT" to department)
        findNavController(R.id.nav_host_fragment).navigate(R.id.browseProductFragment, bundle)
    }

    fun movetoProductDetails(productId: String, hideAction: Boolean = false) {
        var bundle = bundleOf("PRODUCT_ID" to productId, "HIDE_ACTION" to hideAction)
        findNavController(R.id.nav_host_fragment).navigate(R.id.productFragment, bundle)
    }

    fun movetoCreateReview(
        contentId: String,
        contentType: Int,
        image: String,
        title: String,
        subtitle: String
    ) {
        var bundle = bundleOf(
            "CONTENT_ID" to contentId,
            "CONTENT_TYPE" to contentType,
            "IMAGE" to image,
            "TITLE" to title,
            "SUBTITLE" to subtitle
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.createReviewFragment, bundle)
    }

    fun moveToProductList(loadType: Int, category: String? = null, tag: String? = null) {
        var bundle = bundleOf("LOAD_TYPE" to loadType, "CATEGORY" to category, "TAG" to tag)
        findNavController(R.id.nav_host_fragment).navigate(R.id.productListFragment, bundle)
    }

    fun moveToProductCustomization(
        productIdCsv: String,
        purchaseType: Int,
        teamId: String? = null,
        additionalQty : Int? = 0
    ) {
        var bundle = bundleOf(
            "PRODUCT_ID_CSV" to productIdCsv,
            "TEAM_ID" to teamId,
            "PURCHASE_TYPE" to purchaseType,
            "ADDITIONAL_QTY" to additionalQty
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.customizeProductFragment, bundle)
    }

    fun moveToCreateTEam(
        productId: List<String>,
        teamSize: Int,
        teamDuration: Int? = null,
        teamType: Int? = null
    ) {
        var bundle = bundleOf(
            "PRODUCT_ID" to productId, "TEAM_SIZE" to teamSize, "TEAM_DURATION" to teamDuration,
            "TEAM_TYPE" to teamType
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.createTeamFragment, bundle)
    }

    fun movetoTeamDetails(
        teamId: String,
        inviterId: String? = null,
        @IdRes backstackId: Int? = null,
        loadType : Int? = 0
    ) {
        var bundle =
            bundleOf("TEAM_ID"  to teamId, "INVITER_ID" to inviterId, "LOAD_TYPE" to loadType ,  "BACKSTACK_ID" to backstackId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.teamFragment, bundle)
    }

    fun moveToGameTeamDetails(
        adId: String,
        inviterId: String? = null,
        pageIndex: Int = 0,
        @IdRes backstackId: Int? = null
    ) {
        var bundle = bundleOf(
            "AD_ID" to adId,
            "INVITER_ID" to inviterId,
            "PAGE_INDEX" to pageIndex,
            "BACKSTACK_ID" to backstackId
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.gameTeamFragment, bundle)
    }


    fun movetoPackageTeamDetails(teamId: String, @IdRes backstackId: Int? = null) {
        var bundle = bundleOf("TEAM_ID" to teamId, "BACKSTACK_ID" to backstackId)
        findNavController(R.id.nav_host_fragment).navigate(R.id.packageTeamFragment, bundle)
    }


    fun moveToTeamList(
        loadType: Int,
        productInfo: ProductMetadata<Product, String>,
        city: String? = null
    ) {
        var bundle =
            bundleOf("LOAD_TYPE" to loadType, "PRODUCT_INFO" to productInfo, "CITY" to city)
        findNavController(R.id.nav_host_fragment).navigate(R.id.teamListFragment, bundle)
    }

    fun moveToTeamList(loadType: Int , teamList: List<Team<Product>>? = null) {
        var bundle = bundleOf("LOAD_TYPE" to loadType , "TEAM_LIST" to teamList)
        findNavController(R.id.nav_host_fragment).navigate(R.id.teamGridListFragment, bundle)
    }


    fun moveToOrderList() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.orderListFragment)
    }

    fun movetoOrderDetailsFragment(
        orderId: String,
        loadType: Int,
        order: Order<String, String>? = null,
        popToStart: Boolean = false
    ) {
        var bundle = bundleOf(
            "ORDER_ID" to orderId,
            "LOAD_TYPE" to loadType,
            "ORDER" to order,
            "POP_TO_START" to popToStart
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.orderDetailFragment, bundle)
    }

    fun movetoOrderFragment(
        orderInfo: Order<String, Address>,
        selectedAddress: Address? = null,
        purchaseType: Int? = null,
        maxDeliveryDay : Int? = 1
    ) {
        var bundle = bundleOf(
            "ORDER" to orderInfo,
            "SELECTED_ADDRESS" to selectedAddress,
            "PURCHASE_TYPE" to purchaseType,
            "MAX_DELIVERY_DAY" to maxDeliveryDay
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.orderFragment, bundle)
    }


    fun movetoOrderCompletedFragment(order: Order<String, String>) {
        var bundle = bundleOf("ORDER" to order)
        findNavController(R.id.nav_host_fragment).navigate(R.id.orderCompleteFragment, bundle)
    }

    fun moveToAddresList(order: Order<String, Address>, addressList: List<Address> , maxDeliveryDay: Int? = 1) {
        var bundle = bundleOf("ORDER" to order, "ADDRESS_LIST" to addressList , "MAX_DELIVERY_DAY" to maxDeliveryDay)
        findNavController(R.id.nav_host_fragment).navigate(R.id.addressListFragment, bundle)
    }

    fun moveToCreateAddress(order: Order<String, Address>, addressList: List<Address> , maxDeliveryDay: Int? = 1) {
        var bundle = bundleOf("ORDER" to order, "ADDRESS_LIST" to addressList , "MAX_DELIVERY_DAY" to maxDeliveryDay)
        findNavController(R.id.nav_host_fragment).navigate(R.id.createAddressFragment, bundle)
    }


    fun moveToAdDisplay(
        adInfo: Ads,
        questionIndex: Int = 0,
        teamId: String,
        totalResult: Int,
        isUserPlayed: Boolean? = null
    ) {
        var bundle = bundleOf(
            "AD_INFO" to adInfo, "QUESTION_INDEX" to questionIndex,
            "TEAM_ID" to teamId, "RESULT" to totalResult, "IS_USER_PLAYED" to isUserPlayed
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.adDisplayFragment, bundle)
    }

    fun moveToStartGame(adInfo: Ads, questionIndex: Int = 0, teamId: String, totalResult: Int) {
        var bundle = bundleOf(
            "AD_INFO" to adInfo,
            "QUESTION_INDEX" to questionIndex,
            "TEAM_ID" to teamId,
            "RESULT" to totalResult
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.startGameFragment, bundle)
    }

    fun moveToQuiz(
        adInfo: Ads,
        questionIndex: Int = 0,
        teamId: String,
        totalResult: Int,
        isUserPlayed: Boolean? = null
    ) {
        var bundle = bundleOf(
            "AD_INFO" to adInfo, "QUESTION_INDEX" to questionIndex,
            "TEAM_ID" to teamId, "RESULT" to totalResult, "IS_USER_PLAYED" to isUserPlayed
        )
        findNavController(R.id.nav_host_fragment).navigate(R.id.quizFragment, bundle)
    }

    fun movetoCompleteGame(result: Int, adInfo: Ads) {
        var bundle = bundleOf("RESULT" to result, "AD_INFO" to adInfo)
        findNavController(R.id.nav_host_fragment).navigate(R.id.completeGameFragment, bundle)
    }


}
