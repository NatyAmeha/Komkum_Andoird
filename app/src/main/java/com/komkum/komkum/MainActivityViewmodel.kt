package com.komkum.komkum

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.repo.*
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.data.viewmodel.RewardInfo
import com.komkum.komkum.media.ServiceConnector
import com.komkum.komkum.ui.account.accountservice.PaymentManager
import com.komkum.komkum.ui.account.subscription.SubscriptionManager
import com.komkum.komkum.usecase.*
import com.komkum.komkum.util.LibraryService
import com.komkum.komkum.util.adaper.DownloadAdapter
import com.komkum.komkum.util.adaper.PaymentMethodAdapter
import com.komkum.komkum.util.extensions.convertoLocalDate
import com.komkum.komkum.util.notification.FcmService
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.PlayerState
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewmodel @ViewModelInject
constructor(@Assisted var savedstate : SavedStateHandle, @ActivityContext var appContext : Context,
            var serviceConnector: ServiceConnector, var libraryService : LibraryService, var subscriptionrepo : SubscriptionRepository,
            var downloadTracker: DownloadTracker, var downloadRepo : DownloadRepository, var userRepo : UserRepository, var homerepo : HomeRepository,
            var radioRepo : RadioRepository, var appDb: AppDb, var podcastRespo : PodcastRepository, var subscriptionManager: SubscriptionManager,
            var paymentManager: PaymentManager, var billingRepo : BillingRepository , var adRepo : AdRepository ,
var walletUsecase: WalletUsecase , var paymentUsecase: PaymentUsecase , var googlePlayPayment: GooglePlayVendor , var walletPayment: WalletPayment) : ViewModel() {

    var url = "http://192.168.137.82"

    var playlistQueue  = MutableLiveData<MutableList<Streamable<String,String>>>()
    var newAddedSongToPlaylist = MutableLiveData<List<Song<String,String>>>()
    var audiobookPlaylistQueue = MutableLiveData<List<Chapter>>(emptyList())
    var playlistSelectedIndex = MutableLiveData(-1)

    var playedFrom = MutableLiveData("Now Playing")
    var showPlayerCard = true

    val firebaseAnalytics = Firebase.analytics

    val songrecyclerviewStateManeger : RecyclerviewStateManager<Song<String, String>> by lazy {
        var stateManager = RecyclerviewStateManager<Song<String,String>>()
        stateManager
    }

    val episodeStateManager : RecyclerviewStateManager<PodcastEpisode> by lazy {
        RecyclerviewStateManager<PodcastEpisode>()
    }

    var googlePlaysubscriptionPurchaseState = billingRepo.PurchaseStatus

    var isAudioPrepared = false
    var isAudiobookIntro = false
    var isLoadFromCache = false
    var isFavoriteSong = MutableLiveData(false)
    var isEpisodeInFavorite = MutableLiveData(false)
    var isAlbumFavorite = MutableLiveData(false)

    var selectedRadio : Radio? = null

    var totalCartCounter = MutableLiveData(0)

    // track where orders originated from
    var purchasedFrom : String? = null

    //track sign up source
    var signUpSource : String = FcmService.F_EPV_ORGANIC

    // the following field used to track user listening when user moves between chapter and books
    var previousListeningBookChapterId : String? = null



    var error = MutableLiveData<String>()
    fun getError() : MediatorLiveData<String> {
        var a = MediatorLiveData<String>()
        a.addSource(userRepo.error){error.value = it}
        a.addSource(subscriptionrepo.error){ error.value = it}
        a.addSource(radioRepo.error){ error.value = it}
        a.addSource(adRepo.error){error.value = it}
        a.addSource(libraryService.songRepo.error){error.value = it}
        a.addSource(paymentUsecase.error){error.value = it}
        a.addSource(walletUsecase.error){error.value = it}
        a.addSource(googlePlayPayment.error){error.value = it}
        a.addSource(walletPayment.error){error.value = it}
        return a
    }

    fun removeOldErrors(){
        userRepo.error.value = null
        subscriptionrepo.error.value = null
        radioRepo.error.value = null
        libraryService.error.value = null
        paymentUsecase.error.value = null
        walletUsecase.error.value = null
        googlePlayPayment.error.value = null
        walletPayment.error.value = null
    }

    fun getSelectedSongInfo(songId : String) = playlistQueue.value?.find { song -> song._id == songId }
    fun getSelectedSongInfo(index : Int) = playlistQueue.value!![index]



    val playbackState = serviceConnector.playbackstate
    val nowPlaying = serviceConnector.nowPlaying
    var playerState = MutableLiveData<PlayerState>()
    var playbackDuration = serviceConnector.duration
    var playbackPosition = serviceConnector.currentPosition
    var queueitem = serviceConnector.queue

    fun connectToService() = serviceConnector.connect()
    fun disconnectToService() = serviceConnector.disconnect()
    fun preparePlayer(mediaType : Int , isShuffled : Boolean? = false) = serviceConnector.initiatePlayer(playlistQueue.value!! , mediaType , isShuffled)




    fun prepareAndPlaySongsBeta(songs : List<Song<String,String>>  , state: PlayerState ,  position: Int? , isShuffled : Boolean? = false ,
                                loadFromCache : Boolean = false , playerStateIndicator: Int? = 1){
        showPlayerCard = true
        previousListeningBookChapterId = null
        playerState.value = state

        var sSongs = songs.toMutableList<Streamable<String,String>>()
        var songsWithAdsFinal = mutableListOf<Streamable<String,String>>()
        var addCounter = 0

        if(loadFromCache){
            playlistQueue.value = songs.toMutableList()
            serviceConnector.initiatePlayer(songs , ServiceConnector.STREAM_TYPE_DOWNLOAD , isShuffled , 5, position)
        }
        else{
            var isSubscriptionValid = userRepo.userManager.hasValidSubscription()
            if(!isSubscriptionValid){
                viewModelScope.launch {
                    var adsResult = userRepo.getAdvertisement(songs.size).data
                    adsResult?.let {ads ->
                        var adsList = ads as  List<Streamable<String , String>>
                        sSongs.forEach {
                            songsWithAdsFinal.add(adsList[addCounter % adsList.size])
                            songsWithAdsFinal.add(it)
                            ++addCounter
                        }
                        playlistQueue.value = songsWithAdsFinal.toMutableList()
                        serviceConnector.initiatePlayer(songsWithAdsFinal , ServiceConnector.STREAM_TYPE_MEDIA , isShuffled , playerStateIndicator , position?.plus(position))
                    }
                }
            }
            else{
                playlistQueue.value = sSongs
                serviceConnector.initiatePlayer(songs , ServiceConnector.STREAM_TYPE_MEDIA , isShuffled , playerStateIndicator , position)
            }
        }
    }

     fun prepareAndPlayAudioBook(chapterList : List<Chapter> , isIntro : Boolean , state : PlayerState , position: Int? , loadFromCache: Boolean = false ,
                                bookid : String? = null , chapterPosition : Long? = null , chapterIndex : Int? = null , duration: Int? = null) {

        playlistQueue.value = chapterList.toMutableList()

        audiobookPlaylistQueue.value = chapterList
        playerState.value = state
        if(isIntro) serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK_SAMPLE)
        else{
            showPlayerCard = true
            if(loadFromCache){
                //store previously played audiobook info
                Log.i("audioupdate" , "$chapterPosition")
                if(previousListeningBookChapterId != null){
                    var pos = playbackPosition.value
                    downloadRepo.updateChapterPosition(previousListeningBookChapterId!! , pos!!.toLong())
                }
                serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK , bookid , chapterPosition , chapterIndex)
                playerState.value = state
                previousListeningBookChapterId = chapterList[position!!]._id
                play(position.toLong())
            }
//            else serviceConnector.initiateAudioBookPlayer(chapterList , ServiceConnector.STREAM_TYPE_AUDIOBOOK)

        }
    }



    fun prepareAndPlayPodcast(episodes : List<PodcastEpisode>  , state : PlayerState , position: Int? , loadFromCache: Boolean = false ,
                                podcastId : String? = null , episodePosition : Long? = null , episodeIndex : Int? = null) {

        playerState.value = state
        showPlayerCard = true
            if(loadFromCache){
                serviceConnector.initiatePodcastPlayer(episodes , ServiceConnector.STREAM_TYPE_DOWNLOAD , podcastId , episodePosition , episodeIndex)
                if (position != null) play(position.toLong())
                else play()
            }
            else{
                var sEpisodes = episodes.toMutableList<Streamable<String,String>>()
                var episodeWithAds = mutableListOf<Streamable<String,String>>()
                var addCounter = 0

                var isSubscriptionValid = userRepo.userManager.hasValidSubscription()
                if(!isSubscriptionValid){
                    viewModelScope.launch {
                        var adsResult = userRepo.getAdvertisement(6).data
                        adsResult?.let {ads ->
                            var adsList = ads as  List<Streamable<String , String>>
                            sEpisodes.addAll(adsList)

                            sEpisodes.forEach {
                                episodeWithAds.add(adsList[addCounter % adsList.size])
                                ++addCounter
                                episodeWithAds.add(adsList[addCounter % adsList.size])
                                ++addCounter
                                episodeWithAds.add(it)

                            }
                            playlistQueue.value = episodeWithAds.toMutableList()
                            serviceConnector.initiatePodcastPlayer(episodeWithAds , ServiceConnector.STREAM_TYPE_PODCAST_EPISODE , podcastId , episodePosition , episodeIndex)
                        }
                    }
                }
                else{
                    playlistQueue.value = sEpisodes
                    serviceConnector.initiatePodcastPlayer(episodes , ServiceConnector.STREAM_TYPE_PODCAST_EPISODE , podcastId , episodePosition , episodeIndex)
                }
                if (position != null) play(position.toLong())
                else play()
            }
    }




    fun updateChapterCurrentPosition(bookId : String , chapterId : String , chapterIndex : Int , position : Long , duration : Int = 0){
        CoroutineScope(Dispatchers.IO).launch{

            bookId?.let {

                appDb.audiobookDao.updateLastListeningChapter(it , chapterIndex)  }
        }
    }

    fun continueSongStream(){
        serviceConnector.sendCommand(ServiceConnector.CONTINUE_SONG_STREAM)
    }

    fun play() = serviceConnector.sendCommand(ServiceConnector.ACTION_PLAY)
    fun play(playlistIndex : Long) = serviceConnector.sendCommand(ServiceConnector.ACTION_SKIP_TO_ITEM , playlistIndex)
    fun pause() = serviceConnector.sendCommand(ServiceConnector.ACTION_PAUSE)
    fun next() = serviceConnector.sendCommand(ServiceConnector.ACTION_NEXT)
    fun prev() = serviceConnector.sendCommand(ServiceConnector.ACTION_PREV)
    fun rewind() = serviceConnector.sendCommand(ServiceConnector.ACTION_REWIND)
    fun forward() = serviceConnector.sendCommand(ServiceConnector.ACTION_FORWARD)
    fun stopPlayback(){
        serviceConnector.sendCommand(ServiceConnector.ACTION_STOP)
    }
    fun seekTo(position : Long) = serviceConnector.sendCommand(ServiceConnector.ACTION_SEEK_TO , null , position = position)
    fun shuffle() = serviceConnector.sendCommand(ServiceConnector.ACTION_SHUFFLE)
    fun updateStreamCount() = serviceConnector.sendCommand(ServiceConnector.UPDATE_STREAM_COUNT)

    fun addToQueue(songList : MutableList<Song<String,String>>){
        if(playlistQueue.value.isNullOrEmpty()){
            prepareAndPlaySongsBeta(songList, PlayerState.PlaylistState() , null , false , false , 1)
        }
        else {
            var initialList = playlistQueue.value!!.toMutableList()
            initialList.addAll(songList)
            playlistQueue.value  = initialList
        }
        serviceConnector.addToQueueList(songList)
    }

    fun removeQueueItem(position: Int) = serviceConnector.removeQueueItem(position)
    fun moveQueueItem(prevPosition : Int , newPosition : Int) = serviceConnector.moveQueueItem(prevPosition , newPosition)


    fun isSubscriptionValid() = liveData {
        var result = userRepo.checkSubscriptionFromServer()
        emit(result.data)
    }

    var subscriptionList = liveData {
        var result = subscriptionrepo.getSubscriptionsBeta().data
        emit(result)
    }

    fun getWalletBalance() = liveData {
        var result = walletUsecase.getWalletBalance()
        emit(result)
    }

    fun handleyenepayPayment(price : Double , orderId : String , orderName : String , context : Context){
        paymentManager.purchaseUsingYenepay(price , orderId , orderName , context){}
    }

    fun purchaseUsingGooglePlay(productId : String , productType : String){
        billingRepo.initAndLaunchBillingClient(productId , productType)
    }

    fun upgradeSubscription(subscriptionId : String , paymentMethod: Int) = liveData {
        var result = subscriptionrepo.upgradeSubscription(subscriptionId , paymentMethod).data
        emit(result)
    }

    fun handleSubscriptionUpgrade(paymentHandler : IPayment , subscriptionId: String , token: String? = null , onSuccess : () -> Unit) = viewModelScope.launch{
        var result = subscriptionrepo.upgradeSubscription(subscriptionId , paymentHandler.paymentMethod , token).data
        if(result == true) paymentHandler.completePayment(token , paymentUsecase.transactionType)
        onSuccess()
    }

    fun initWalletPaymentFlow(transactionType : Int, amount: Double, id : String, context: Context, name : String, owner : LifecycleOwner, listener: OnPaymentListener ) = viewModelScope.launch{
        paymentUsecase.initPaymentFlow(walletPayment, transactionType , amount , id , context , name , owner  , listener)
    }

    fun initGooglePlayPaymentFlow(transactionType: Int, amount: Double, id : String, context: Context, name : String, owner : LifecycleOwner, listener: OnPaymentListener ) = viewModelScope.launch{
        paymentUsecase.initPaymentFlow(googlePlayPayment , transactionType , amount , id , context , name , owner  , listener)
    }

    fun verifySubscriptionPayment(paymentInfo : VerifyPayment) = liveData {
        try{
            var result = subscriptionManager.verifySubscription(paymentInfo)
            emit(result)
        }catch (ex : Throwable){
            error.value = ex.message
        }
    }

    fun acknowledgeSubscription(purchaseToken : String) = liveData{
        var result = billingRepo.acknowledgePurchases(purchaseToken)
        emit(result)
    }


    fun showPaymentMethodsDialog(context : Context , paymentMethods : List<PaymentMethod> , amount : Double , lifecycleOwner: LifecycleOwner , onPaymentMethodSelected : (index : Int) -> Unit){
        var dialog = MaterialDialog(context , BottomSheet(LayoutMode.WRAP_CONTENT))
        dialog.cornerRadius(literalDp = 14f).customView(R.layout.payment_method_custom_view , scrollable = true)
            .cancelOnTouchOutside(false)

        var interactionListener = object : IRecyclerViewInteractionListener<PaymentMethod> {
            override fun onItemClick(data: PaymentMethod, position: Int, option: Int?) {
                dialog.dismiss()
                onPaymentMethodSelected(position)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var info = RecyclerViewHelper(interactionListener = interactionListener , owner = lifecycleOwner)
        var paymentAdapter = PaymentMethodAdapter(info , paymentMethods)

        var customview =  dialog.getCustomView()

        var totalPriceTextview = customview.findViewById<TextView>(R.id.total_price_tview)
        var paymentMethodsRecyclerview = customview.findViewById<RecyclerView>(R.id.payment_method_recyclerview)

        totalPriceTextview.text = "${context.resources.getString(R.string.birr)} $amount"
        paymentMethodsRecyclerview.layoutManager = LinearLayoutManager(context)
        paymentMethodsRecyclerview.adapter = paymentAdapter

        dialog.show()
    }



    fun downloadSong(song : Song<String,String> , owner : LifecycleOwner ) = viewModelScope.launch{
       downloadRepo.downloadSongs(listOf(song))
       downloadTracker.addDownloads(listOf(song) , owner , DownloadAdapter.DOWNLOAD_TYPE_SONG)
   }

    fun updateRadioStation(radioInfo : UserRadioInfo) = radioRepo.updateRadioStation(radioInfo)

//    fun handleDownloadNotification(context : Context , lifecycleOwner: LifecycleOwner , downloadInfo: DbDownloadInfo) = viewModelScope.launch {
//        withContext(Dispatchers.IO){
//            downloadTracker.calculateProgress(
//                downloadInfo, lifecycleOwner , null ,
//                fun (progress : Int){
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        NotificationHelper.buildAlbumDownloadNotification(context , downloadInfo._id!! , downloadInfo.name , progress)
//                    }
//                }){ NotificationHelper.buildCompleteNotification(context , downloadInfo._id!! , downloadInfo.name)
//            }
//        }
//
//    }

    fun checkSongInFavorite(songId: String) = viewModelScope.launch { isFavoriteSong.value = libraryService.checkSongInFavorite(songId) }
    fun checkEpisodeInFavorite(episodeId: String) = viewModelScope.launch { isEpisodeInFavorite.value = podcastRespo.checkEpisodeFavorite(episodeId).data }


    fun addToFavoriteList(type : Int , songIds : List<String>) = liveData {
        var addResult =   libraryService.addToFavorite(type , songIds)
        addResult?.let {
            when(type){
                LibraryService.CONTENT_TYPE_SONG -> isFavoriteSong.value = addResult ?: null
                LibraryService.CONTENT_TYPE_ALBUM -> isAlbumFavorite.value = addResult ?: null
            }
        }
        emit(addResult)
    }
    fun removeFromFavorite(type : Int , ids : List<String>) = viewModelScope.launch {
        var removeResult =  libraryService.removeFromFavorite(type , ids)
        removeResult?.let {
            when(type){
                LibraryService.CONTENT_TYPE_SONG -> isFavoriteSong.value = !removeResult
                LibraryService.CONTENT_TYPE_ALBUM -> isAlbumFavorite.value = !removeResult
            }

        }
    }

    fun addToPlaylist(playlistId : String? , songIds : List<String>?) = libraryService.addToPlaylist(playlistId!! , songIds!!)

    fun followEpisode(episodeId : String) = liveData{
        var result = podcastRespo.followEpisode(episodeId)
        emit(result.data)

    }
    fun unfollowEpisode(episodeId : String) = liveData{
        var result = podcastRespo.unfollowEpisode(episodeId)
        emit(result.data)
    }

    fun downloadEpisode(episode : PodcastEpisode) = liveData{
        var result = podcastRespo.downloadEpisode(episode._id)
        if(result.data == true){
            downloadRepo.saveEpisodetoDatabase(episode)
            var path = Uri.parse(episode.filePath)
            downloadTracker.addProgressiveDownload(episode._id , path)
            return@liveData emit(true)
        }
        else return@liveData emit(result.data)
    }


    fun rechargeWallet(amount : Int , id: String , paymentVendorType : Int) = liveData {
        var result = walletUsecase.rechargeWallet(amount , id , paymentVendorType)
        emit(result)
    }





    fun getDonations(creatorId : String) = liveData {
        var result = homerepo.getDonations(creatorId).data
        emit(result)
    }

    fun getTopDonators(category : Int , size : Int) = liveData {
        var result = homerepo.getTopDonators(category , size).data
        emit(result)
    }

    fun getTopCreatorsByDonation(category : Int , size : Int) = liveData {
        var result = homerepo.getTopCreatorsByDonation(category , size).data
        emit(result)
    }

    fun getCommission() = liveData {
        var result = userRepo.getCommissionAmount().data
        emit(result)
    }

    var allRewards = MutableLiveData<List<RewardInfo>?>()
    fun getTotalGameReward() = viewModelScope.launch {
        var result = userRepo.getGameAndCommissionRewardAmount().data
         allRewards.value = result
    }

    fun transferCommissionToWallet(amount : Double) = liveData {
        var result = walletUsecase.transferCommission(amount)
        emit(result)
    }

    fun transferGamerewardToWallet(amount : Double) = liveData {
        var result = walletUsecase.transferGamePrize(amount)
        emit(result)
    }


    fun updateProductAdClick(adId : String) = liveData {
        var result = adRepo.updateAdClick(adId)
        emit(result)
    }

    suspend fun getGameAds(limit : Int = 5) = adRepo.getGameAds(limit).data

    fun getTriviaQuestions(adId : String) = liveData {
        var result = adRepo.getTriviaQuestions(adId).data
        emit(result)
    }

    var remainingTime = MutableLiveData<Long>(30000)
    fun controlTimerForGames(givenTime : Long) : CountDownTimer{
        var timer = object : CountDownTimer(givenTime , 1000) {
            override fun onTick(p0: Long) {
                remainingTime.value = p0
            }
            override fun onFinish() {
                remainingTime.value = 0
            }


        }
        timer.start()
        return timer
    }



    fun updateFcmToken(token : String) = userRepo.updateFcmToken(token)

    fun showSongOption(song : Song<String , String>){
        var info  = song.songCredit?.toMutableList()
        var album = if(song.album != null) "Album - ${song.albumName}" else null
        var a = listOf("title -  ${song.tittle}" , "artist - ${song.artistsName?.joinToString(", ")}",
            album  , "Genre - ${song.genre}" , "Release date - ${song.dateCreated?.convertoLocalDate(longMonthName = false)}", "Language - ${song.language?.joinToString(", ")}",
            "${song.monthlyStreamCount} Monthly Stream | ${song.monthlyDownloadCount} Monthly Download").toMutableList()
        info?.let { a.addAll(it) }
        a.let {
            MaterialDialog(appContext).show {
                cornerRadius(literalDp = 10f)
                title(text = "Song Info")
                positiveButton(text = "OK")
                listItems(items = it as List<CharSequence>)
            }
        }

    }





    fun showBackgroundPalete(imageUrl : String , appbar : AppBarLayout , activity : Activity){
        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    Palette.from(it).generate {
                        it?.darkMutedSwatch?.let {
                            var grDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000))
                            var grDrawable2 = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM ,  intArrayOf(it.rgb,0x00000000 , 0x00000000))

//                               binding.queueSongImageViewpager.setBackgroundResource(R.drawable.item_selected)
                            appbar.background = grDrawable
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                activity.window.statusBarColor = it.rgb
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
