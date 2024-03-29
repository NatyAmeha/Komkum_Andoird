package com.komkum.komkum.util.adaper

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.*
import com.komkum.komkum.databinding.DownloadListItemBinding
import com.komkum.komkum.ui.download.DownloadViewmodel
import com.komkum.komkum.util.extensions.toSong
import com.google.android.exoplayer2.offline.Download
import com.novoda.downloadmanager.DownloadBatchStatus
import com.komkum.komkum.ui.download.DownloadViewpagerAdapter
import com.komkum.komkum.ui.download.TimerTracker
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.extensions.toEpisode
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class DownloadAdapter(var info: RecyclerViewHelper<DbDownloadInfo>, var tracker: DownloadTracker, var appDb: AppDb ,
var timerTracker: TimerTracker)
    : ListAdapter<DbDownloadInfo, DownloadAdapter.DownloadViewholder>(DownloadDiffUtil()) {

    companion object{
        //state
        const val COMPLETED = 0
        const val  RUNNING = 1
        const val FAILED = 2
        const val PAUSED = 3

        //download action
        const val DOWNLOAD_ACTION_PLAY = 10000
        const val DOWNLOAD_ACTION_DELETE = 20000
        const val DOWNLOAD_ACTION_PAUSE = 30000
        const val DOWNLOAD_ACTION_RESUME = 40000

        //download type
        const val DOWNLOAD_TYPE_SONG = 0
        const val DOWNLOAD_TYPE_ALBUM = 1
        const val DOWNLOAD_TYPE_PLAYLIST = 2
        const val DOWNLOAD_TYPE_AUDIOBOOK  = 3
        const val DOWNLOAD_TYPE_EBOOK = 4
    }

    lateinit var mainActivity: MainActivity
    var downloadTrackerScope = CoroutineScope(Dispatchers.IO)
    var allDownloadList : MutableList<Download> = mutableListOf()

    var timer : Timer? = null
    var timers : MutableList<Timer> = mutableListOf()

    val DOWNLOAD_COMPLETE_MENU_OPTIONS = listOf("Delete")
    val DOWNLOAD_RUNNGIN_AND_WAITING_MENU_OPTIONS = listOf("Pause" , "Delete")
    val  DOWNLOAD_PAUSED_AND_FAILED_MENU_OPTIONS = listOf("Resume" , "Delete")

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        var context  = recyclerView.context
//        mainActivity = context as MainActivity

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewholder {
        var binding =
            DownloadListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadViewholder(binding)
    }


    override fun onBindViewHolder(holder: DownloadViewholder, position: Int) {
        holder.bind(getItem(position))
    }



    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Log.i("downloadcurrentdetachcd" , "big detach called")
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewDetachedFromWindow(holder: DownloadViewholder) {
        Log.i("downloadcurrentdetach" , "big detach called")
        downloadTrackerScope.cancel()

        timers.forEach { timer -> timer.cancel() }
        super.onViewDetachedFromWindow(holder)
    }




    inner class DownloadViewholder(var binding: DownloadListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(download: DbDownloadInfo) {
            binding.tittleTextview.text = download.name


            with(binding.root) {
                setOnClickListener {
                    info.interactionListener?.onItemClick(download, adapterPosition,null)
                }
            }

            //caclulate progress
            downloadTrackerScope.launch {
                var songResult : List<Song<String,String>> = emptyList()
                when (download.type) {
                    DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST -> {
                        songResult = appDb.playlistSongJoinDao.getPlaylistSongs(download.contentId).toSong()
                        var imageList = songResult.distinctBy { song -> song.thumbnailPath }.map { song -> song.thumbnailPath!!.replace("localhost" , AdapterDiffUtil.URL) }
                        withContext(Dispatchers.Main) {
                            binding.thumbnailImageview.loadImage(imageList)
                            binding.sizeTextview.text = "${songResult.size} songs"
                            binding.subtittleTextview.text = "Playlist"
                            calculateProgress(download, songResult , binding.root.context)
                        }
                    }
                    DownloadViewmodel.DOWNLOAD_TYPE_ALBUM -> {
                        songResult = appDb.albumSongJoinDao.getAlbumSongs(download.contentId)?.toSong() ?: emptyList()
                        withContext(Dispatchers.Main) {
                            binding.subtittleTextview.text = "Album"
                            binding.sizeTextview.text = "${songResult.size} songs"
                            binding.thumbnailImageview.loadImage(listOf(download.imagePath) , R.drawable.album_placeholder)
                            calculateProgress(download, songResult , binding.root.context)
                        }
                    }

                    DownloadViewmodel.DOWNLOAD_TYPE_EPISODE ->{
                        withContext(Dispatchers.Main){
                            binding.subtittleTextview.text = download.description
                            binding.thumbnailImageview.loadImage(listOf(download.imagePath.replace("localhost" , AdapterDiffUtil.URL)) , R.drawable.ic_baseline_mic_24)
                            binding.sizeTextview.text = "Podcast"
                            appDb.episodeDao.getEpisode(download.contentId)?.toEpisode()?.let {
                                calculateProgress(download , listOf(it) , binding.root.context , false)
                            }
                        }

                    }
//                    DownloadViewmodel.DOWNLOAD_TYPE_SONG -> {
//                        songResult = appDb.songDao.getSongs(download.contentId).toSong()
//                        withContext(Dispatchers.Main) {
//                            calculateProgress(download, songResult)
//                        }
//                    }
                    DownloadViewmodel.DOWNLOAD_TYPE_AUDIOBOOK -> {

                        var chapterList = appDb.chapterDao.getBookChapters(download.contentId).map { chapterDbInfo ->
                            Chapter(chapterDbInfo._id , songFilePath = chapterDbInfo.chapterfilePath , mpdPath = chapterDbInfo.mpdPath
                                .replace("index.m3u8" , "hls_audio_64k_v4.m3u8"))
                        }
                        withContext(Dispatchers.Main){
                            binding.subtittleTextview.text = download.description
                            binding.sizeTextview.text = "${chapterList.size} chapters"
                            binding.thumbnailImageview.loadImage(listOf(download.imagePath) , R.drawable.audiobook_placeholder)
                            calculateProgress(download , chapterList , binding.root.context)
                        }
                    }
                    DownloadViewmodel.DOWNLOAD_TYPE_EBOOK -> {
                        appDb.ebookDao.getBook(download.contentId)?.let {
                            withContext(Dispatchers.Main){
                                binding.thumbnailImageview.loadImage(listOf(download.imagePath) , R.drawable.book_placeholder)
                                binding.subtittleTextview.text = download.description
                                binding.sizeTextview.text = "Ebook"
                                calculatePr(download)
                            }
                        }
                    }
                }
            }
        }
        fun calculatePr(downloadInfo: DbDownloadInfo){
            var downloadProgress = MutableLiveData<Int>(0)
            var prevProgress = -1
            var timerStopFlag = false

            downloadProgress.observe(info.owner!!, Observer { progress ->
                if(progress > prevProgress ){
                    prevProgress = progress
                    timerStopFlag = false
                    binding.downloadStateTextview.text = "${prevProgress}% downloading"
                }
//                else if(progress == 0) binding.downloadStateTextview.text = "waiting..."

                if (progress == 100){
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    binding.downloadStateTextview.text = "completed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                    activateItem()
                    timerStopFlag = true
                }
            })

            info.downloadTracker!!.getAllNovodaDownload().observe(info.owner!! , Observer{
                it.find { batch -> batch.downloadBatchId.rawId() == downloadInfo.contentId }?.let {
                    var status = it.status()
                    when(status){
                        DownloadBatchStatus.Status.PAUSED -> {
                            menuOptionsClickHandle(downloadInfo , FAILED)
                            inactiveItem()
                            timerStopFlag = true
                            binding.downloadStateTextview.text = "Paused"
                            binding.downloadStateIconImageview.setImageResource(R.drawable.exo_controls_pause)
                        }
                        DownloadBatchStatus.Status.DOWNLOADED -> {
                            menuOptionsClickHandle(downloadInfo , COMPLETED)
                            activateItem()
                            var size = it.bytesTotalSize().div(1024)
                            binding.downloadStateTextview.text ="${size.div(1024)} Mb"
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                        }
                        DownloadBatchStatus.Status.ERROR ->{
                            timerStopFlag = true
                            inactiveItem()
                            menuOptionsClickHandle(downloadInfo , FAILED)
                            var reason = it.downloadError()?.message()
                            binding.downloadStateTextview.text = reason
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                        }
                        DownloadBatchStatus.Status.WAITING_FOR_NETWORK ->{
                            menuOptionsClickHandle(downloadInfo , RUNNING)
                            inactiveItem()
                            binding.downloadStateTextview.text = "waiting for network"
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                        }
                        DownloadBatchStatus.Status.QUEUED -> {
                            menuOptionsClickHandle(downloadInfo , RUNNING)
                            inactiveItem()
                            binding.downloadStateTextview.text = "download queued"
                        }
                        DownloadBatchStatus.Status.DOWNLOADING ->{
                            menuOptionsClickHandle(downloadInfo , RUNNING)
                            inactiveItem()
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_file_download_black_24dp)

                            fixedRateTimer("EBOOK_DOWNLOAD", false, 0, 3000) {
                                if( timerStopFlag) cancel()
                                var progress = it.percentageDownloaded()
                                downloadProgress.postValue(progress)
                                if(progress == 100){
                                    timerStopFlag = true
                                    cancel()
                                }
                            }
                        }
                        else -> {}
                    }
                }
            })
        }


        suspend fun calculateEbookDownloadProgress(downloadInfo: DbDownloadInfo ,status : Int , ref : Long){
            var timerStopFlag = false
            when(status){
                DownloadManager.STATUS_PAUSED -> {
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    timerStopFlag = true
                    var reason = info.downloadTracker!!.getDownloadPausedReason(ref) ?: "Paused Unknown"
                    binding.downloadStateTextview.text = reason
                    binding.downloadStateIconImageview.setImageResource(R.drawable.exo_controls_pause)
                }
                DownloadManager.STATUS_FAILED ->{
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    var reason = info.downloadTracker!!.getDownloadFailedReason(ref) ?: "Failed"
                    binding.downloadStateTextview.text = reason
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                }
                DownloadManager.STATUS_PENDING ->{
                    var reason  = info.downloadTracker!!.getDownloadPausedReason(ref) ?:"unkown"
                    Log.i("pendingreason" , reason)
                    binding.downloadStateTextview.text = "waiting for download"
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , RUNNING)
                }

                DownloadManager.STATUS_SUCCESSFUL ->{
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    var size = info.downloadTracker!!.getDownloadSize(ref)
                    binding.downloadStateTextview.text ="${size?.div(1024)} Kb" ?: "completed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                }
                DownloadManager.STATUS_RUNNING ->{
                    var downloadProgress = MutableLiveData<Int>(0)
                    var prevProgress = -1

                    downloadProgress.observe(info.owner!!, Observer { progress ->
                        if(progress > prevProgress ){
                            prevProgress = progress
                            timerStopFlag = false
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_file_download_black_24dp)
                            binding.downloadStateTextview.text = "${prevProgress}% downloading"
                        }

                        if (progress == 100){
                            menuOptionsClickHandle(downloadInfo , COMPLETED)
                            binding.downloadStateTextview.text = "completed"
                            binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                            timerStopFlag = true
                        }

//                        fixedRateTimer("EBOOK_DOWNLOAD", false, 0, 3000) {
//                            if( timerStopFlag) cancel()
//
////                             info.downloadTracker!!.getEbookDownloadProgress(ref)
//
//                            Log.i("downloadbookpr", "download progress $progress")
//                            downloadProgress.postValue(progress)
//                            if(progress == 100) cancel()
//                        }
                    })
                }
            }
        }

        fun calculateProgress(downloadInfo: DbDownloadInfo, result: List<Streamable<String,String>> , context : Context , isAdaptiveStream : Boolean = true) {
            var downloadProgress = MutableLiveData<Int>()
            var prevProgress = -1
            var timerStopFlag = false

            downloadProgress.observe(info.owner!!, Observer { pr ->
                if(pr > prevProgress ){
                    prevProgress = pr
                    timerStopFlag = false
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_file_download_black_24dp)
                    binding.downloadStateTextview.text = "${prevProgress}% downloading"
                }
//                else if(progress == 0) binding.downloadStateTextview.text = "waiting..."

                if (pr == 100){
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    activateItem()
                    binding.downloadStateTextview.text = "completed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                    activateItem()
                    timerStopFlag = true
                }
            })

            var uriList = if(isAdaptiveStream){
                var preference = PreferenceHelper.getInstance(context)
                var qualitySelector = preference.get("download_quality", "64")
                when(qualitySelector){
                    "64" ->  result.map { streamable -> Uri.parse(streamable.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8")) }
                    "160" -> result.map { streamable -> Uri.parse(streamable.mpdPath?.replace("index.m3u8" , "hls_audio_160k_v4.m3u8")) }
                    else -> result.map { streamable -> Uri.parse(streamable.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8")) }
                }
            }else{
                result.map { streamable -> Uri.parse(streamable.songFilePath) }
            }


            tracker.getDownloads().observe(info.owner!! , Observer{allDownloads ->
                allDownloadList = allDownloads

                var completedDownloads = allDownloadList.filter { download -> download.state == Download.STATE_COMPLETED }
                    .filter { download -> uriList.contains(download.request.uri) }
//                Log.i("compdown" , completedDownloads.size.toString()+  " " + uriList.size.toString())
                if(completedDownloads.size >= uriList.size){

                    if(completedDownloads.isNotEmpty()){
                        var size = completedDownloads.map { download -> download.bytesDownloaded }.reduce { acc, download -> acc.plus(download) }
                        binding.downloadStateTextview.text = "${ (size / (1024 * 1024))} Mb"
                        binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                    }
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    activateItem()
                    return@Observer
                }


                var failedDownloads = allDownloadList.filter { download -> download.state == Download.STATE_FAILED }
                    .filter { download -> uriList.contains(download.request.uri) }
                if(failedDownloads.isNotEmpty()){
                    var contentLength = failedDownloads.map { download -> download.contentLength }.reduce { acc, download -> acc.plus(download) }
                    binding.downloadStateTextview.text = "Failed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    inactiveItem()
                    return@Observer
                }
            })

            tracker.getOngoingDownloads().observe(info.owner!!, Observer { downloadList ->
                var pausedDownload = downloadList.filter { download -> download.state == Download.STATE_STOPPED }
                    .filter { download -> uriList.contains(download.request.uri) }
                if(pausedDownload.isNotEmpty()){
                    binding.downloadStateTextview.text = "Paused"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.exo_controls_pause)
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    inactiveItem()
                    timerStopFlag = true
                    return@Observer
                }

                var currentlyDownloadedSongs = downloadList.filter { download -> download.state != Download.STATE_STOPPED}
                    .filter { download -> uriList.contains(download.request.uri) }
                Log.i("downloadcurrent", downloadList.size.toString())

                if (currentlyDownloadedSongs.isNullOrEmpty()) {
//                    timerStopFlag = true
                    return@Observer
                }
                else {
                    menuOptionsClickHandle(downloadInfo , RUNNING)
                    inactiveItem()
                    tracker.getNotMetRequirements()?.let {
                        Log.i("downloadprwithreq", it+ "requested")
                        var contentLength = downloadList.map { download -> download.contentLength }.reduce { acc, download -> acc.plus(download) }
                        binding.downloadStateTextview.text = tracker.getNotMetRequirements()
                        binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                        timerStopFlag = true

                        return@Observer
                    }
                    var progress: Int

                    timer = fixedRateTimer("ALBUM_DONWLOAD_PROGRESS", false, 0, 1000) {
                        if( timerStopFlag) cancel()

                        var progressList = currentlyDownloadedSongs.map { download -> download.percentDownloaded }
//                        var downloadPercentage = allDownloadList
//                            .filter { download -> uriList.contains(download.request.uri) }
//                            .map { download -> download.percentDownloaded }

                        progress = (progressList.reduce { acc, fl -> acc.plus(fl) }.div(progressList.size)).toInt()

                        Log.i("downloadpr", "download progress $progress   , ${progressList.size}")

                        downloadProgress.postValue(progress)
                        if(progress == 100) cancel()
                        this.scheduledExecutionTime()
                    }
                    timers.add(timer!!)
                    timerTracker.onAdd(timer!!)
                }
            })
        }



        fun inactiveItem(){
            binding.downloadContainer.alpha = 0.4f
        }

        fun activateItem(){
            binding.downloadContainer.alpha = 1.0f
        }



        fun menuOptionsClickHandle(downloadInfo : DbDownloadInfo, state : Int){
            binding.downloadOptionsImageview.setOnClickListener {
                 MaterialDialog(binding.root.context , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    cancelOnTouchOutside(true)
                    cornerRadius(literalDp = 13f)
                   when(state){
                       COMPLETED ->{
                           listItems(items = DOWNLOAD_COMPLETE_MENU_OPTIONS){ _, index, item ->
                               if(item == "Delete"){
                                   info.interactionListener!!.onItemClick(downloadInfo, DOWNLOAD_ACTION_DELETE,null)
                                   this@DownloadAdapter.notifyItemRemoved(adapterPosition)
                               }
                           }
                       }
                       RUNNING ->{
                           listItems(items = DOWNLOAD_RUNNGIN_AND_WAITING_MENU_OPTIONS){ _, index, item ->
                              if(item == "Pause"){
                                  timer?.cancel()
                                  info.interactionListener!!.onItemClick(downloadInfo, DOWNLOAD_ACTION_PAUSE,null)
                                  if(downloadInfo.type == DownloadViewmodel.DOWNLOAD_TYPE_EBOOK) notifyDataSetChanged()
                              }
                              else if(item == "Delete"){
                                  timer?.cancel()
                                  info.interactionListener!!.onItemClick(downloadInfo, DOWNLOAD_ACTION_DELETE,null)
                              }
                           }
                       }
                       FAILED ->{
                           listItems(items = DOWNLOAD_PAUSED_AND_FAILED_MENU_OPTIONS){ _, index, item ->
                              if(item == "Resume"){
                                  info.interactionListener!!.onItemClick(downloadInfo, DOWNLOAD_ACTION_RESUME,null)
                                  if(downloadInfo.type == DownloadViewmodel.DOWNLOAD_TYPE_EBOOK) notifyDataSetChanged()
                              }
                              else if(item == "Delete"){
                                  timer?.cancel()
                                  info.interactionListener!!.onItemClick(downloadInfo, DOWNLOAD_ACTION_DELETE,null)
                                  this@DownloadAdapter.notifyItemRemoved(adapterPosition)
                              }
                           }
                       }
                   }

                }

            }

        }

    }

}

  class AdapterObserver : RecyclerView.AdapterDataObserver(){

}