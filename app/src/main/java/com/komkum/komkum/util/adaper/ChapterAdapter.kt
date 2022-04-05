package com.komkum.komkum.util.adaper

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.offline.Download
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Chapter
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.ChapterListItemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.viewhelper.RecyclerviewState
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.MenuItem
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get

import java.util.*
import kotlin.concurrent.fixedRateTimer

class ChapterAdapter(var info : RecyclerViewHelper<Chapter> , var chapterList : List<Chapter> , var bookImage : String)
    : RecyclerView.Adapter<ChapterAdapter.ChapterViewholder>() {

    var completedChapters = mutableSetOf<Chapter>()
    var timers : MutableList<Timer> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewholder {
       var binding = ChapterListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ChapterViewholder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewholder, position: Int) {
        holder.bind(chapterList[position])
    }

    override fun onViewDetachedFromWindow(holder: ChapterViewholder) {
        Log.i("currentdownloadca" , " chapter detach called")
        timers.forEach { timer -> timer.cancel() }
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
       return chapterList.size
    }

    inner class ChapterViewholder(var binding : ChapterListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(chapter : Chapter){
            binding.lifecycleOwner = info.owner
            binding.bookImage = bookImage
            binding.chapter = chapter
            binding.info = info


            var hour = chapter.duration?.div(3600)
            var minute = chapter.duration?.rem(3600)?.div(60)
            binding.durationTextview.text = "$hour hr $minute min"
            if(chapter.completed){
                binding.completedProgressbar.max = 100
                binding.completedProgressbar.progress = 100
            }
            else {
                binding.completedProgressbar.max = chapter.duration ?: 0
                binding.completedProgressbar.progress = chapter.currentPosition.div(1000).toInt()
            }

            if(info.stateManager?.state!!.value is RecyclerviewState.DownloadState){
                handleDownloadStatus(chapter , binding.root.context)
            }else makeViewActive()

           info.stateManager?.selectedItem?.observe(info.owner!!, Observer{
                    if(it == chapter){
                       makeViewActive(R.color.primaryColor)
                        binding.selected = true
                    }
                    else {
                        binding.selected = false
                        if(info.stateManager?.state?.value is RecyclerviewState.DownloadState){
                            if(completedChapters.contains(chapter)){
                                makeViewActive()
                            }
                            else{
                                makeViewInactive(false)
                            }
                        }
                        else makeViewActive()
                    }
                })

            binding.root.setOnClickListener {
                info.interactionListener!!.onItemClick(chapter , adapterPosition , SongAdapter.NO_OPTION)
            }
        }

        fun handleDownloadStatus(chapter: Chapter , context : Context){
            var isDownloadFailed = false
            var timerStopflag = false

            var preference = PreferenceHelper.getInstance(context)
            var qualitySelector = preference.get("download_quality", "64")

//           observe download progress
            var downloadProgress = MutableLiveData(0)
            var prevProgress = 0
            downloadProgress.observe(info.owner!!, Observer { progress ->
                if(progress >= prevProgress){
                    prevProgress = progress
                    makeViewInactive(true)
                    binding.downloadProgressTextview.text = "${prevProgress}%"
                }
                if (progress == 100){
                    info.stateManager?.removeFromDownload(chapter.mpdPath!!)
                    makeViewActive()
                    timerStopflag = true
                }
            })

            info.stateManager?._downloadingItems?.observe(info.owner!!, Observer { downloadItems ->
                var downloadUrl = downloadItems.map { item -> item.request.uri }
                var chapterUri = when(qualitySelector){
                    "64" -> Uri.parse(chapter.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8"))
                    "160" -> Uri.parse(chapter.mpdPath?.replace("index.m3u8" , "hls_audio_160k_v4.m3u8"))
                    else -> Uri.parse(chapter.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8"))
                }

                if(downloadUrl.contains(chapterUri)) {
                    makeViewInactive(true)
                    info.downloadTracker?.getNotMetRequirements()?.let {
                        binding.downloadProgressTextview.text = ""
                        binding.downloadStatusImageview.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                        isDownloadFailed = true
                        timerStopflag = true
                        return@Observer
                    }

                    var pausedDownloadsUri = downloadItems.filter { download -> download.state == Download.STATE_STOPPED }
                        .map { download -> download.request.uri }
                    if(pausedDownloadsUri.contains(chapterUri)){
                        binding.downloadProgressTextview.text = ""
                        binding.downloadStatusImageview.setImageResource(R.drawable.exo_controls_pause)
                        timerStopflag = true
                        return@Observer
                    }


                    var timer =  fixedRateTimer("SONG_DONWLOAD_PROGRESS", false, 0, 1000) {
                        var songDownload = downloadItems.first { download -> download.request.uri == chapterUri }
                        var progress = songDownload.percentDownloaded.toInt()
                        Log.i("downloadbook", "continue progress checking" + "   ${progress}")
                        downloadProgress.postValue(progress)
                        if(progress == 100 || isDownloadFailed || timerStopflag) cancel()
                    }
                    timers.add(timer)
                }
                else{
                    info.downloadTracker?.getFailedDownloads()?.observe(info.owner!! , Observer{ downloads ->
                        var isFailed =   downloads.map {  download -> download.request.uri }.contains(chapterUri)
                        if(isFailed){
                            makeViewInactive(false)
                            binding.downloadStatusImageview.visibility = View.VISIBLE
                            binding.downloadStatusImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            isDownloadFailed = isFailed
                            timerStopflag = true
                        }
                        else{
                            completedChapters.add(chapter)
                            makeViewActive()
                            timerStopflag = true
                        }
                    })

                }

            })
        }
    }

    private fun ChapterViewholder.makeViewInactive(isprogressVisible: Boolean) {
        binding.chapterCardView.alpha = 0.3f
        binding.downloadStatusImageview.visibility = View.VISIBLE
        binding.downloadProgressTextview.visibility = View.VISIBLE
        binding.downloadProgressTextview.isVisible = true

    }

    private fun ChapterViewholder.makeViewActive(color : Int? = null) {
        binding.chapterCardView.alpha = 1.0f
        binding.downloadProgressTextview.isVisible = false
        binding.downloadStatusImageview.isVisible = true
        binding.downloadStatusImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)

//        binding.chapterNumberTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.secondaryTextColor))
//        binding.chapterNameTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.primaryTextColor))
//        binding.durationTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.primaryTextColor))
    }
}