package com.example.ethiomusic.util.adaper

import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.db.AppDb
import com.example.ethiomusic.data.model.DbDownloadInfo
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.DownloadListItemBinding
import com.example.ethiomusic.ui.download.DownloadViewmodel
import com.example.ethiomusic.util.extensions.toSong
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.util.Util
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import java.io.File
import kotlin.concurrent.fixedRateTimer

class DownloadAdapter(var info: RecyclerViewHelper<DbDownloadInfo>, var tracker: DownloadTracker, var appDb: AppDb)
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
    }

    lateinit var mainActivity: MainActivity
    var downloadTrackerScope = CoroutineScope(Dispatchers.IO)
    var baseDownloadNotificationId = 0

    val DOWNLOAD_COMPLETE_MENU_OPTIONS = listOf("Play" , "Delete")
    val DOWNLOAD_RUNNGIN_AND_WAITING_MENU_OPTIONS = listOf("Pause" , "Delete")
    val  DOWNLOAD_PAUSED_AND_FAILED_MENU_OPTIONS = listOf("Resume" , "Delete")

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        var context  = recyclerView.context
        mainActivity = context as MainActivity

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewholder {
        var binding =
            DownloadListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadViewholder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DownloadViewholder, position: Int) {
        holder.bind(getItem(position))
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        downloadTrackerScope.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class DownloadViewholder(var binding: DownloadListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(download: DbDownloadInfo) {
            binding.tittleTextview.text = download.name
            binding.subtittleTextview.text = download.artists

            if(download.type == DOWNLOAD_TYPE_SONG) binding.downloadCardView.visibility = View.GONE
            else if(download.type == DOWNLOAD_TYPE_ALBUM){
                binding.downloadTypeImageview.setImageResource(R.drawable.ic_album_black_24dp)
                binding.downloadTypeTextview.text = "Album"
                binding.downloadDescriptionTextview.text = download.description
            }
            else if(download.type == DOWNLOAD_TYPE_PLAYLIST){
                binding.downloadTypeImageview.setImageResource(R.drawable.ic_baseline_playlist_play_24)
                binding.downloadTypeTextview.text = "Playlist"
                binding.downloadDescriptionTextview.text = download.description
            }
            Picasso.get().load(download.imagePath.replace("localhost", "192.168.43.166")).fit().centerCrop().placeholder(R.drawable.backimage).into(binding.thumbnailImageview)

            with(binding.root) {
                setOnClickListener {
                    info.interactionListener?.onItemClick(download, adapterPosition,null)
                }
            }

            //caclulate progress
            downloadTrackerScope.launch {
                var songResult : List<Song<String,String>> = emptyList()
                if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_ALBUM){
                    songResult = appDb.albumSongJoinDao.getAlbumSongs(download.contentId).toSong()
                    withContext(Dispatchers.Main) {
                        calculateProgress(download, songResult)
                    }
                }
                else if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG){
                    songResult = appDb.songDao.getSongs(download.contentId).toSong()
                    withContext(Dispatchers.Main) {
                        calculateProgress(download, songResult)
                    }
                }
                else if(download.type == DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST){
                    songResult = appDb.playlistSongJoinDao.getPlaylistSongs(download.contentId).toSong()
                    withContext(Dispatchers.Main) {
                        calculateProgress(download, songResult)
                    }
                }
            }
        }

        fun calculateProgress(downloadInfo: DbDownloadInfo, result: List<Song<String,String>>) {
            var downloadProgress = MutableLiveData<Int>(0)
            var prevProgress = 0
            var timerStopFlag = false

            downloadProgress.observe(info.owner!!, Observer { progress ->
                if(progress > prevProgress && (downloadInfo.state != "PAUSED")){
                    prevProgress = progress
                    timerStopFlag = false
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_file_download_black_24dp)
                    binding.downloadStateTextview.text = "${prevProgress}% donwloading"
                }
//                else if(progress == 0) binding.downloadStateTextview.text = "waiting..."

                if (progress == 100){
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    binding.downloadStateTextview.text = "completed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                    timerStopFlag = true
                }
            })
            var songUriList = result.map { song -> Uri.parse(song.songFilePath!!.replace("localhost", "192.168.43.166")) }

            tracker.getDownloads().observe(info.owner!! , Observer{allDownloads ->
                var completedDownloads = allDownloads.filter { download -> download.state == Download.STATE_COMPLETED }
                    .filter { download -> songUriList.contains(download.request.uri) }
                if(completedDownloads.size == songUriList.size){
                    var size = completedDownloads.map { download -> download.bytesDownloaded }.reduce { acc, download -> acc.plus(download) }
                    binding.downloadStateTextview.text = "${ (size / (1024 * 1024))} Mb"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , COMPLETED)
                    return@Observer
                }

                var failedDownloads = allDownloads.filter { download -> download.state == Download.STATE_FAILED }
                    .filter { download -> songUriList.contains(download.request.uri) }
                if(failedDownloads.isNotEmpty()){
                    var contentLength = failedDownloads.map { download -> download.contentLength }.reduce { acc, download -> acc.plus(download) }
                    binding.downloadStateTextview.text = "Failed"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    return@Observer
                }
            })

            tracker.getOngoingDownloads().observe(info.owner!!, Observer { downloadList ->
                Log.i("downloadcurrent", downloadList.size.toString())
                if(downloadInfo.state == "PAUSED"){
                    binding.downloadStateTextview.text = "Paused"
                    binding.downloadStateIconImageview.setImageResource(R.drawable.exo_controls_pause)
                    timerStopFlag = true
                    menuOptionsClickHandle(downloadInfo , FAILED)
                    return@Observer
                }

                var currentlyDownloadedSongs = downloadList.filter { download -> songUriList.contains(download.request.uri) }
                if (currentlyDownloadedSongs.isNullOrEmpty()) {
                    timerStopFlag = true
                    return@Observer
                }
                else {
                    menuOptionsClickHandle(downloadInfo , RUNNING)
                    tracker.getNotMetRequirements()?.let {
                        Log.i("downloadpr", it+ "requested")
                        var contentLength = downloadList.map { download -> download.contentLength }.reduce { acc, download -> acc.plus(download) }
                        binding.downloadStateTextview.text = tracker.getNotMetRequirements()
                        binding.downloadStateIconImageview.setImageResource(R.drawable.ic_baseline_wifi_24)
                        timerStopFlag = true
                        return@Observer
                    }
                    fixedRateTimer("ALBUM_DONWLOAD_PROGRESS", false, 0, 1000) {
                        var progress = 0
                        if( timerStopFlag) cancel()
                        if(downloadInfo.type == DownloadViewmodel.DOWNLOAD_TYPE_ALBUM || downloadInfo.type == DownloadViewmodel.DOWNLOAD_TYPE_PLAYLIST){
                            var progressList = currentlyDownloadedSongs.map { download -> download.percentDownloaded }
                            progress = (progressList.reduce { acc, fl -> acc.plus(fl) }.div(progressList.size)).toInt()
                            Log.i("downloadpr", "download album progress $progress")
                        }
                        else if(downloadInfo.type == DownloadViewmodel.DOWNLOAD_TYPE_SONG){
                            progress = currentlyDownloadedSongs.first().percentDownloaded.toInt()
                            Log.i("downloadpr", "download song progress $progress")
                        }
                        downloadProgress.postValue(progress)

                    }
                }
            })
        }



        fun menuOptionsClickHandle(downloadInfo : DbDownloadInfo, state : Int){
            binding.downloadOptionsImageview.setOnClickListener {
                 MaterialDialog(binding.root.context , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    cancelOnTouchOutside(true)
                    cornerRadius(literalDp = 13f)
                   when(state){
                       COMPLETED ->{
                           listItems(items = DOWNLOAD_COMPLETE_MENU_OPTIONS){ _, index, item ->
                               if(item == "Play") Toast.makeText(binding.root.context , "$item pending " , Toast.LENGTH_SHORT).show()
                               else if(item == "Delete") info.interactionListener!!.onItemClick(
                                   downloadInfo,
                                   DOWNLOAD_ACTION_DELETE,null
                               )
                           }
                       }
                       RUNNING ->{
                           listItems(items = DOWNLOAD_RUNNGIN_AND_WAITING_MENU_OPTIONS){ _, index, item ->
                              if(item == "Pause") info.interactionListener!!.onItemClick(
                                  downloadInfo,
                                  DOWNLOAD_ACTION_PAUSE,null
                              )
                              else if(item == "Delete") info.interactionListener!!.onItemClick(
                                  downloadInfo,
                                  DOWNLOAD_ACTION_DELETE,null
                              )
                           }
                       }
                       FAILED ->{
                           listItems(items = DOWNLOAD_PAUSED_AND_FAILED_MENU_OPTIONS){ _, index, item ->
                              if(item == "Resume") info.interactionListener!!.onItemClick(
                                  downloadInfo,
                                  DOWNLOAD_ACTION_RESUME,null
                              )
                              else if(item == "Delete") info.interactionListener!!.onItemClick(
                                  downloadInfo,
                                  DOWNLOAD_ACTION_DELETE,null
                              )
                           }
                       }
                   }

                }

            }

        }

    }

}