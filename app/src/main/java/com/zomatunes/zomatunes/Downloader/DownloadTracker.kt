package com.zomatunes.zomatunes.Downloader


import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.zomatunes.zomatunes.ZomaTunesApplication
import com.zomatunes.zomatunes.data.db.AppDb
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.data.repo.SongRepository
import com.zomatunes.zomatunes.ui.download.DownloadViewmodel
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.DownloadAdapter
import com.zomatunes.zomatunes.util.extensions.toSong
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.novoda.downloadmanager.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.IOException
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class DownloadTracker @Inject constructor(
    @ApplicationContext var context: Context,
    var downloadManager: DownloadManager,
    var songRepo: SongRepository,
    var dataSourceFactory: DefaultDataSourceFactory,
    var drmSessionManager: DrmSessionManager<FrameworkMediaCrypto>,
    var renderersFactory: RenderersFactory,
    var appDb: AppDb
) {

    companion object{
        const val PAUSE_REASON = 1
    }

    var downloadHelper : DownloadHelper? = null

    var downloadTrackerScope = CoroutineScope(Dispatchers.IO)


    var currentDownloadList = MutableLiveData<List<Download>>(emptyList())
    var completedDownloadList = MutableLiveData<List<Download>>()
    var pausedDownloadLIst = MutableLiveData<List<Download>>()
    var failedDownloadList = MutableLiveData<List<Download>>()

    var allDownloadList = MutableLiveData<List<Download>>()


    init {
        downloadManager.addListener(DownloadTrackerDownloadListner())
    }

    fun getAllCurrentlyDownloadedItems() : List<Download>{
        var result = downloadManager.currentDownloads
        return result
    }

    fun getCurrentlyDownloadedItems(ids: List<String>) : List<Download>{
        return getAllCurrentlyDownloadedItems().filter { download -> ids.contains(download.request.id) }
    }

    fun getCurrentDownloadItem(uri: String) = getAllCurrentlyDownloadedItems().find { download -> download.request.uri == Uri.parse(uri) }


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

    suspend fun getFailedDownloadBeta() : List<Download>{
        return withContext(Dispatchers.IO){
            var downloadList = mutableListOf<Download>()
            var downloads = downloadManager.downloadIndex.getDownloads(Download.STATE_FAILED)
            while(downloads.moveToNext()){
                var d = downloads.download
                downloadList.add(d)
            }
            return@withContext downloadList
        }
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

    suspend fun getPausedDownloadBeta() : List<Download>{
       return  withContext(Dispatchers.IO){
            var downloadList = mutableListOf<Download>()
            var downloads = downloadManager.downloadIndex.getDownloads(Download.STATE_STOPPED)
            while(downloads.moveToNext()){
                var d = downloads.download
                downloadList.add(d)
            }
            return@withContext downloadList
        }
    }

    fun getDownloads(state: Int? = null) = liveData {
        var downloadList = mutableListOf<Download>()
        var downloads = if(state == null) downloadManager.downloadIndex.getDownloads()
        else downloadManager.downloadIndex.getDownloads(state)
        while(downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
//        allDownloadList.value = downloadList
        emit(downloadList)
    }

    suspend fun getDownloadsSuspended(state: Int? = null) = withContext(Dispatchers.IO) {
        var downloadList = mutableListOf<Download>()

        var downloads = if(state == null) downloadManager.downloadIndex.getDownloads()
        else downloadManager.downloadIndex.getDownloads(state)
        while(downloads.moveToNext()){
            var d = downloads.download
            downloadList.add(d)
        }
//        allDownloadList.value = downloadList
        return@withContext downloadList
    }

     fun getDownloadRequest(id : String) = runBlocking{
         Log.i("reqq" , downloadManager.downloadIndex.getDownload(id)?.request.toString())
        downloadManager.downloadIndex.getDownload(id)?.request
    }

    fun addDownload(id: String, uri: Uri , downloadtype : Int?= null){
        if(downloadtype == DownloadAdapter.DOWNLOAD_TYPE_SONG){
            downloadTrackerScope.launch {
                Log.i("reqdownload" , "req")
                songRepo.downloadSong(id).data?.let {
                    withContext(Dispatchers.Main){
                        if(!it) Toast.makeText(context, "you download this song morethan 3 times", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        val qualityParams: DefaultTrackSelector.Parameters = DefaultTrackSelector().parameters
        downloadHelper = DownloadHelper.forDash(uri , dataSourceFactory , renderersFactory , drmSessionManager , qualityParams)
        downloadHelper!!.prepare(DownloadHelperCallback(id))
    }



    fun addDownloads(streamableList: List<Streamable<String, String>> , lifecycleOwner: LifecycleOwner , downloadtype: Int? = null){
      getCompletedDownloads().observe(lifecycleOwner, Observer {
          var completedSong = it?.map { download -> download.request.uri }
          streamableList.forEach { song ->
              var uri = Uri.parse(song.mpdPath!!.replace("localhost", AdapterDiffUtil.URL))
              if (completedSong.isNullOrEmpty()) addDownload(song._id, uri , downloadtype)
              else {
                  if (!completedSong.contains(uri))
                      addDownload(song._id, uri)
              }
          }
      })
    }

    fun addbookDownload(chapters: List<Chapter>, lifecycleOwner: LifecycleOwner){
        getCompletedDownloads().observe(lifecycleOwner, Observer {
            var completedSong = it?.map { download -> download.request.uri }
            chapters.forEach { chapter ->
                var uri = Uri.parse(
                    chapter.chapterfilePath!!.replace(
                        "localhost",
                        AdapterDiffUtil.URL
                    )
                )
                if (completedSong.isNullOrEmpty()) addDownload(chapter._id, uri)
                else {
                    if (!completedSong.contains(uri))
                        addDownload(chapter._id, uri)
                }
            }
        })
    }


    fun removeDownload(id: String){
        DownloadService.sendRemoveDownload(context, MediaDownloaderService::class.java, id, false)
    }

    fun removeDownlaods(ids: List<String>){ ids.forEach { id -> removeDownload(id)  } }

    fun pauseDownload(id: String) =  DownloadService.sendSetStopReason(
        context,
        MediaDownloaderService::class.java,
        id,
        PAUSE_REASON,
        false
    )

    fun pauseDownloads(ids: List<String>) = ids.forEach { id -> pauseDownload(id) }


    fun resumeDownload(id: String){
        DownloadService.sendSetStopReason(context, MediaDownloaderService::class.java, id, Download.STOP_REASON_NONE, false)
        getFailedDownloads().value?.let {
            it.find { download -> download.request.id == id }?.let { addDownload(it.request.id, it.request.uri) }
        }
    }

    suspend fun resumeDownloads(ids: List<String>){
        ids.forEach { id -> DownloadService.sendSetStopReason(context, MediaDownloaderService::class.java, id, Download.STOP_REASON_NONE, false) }
        var allFailedDownloads = getDownloadsSuspended(Download.STATE_FAILED)
        Log.i("downloadidsids" , allFailedDownloads.size.toString())
        var failedIds = allFailedDownloads.filter { download -> ids.contains(download.request.id) }
        withContext(Dispatchers.Main) {
            failedIds.forEach { download -> addDownload(download.request.id, download.request.uri) }
        }
    }

    fun resumeAllDownload(){
        DownloadService.sendSetStopReason(context, MediaDownloaderService::class.java, null, Download.STOP_REASON_NONE, false)
        getFailedDownloads().value?.let {
            it.forEach { download -> addDownload(download.request.id, download.request.uri) }
        }

//        DownloadService.sendResumeDownloads(context , MediaDownloaderService::class.java , false)
    }

    fun deleteAllDownload() = DownloadService.sendRemoveAllDownloads(
        context,
        MediaDownloaderService::class.java,
        false
    )

    fun getNotMetRequirements() : String?{
        var result = downloadManager.notMetRequirements
        Log.i("downloadpreqnumber", result.toString())
        return when (result) {
            Requirements.NETWORK -> "No Network Connection"
            Requirements.NETWORK_UNMETERED -> "Waiting for Wifi"
            Requirements.NETWORK_UNMETERED + Requirements.NETWORK -> "Waiting for Wifi"
            else -> null
        }
    }



    // download manager using built in android download manager

    fun getDownloadManager() = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager


    fun getNovodaDownloadManager() = (context as ZomaTunesApplication).downloadManeger



    fun downloadUsingNovoda(uri: String, id: String, name: String){
        var batch = Batch.with(
            StorageRootFactory.createPrimaryStorageDownloadsDirectoryRoot(context),
            DownloadBatchIdCreator.createSanitizedFrom(id), "${name}.pdf"
        ).downloadFrom(uri).withIdentifier(DownloadFileIdCreator.createFrom(id)).apply().build()
        getNovodaDownloadManager().download(batch)
    }


    fun getAllNovodaDownload() = liveData(Dispatchers.IO) {
       var result =  getNovodaDownloadManager().allDownloadBatchStatuses
        emit(result)
    }

    fun getNovodaDownload(batchId: String) = liveData(Dispatchers.IO){
         getNovodaDownloadManager().allDownloadBatchStatuses.find { batch -> batch.downloadBatchId.rawId() == batchId }?.let {
             emit(it)
         }
    }

    fun getNovodaDownloadedFile(id: String) = liveData(Dispatchers.IO) {
       var result =  getNovodaDownloadManager()
            .getDownloadFileStatusWithMatching(
                DownloadBatchIdCreator.createSanitizedFrom(id), DownloadFileIdCreator.createFrom(
                    id
                )
            )
        emit(result)
    }



    fun createDownloadRequest(uri: String, name: String, description: String) : android.app.DownloadManager.Request{
        var downloadonWifi = PreferenceHelper.getInstance(context).getBoolean(
            "download_on_wifi",
            false
        )
        var request = android.app.DownloadManager.Request(Uri.parse(uri))
        if(downloadonWifi) request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI)
        else request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_MOBILE)
        request.setMimeType("application/pdf");
        request.setAllowedOverMetered(true)
        request.setAllowedOverRoaming(true)
        request.setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            "${name}.pdf"
        )
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(name)
        request.setDescription(description)
        request.setVisibleInDownloadsUi(false)
        return request
    }

    fun getEbookDownloadProgress(ref: Long) : Int?{
            var query = android.app.DownloadManager.Query().setFilterById(ref)
            var cursorResult = getDownloadManager().query(query)
            if(cursorResult.moveToFirst()){
                var downloadProgressIndex = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                var downloadSizeIndex = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                var progress = cursorResult.getInt(downloadProgressIndex)
                return progress
            }
            return null
    }

    suspend fun getDownloadSize(ref: Long) : Int?{
        return withContext(Dispatchers.IO){
            var query = android.app.DownloadManager.Query().setFilterById(ref)
            var cursorResult = getDownloadManager().query(query)
            if(cursorResult.moveToFirst()){
                var downloadProgressIndex = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                var downloadSizeIndex = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                var progress = cursorResult.getInt(downloadProgressIndex)
                return@withContext progress
            }
            return@withContext null
        }
    }

   suspend fun getDownloadPausedReason(ref: Long) : String?{
        return  withContext(Dispatchers.IO){
            var query = android.app.DownloadManager.Query().setFilterById(ref)
            var cursorResult = getDownloadManager().query(query)
            if(cursorResult.moveToFirst()){
                var reasonIdx = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_REASON);
                var reason  = cursorResult.getInt(reasonIdx)
                when (reason) {
                    android.app.DownloadManager.PAUSED_QUEUED_FOR_WIFI ->
                        return@withContext "Waiting for WiFi."
                    android.app.DownloadManager.PAUSED_WAITING_FOR_NETWORK ->
                        return@withContext "Waiting for connectivity."
                    android.app.DownloadManager.PAUSED_WAITING_TO_RETRY ->
                        return@withContext "Waiting to retry."
                    else -> "Paused";
                }
            }
            return@withContext null
        }
    }

    suspend fun getDownloadFailedReason(ref: Long) : String?{
        return withContext(Dispatchers.IO){
            var query = android.app.DownloadManager.Query().setFilterById(ref)
            var cursorResult = getDownloadManager().query(query)
            if(cursorResult.moveToFirst()){
                var failedReasonIdex = cursorResult.getColumnIndex(android.app.DownloadManager.COLUMN_REASON)
                var reason = cursorResult.getInt(failedReasonIdex)
                Log.i("reasonfail", reason.toString())
                when(reason){
                    android.app.DownloadManager.ERROR_CANNOT_RESUME -> return@withContext "Cannot resume"
                    android.app.DownloadManager.ERROR_INSUFFICIENT_SPACE -> return@withContext "Not Enough space"
                    android.app.DownloadManager.ERROR_HTTP_DATA_ERROR -> return@withContext "Http error"
                    else -> return@withContext "Unable to download"
                }
            }
            return@withContext null
        }
    }

    fun deleteEbookFile(id: String){
        getNovodaDownloadManager().delete(DownloadBatchIdCreator.createSanitizedFrom(id))
    }

    fun addEbookDownload(id: String, uri: String, eBookName: String){
        downloadUsingNovoda(uri, id, eBookName)
    }



    suspend fun calculateProgress(download: DbDownloadInfo, owner: LifecycleOwner, notMetCallback: (() -> Unit)?, progressUpdateCallback: (progress: Int) -> Unit,
        progressCompleteCallback: () -> Unit){

        var downloadProgress = MutableLiveData(0)
        var prevProgress = 0
        withContext(Dispatchers.Main){
            downloadProgress.observe(owner, Observer { progress ->
                if (progress > prevProgress && (download.state != "PAUSED" || download.state != "FALIED")) {
                    prevProgress = progress
                    progressUpdateCallback(prevProgress)
                }
                if (progress == 100) {
                    progressCompleteCallback()
//                    getOngoingDownloads()
                }
            })
        }

        var songResult : List<Song<String, String>> = emptyList()
        if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_ALBUM) songResult = appDb.songDao.getSongsByAlbumId(
            download.contentId
        ).toSong()
        else if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG) songResult = appDb.songDao.getSongs(
            download.contentId
        ).toSong()

        var songUriList = songResult.map { song -> Uri.parse(
            song.songFilePath!!.replace("localhost", AdapterDiffUtil.URL))
        }
        withContext(Dispatchers.Main){
            getOngoingDownloads().observe(owner, Observer { downloadList ->
                var currentlyDownloadedSongs = downloadList.filter { download ->
                    songUriList.contains(
                        download.request.uri
                    )
                }
                if (currentlyDownloadedSongs.isNullOrEmpty()) {
//                    progressCompleteCallback()
                } else {
                    getNotMetRequirements()?.let {
                        if (notMetCallback != null) {
                            notMetCallback()
                        }
                        return@Observer
                    }
                    fixedRateTimer("ALBUM_DONWLOAD_PROGRESS", false, 0, 1000) {
                        if (download.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG) {
                            Log.i("downloadpr", "download song  started")
                            var progress =
                                currentlyDownloadedSongs.first().percentDownloaded.toInt()
                            downloadProgress.postValue((progress))
                            if (progress == 100 || !getNotMetRequirements().isNullOrEmpty()) cancel()
                        } else {
                            Log.i("downloadpr", "download song  started")
                            var progressList: List<Float> =
                                currentlyDownloadedSongs.map { download -> download.percentDownloaded }
                            var netProgress = (progressList.reduce { acc, fl -> acc.plus(fl) }.div(
                                progressList.size
                            )).toInt()
                            downloadProgress.postValue(netProgress)
                            if (prevProgress == 100 || !getNotMetRequirements().isNullOrEmpty()) cancel()
                        }
                    }
                }
            })
        }

    }




    inner class DownloadHelperCallback(var id: String) : DownloadHelper.Callback{
        override fun onPrepared(helper: DownloadHelper?) {
            var trackKeys = mutableListOf<TrackKey>()
            for (i in 0 until helper!!.periodCount) {
                val trackGroups = helper.getTrackGroups(i)

                for (j in 0 until trackGroups.length) {
                    val trackGroup = trackGroups[j]
                    for (k in 0 until trackGroup.length) {
                        val track: Format = trackGroup.getFormat(k)
                        Log.i("trackmime", track.sampleMimeType)
                        trackKeys.add(TrackKey(trackGroups, trackGroup, track))
                    }
                }
            }

            var downloadQualityList = mutableListOf<String>()
            for (i in 0 until trackKeys.size) {
                val trackKey: TrackKey = trackKeys[i]
                val bitrate = trackKey.trackFormat.bitrate.toLong()
                trackKey.trackFormat.maxInputSize
//                val getInBytes: Long = bitrate * videoDurationInSeconds / 8
//                val getInMb: String = formatFileSize(getInBytes)
                val downloadSize = trackKey.trackFormat.bitrate.toString()

                Log.i("tracksize", downloadSize)
                downloadQualityList.add(downloadSize)
            }

            var preference = PreferenceHelper.getInstance(context)
            var qualitySelector = preference.get("download_quality", "128")
            startAdaptiveDownload(qualitySelector , trackKeys.sortedBy { trackKey -> trackKey.trackFormat.bitrate } , helper , id)

//            var request = helper?.getDownloadRequest(id, null)
//            DownloadService.sendAddDownload(context, MediaDownloaderService::class.java, request, false)
//            helper?.release()
        }

        override fun onPrepareError(helper: DownloadHelper?, e: IOException?) {
            Log.i("downloaderror", "error" + e?.message)
            Toast.makeText(context , e?.message , Toast.LENGTH_LONG).show()
        }
    }

    fun startAdaptiveDownload(qualitySelector : String , trackkeys : List<TrackKey> , helper: DownloadHelper , id : String){
        var trackKey =  when(qualitySelector){
            "96" -> trackkeys[0]
            "128" -> trackkeys[1]
            "256" -> trackkeys[2]
            else -> trackkeys[1]
        }

        Log.i("trackavailable", trackkeys.map { trackkey -> trackkey.trackFormat.bitrate }.toString())
        val qualityParams: DefaultTrackSelector.Parameters = DefaultTrackSelector().parameters.buildUpon()
//                .setMaxVideoSize(trackKey.trackFormat().width, trackKey.getTrackFormat().height)
                .setMaxAudioBitrate(trackKey.trackFormat.bitrate)
                .build()
        for (periodIndex in 0 until helper.periodCount) {
            helper.clearTrackSelections(periodIndex)
            val mappedTrackInfo = helper.getMappedTrackInfo( /* periodIndex= */periodIndex)

            Log.i("trackstreamkey" , mappedTrackInfo.rendererCount.toString())
            for (i in 0 until mappedTrackInfo.rendererCount) {
                helper.addTrackSelection(periodIndex , qualityParams)
            }
        }



        val downloadRequest = helper.getDownloadRequest(id  ,null)
        DownloadService.sendAddDownload(context, MediaDownloaderService::class.java, downloadRequest, false)

        helper?.release()
        if (downloadRequest.streamKeys.isEmpty()) {
            // All tracks were deselected in the dialog. Don't start the download.
            return
        }
    }


    fun formatFileSize(size: Long): String {
        val b = size.toDouble()
        val k = size / 1024.0
        val m = size / 1024.0 / 1024.0
        val g = size / 1024.0 / 1024.0 / 1024.0
        val t = size / 1024.0 / 1024.0 / 1024.0 / 1024.0

        val dec = DecimalFormat("0.00")
        var hrSize = when {
            t > 1 -> "${dec.format(t)} TB"

            g > 1 -> "${dec.format(g)} GB"

            m > 1 -> "${dec.format(m)} MB"

            k > 1 -> "${dec.format(k)} KB"

            else -> "${dec.format(b)} Bytes"

        }
        return hrSize
    }


    inner class DownloadTrackerDownloadListner() : DownloadManager.Listener{
        override fun onDownloadChanged(downloadManager: DownloadManager?, download: Download?) {
            currentDownloadList.value = downloadManager?.currentDownloads
            when (download?.state) {
                Download.STATE_FAILED -> {
                    getDownloads()
                    getOngoingDownloads()
                }
                Download.STATE_STOPPED -> {
                    getDownloads()
                    getOngoingDownloads()
                }
            }
        }

        override fun onInitialized(downloadManager: DownloadManager?) {

        }

        override fun onRequirementsStateChanged(downloadManager: DownloadManager?, requirements: Requirements?, notMetRequirements: Int) {
            if(notMetRequirements == 0){
                DownloadService.startForeground(context, MediaDownloaderService::class.java)
                getOngoingDownloads()
            }
        }
    }


}