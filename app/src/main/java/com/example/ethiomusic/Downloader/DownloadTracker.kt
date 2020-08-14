package com.example.ethiomusic.Downloader


import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.data.repo.SongRepository
import com.example.ethiomusic.ui.download.DownloadViewmodel
import com.example.ethiomusic.util.PreferenceHelper
import com.example.ethiomusic.util.extensions.toSong
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class DownloadTracker @Inject constructor(var context: Context, var downloadManager: DownloadManager , var songRepo : SongRepository,
                                          var dataSourceFactory: DefaultDataSourceFactory , var appDb: AppDb) {

    companion object{
        const val PAUSE_REASON = 1
    }

    var downloadHelper : DownloadHelper? = null

    var downloadTrackerScope = CoroutineScope(Dispatchers.IO)


    var currentDownloadList = MutableLiveData<List<Download>>(emptyList())
    var completedDownloadList = MutableLiveData<List<Download>>()
    var pausedDownloadLIst = MutableLiveData<List<Download>>()
    var failedDownloadList = MutableLiveData<List<Download>>()


    init {
        downloadManager.addListener(DownloadTrackerDownloadListner())
    }

    fun getCurrentlyDownloadedItems() : List<Download>{
        var result = downloadManager.currentDownloads
        return result
    }

    fun getCurrentDownloadItem(uri : String) = getCurrentlyDownloadedItems().find { download -> download.request.uri == Uri.parse(uri) }


    fun getOngoingDownloads() : LiveData<List<Download>>{
         currentDownloadList.postValue(downloadManager.currentDownloads)
        return currentDownloadList
    }


    fun getCompletedDownloads() : LiveData<List<Download>>{
        var downloadList = mutableListOf<Download>()
        var downloads = downloadManager.downloadIndex.getDownloads(Download.STATE_COMPLETED)
        while (downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
        completedDownloadList.postValue(downloadList)
        return completedDownloadList
    }

    fun getFailedDownloads() : LiveData<List<Download>>{
        var downloadList = mutableListOf<Download>()
        var downloads = downloadManager.downloadIndex.getDownloads(Download.STATE_FAILED)
        while(downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
        failedDownloadList.postValue(downloadList)
        return failedDownloadList
    }

    fun getPausedDownlads() : LiveData<List<Download>>{
        var downloadList = mutableListOf<Download>()
        var downloads = downloadManager.downloadIndex.getDownloads(Download.STATE_STOPPED)
        while(downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
        pausedDownloadLIst.value = downloadList
        return pausedDownloadLIst
    }
    fun getDownloads(state : Int? = null) = liveData {
        var downloadList = mutableListOf<Download>()
        var downloads = downloadManager.downloadIndex.getDownloads()
        while(downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
        emit(downloadList)
    }

    fun addDownload(id : String , uri: Uri){
        downloadTrackerScope.launch {
             songRepo.downloadSong(id).data?.let {
                 withContext(Dispatchers.Main){
                     if(!it) Toast.makeText(context , "you download this song morethan 3 times" , Toast.LENGTH_LONG).show()
                 }
             }
        }
        downloadHelper = DownloadHelper.forProgressive(uri)
        downloadHelper!!.prepare(DownloadHelperCallback(id))
    }

    fun addDownloads(songs : List<Song<String , String>> , lifecycleOwner: LifecycleOwner){
      getCompletedDownloads().observe(lifecycleOwner , Observer{
            var completedSong  = it?.map { download -> download.request.uri }
          songs.forEach { song ->
              var uri = Uri.parse(song.songFilePath!!.replace("localhost" , "192.168.43.166"))
              if(completedSong.isNullOrEmpty()) addDownload(song._id , uri)
              else{
                  if(!completedSong.contains(uri))
                      addDownload(song._id , uri)
              }
          }
        })

    }


    fun removeDownload(id : String){
        DownloadService.sendRemoveDownload(context , MediaDownloaderService::class.java , id , false)
    }

    fun removeDownlaods(ids : List<String>){ ids.forEach { id -> removeDownload(id)  } }

    fun pauseDownload(id : String) =  DownloadService.sendSetStopReason(context , MediaDownloaderService::class.java , id , PAUSE_REASON , false)

    fun pauseDownloads(ids : List<String>) = ids.forEach { id -> pauseDownload(id) }


    fun resumeDownload(id : String){
        DownloadService.sendSetStopReason(context , MediaDownloaderService::class.java , id , Download.STOP_REASON_NONE , false)
        getFailedDownloads().value?.let {
            it.find { download -> download.request.id == id }?.let { addDownload(it.request.id , it.request.uri) }
        }
    }

    fun resumeDownloads(ids : List<String>){
        ids.forEach { id -> resumeDownload(id)}
    }

    fun resumeAllDownload(){
        DownloadService.sendSetStopReason(context , MediaDownloaderService::class.java , null , Download.STOP_REASON_NONE ,false)
        getFailedDownloads().value?.let {
            it.forEach { download -> addDownload(download.request.id , download.request.uri) }
        }

//        DownloadService.sendResumeDownloads(context , MediaDownloaderService::class.java , false)
    }

    fun deleteAllDownload() = DownloadService.sendRemoveAllDownloads(context , MediaDownloaderService::class.java , false)

    fun getNotMetRequirements() : String?{
        var result = downloadManager.notMetRequirements
        Log.i("downloadpreqnumber", result.toString())
        return when (result) {
            Requirements.NETWORK -> "No Network Connection"
            Requirements.NETWORK_UNMETERED ->  "Waiting for Wifi"
            Requirements.NETWORK_UNMETERED + Requirements.NETWORK -> "Waiting for Wifi"
            else -> null
        }
    }


    suspend fun calculateProgress(download: DbDownloadInfo, owner: LifecycleOwner, notMetCallback: (() -> Unit)?, progressUpdateCallback: (progress : Int) -> Unit, progressCompleteCallback: () -> Unit){
        var downloadProgress = MutableLiveData(0)
        var prevProgress = 0
        withContext(Dispatchers.Main){
            downloadProgress.observe(owner, Observer { progress ->
                if(progress > prevProgress && (download.state != "PAUSED" || download.state != "FALIED")){
                    prevProgress = progress
                    progressUpdateCallback(prevProgress)
                }
                if (progress == 100){
                    progressCompleteCallback()
//                    getOngoingDownloads()
                }
            })
        }

        var songResult : List<Song<String,String>> = emptyList()
        if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_ALBUM) songResult = appDb.songDao.getSongsByAlbumId(download.contentId).toSong()
        else if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG) songResult = appDb.songDao.getSongs(download.contentId).toSong()

        var songUriList = songResult.map { song -> Uri.parse(song.songFilePath!!.replace("localhost", "192.168.43.166")) }
        withContext(Dispatchers.Main){
            getOngoingDownloads().observe(owner , Observer { downloadList ->
                var currentlyDownloadedSongs = downloadList.filter { download -> songUriList.contains(download.request.uri) }
                if (currentlyDownloadedSongs.isNullOrEmpty()) {
//                    progressCompleteCallback()
                }else{
                    getNotMetRequirements()?.let {
                        if (notMetCallback != null) {
                            notMetCallback()
                        }
                        return@Observer
                    }
                    fixedRateTimer("ALBUM_DONWLOAD_PROGRESS", false, 0, 1000){
                        if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG){
                            Log.i("downloadpr", "download song  started")
                            var progress = currentlyDownloadedSongs.first().percentDownloaded.toInt()
                            downloadProgress.postValue((progress))
                            if(progress == 100 || !getNotMetRequirements().isNullOrEmpty()) cancel()
                        }
                        else{
                            Log.i("downloadpr", "download song  started")
                            var progressList : List<Float> = currentlyDownloadedSongs.map { download -> download.percentDownloaded }
                            var netProgress = (progressList.reduce { acc, fl -> acc.plus(fl) }.div(progressList.size)).toInt()
                            downloadProgress.postValue(netProgress)
                            if (prevProgress == 100 || !getNotMetRequirements().isNullOrEmpty() ) cancel()
                        }
                    }
                }
            })
        }

    }

    inner class DownloadHelperCallback(var id : String) : DownloadHelper.Callback{
        override fun onPrepared(helper: DownloadHelper?) {
//            var trackKey = mutableListOf<TrackK>()
//            var mappedTrack = helper?.getMappedTrackInfo(0)
//            if(helper?.periodCount == 0){
//                // donwload the entire stream
//                return
//            }
//

//            val streamQualityValue = PreferenceHelper.getInstance(context).getString("stream_quality" , "Auto")!!
//            var trackSelector = DefaultTrackSelector()
//
//            when(streamQualityValue){
//                "96" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(96000).build()}
//                "128" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(128000).build()}
//                "256" -> {trackSelector.buildUponParameters().setMaxAudioBitrate(256000).build()}
//                else -> {}
//            }
//            for (periodIndex in 0..helper!!.periodCount){
//                helper.clearTrackSelections(periodIndex)
//                helper.addTrackSelection(periodIndex , trackSelector.parameters)
//            }

            var request = helper?.getDownloadRequest(id , null)
            DownloadService.sendAddDownload(context , MediaDownloaderService::class.java , request , false)
            helper?.release()
        }

        override fun onPrepareError(helper: DownloadHelper?, e: IOException?) {
            Log.i("downloaderror" , "error"+e?.message)
        }
    }



    inner class DownloadTrackerDownloadListner() : DownloadManager.Listener{
        override fun onDownloadChanged(downloadManager: DownloadManager?, download: Download?) {
            currentDownloadList.value = downloadManager?.currentDownloads
            when (download?.state) {
                Download.STATE_FAILED -> {
                    Log.i("downloadfailed" , "download failed")
                    getDownloads()
                }
            }
        }

        override fun onInitialized(downloadManager: DownloadManager?) {

        }

        override fun onRequirementsStateChanged(downloadManager: DownloadManager?, requirements: Requirements?, notMetRequirements: Int) {
            if(notMetRequirements == 0){
                DownloadService.startForeground(context , MediaDownloaderService::class.java)
                getOngoingDownloads()
            }
        }
    }


}