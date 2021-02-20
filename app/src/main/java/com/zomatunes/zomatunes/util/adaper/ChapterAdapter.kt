package com.zomatunes.zomatunes.util.adaper

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
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Chapter
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.Song
import com.zomatunes.zomatunes.databinding.ChapterListItemBinding
import com.zomatunes.zomatunes.ui.album.adapter.SongAdapter
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewState
import com.squareup.picasso.Picasso
import kotlin.concurrent.fixedRateTimer

class ChapterAdapter(var info : RecyclerViewHelper<Chapter> , var chapterList : List<Chapter> , var bookImage : String)
    : RecyclerView.Adapter<ChapterAdapter.ChapterViewholder>() {

    var completedChapters = mutableSetOf<Chapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewholder {
       var binding = ChapterListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ChapterViewholder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewholder, position: Int) {
        holder.bind(chapterList[position])
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

            var hour = chapter.duration?.div(60)?.div(60)
            var minute = chapter.duration?.div(50)?.rem(60)
            binding.durationTextview.text = "$hour hr $minute min"

            if(info.stateManager?.state!!.value is RecyclerviewState.DownloadState){
                handleDownloadStatus(chapter)
            }

           info.stateManager?.selectedItem?.observe(info.owner!!, Observer{
                    if(it == chapter){
                       makeViewActive(R.color.primaryColor)
                    }
                    else {
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

        fun handleDownloadStatus(chapter: Chapter){
            var isDownloadFailed = false
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
                    info.stateManager?.removeFromDownload(chapter.mpdPath!!.replace("localhost", AdapterDiffUtil.URL))
                    makeViewActive()
                }
            })

            info.stateManager?._downloadingItems?.observe(info.owner!!, Observer { downloadItems ->
                var downloadUrl = downloadItems.map { item -> item.request.uri }
                var songUri = Uri.parse(chapter.mpdPath?.replace("localhost" , AdapterDiffUtil.URL))

                if(downloadUrl.contains(songUri)) {
                    makeViewInactive(true)
                    info.downloadTracker?.getNotMetRequirements()?.let {
                        binding.downloadProgressTextview.text = ""
                        binding.downloadStatusImageview.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                        isDownloadFailed = true
                        return@Observer
                    }

                    fixedRateTimer("SONG_DONWLOAD_PROGRESS", false, 0, 1000) {
                        Log.i("downloadbook", "continue progress checking")
                        var songDownload = downloadItems.first { download -> download.request.uri == songUri }
                        var progress = songDownload.percentDownloaded.toInt()
                        downloadProgress.postValue(progress)
                        if(progress == 100 || isDownloadFailed) cancel()
                    }
                }
                else{
                    info.downloadTracker?.getFailedDownloads()?.observe(info.owner!! , Observer{ downloads ->
                        var isFailed =   downloads.map {  download -> download.request.uri }.contains(songUri)
                        if(isFailed){
                            makeViewInactive(false)
                            binding.downloadStatusImageview.visibility = View.VISIBLE
                            binding.downloadStatusImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            isDownloadFailed = isFailed
                        }
                        else{
                            completedChapters.add(chapter)
                            makeViewActive()
                        }
                    })

                }

            })
        }
    }

    private fun ChapterViewholder.makeViewInactive(isprogressVisible: Boolean) {
        binding.downloadStatusImageview.visibility = View.VISIBLE
        binding.downloadProgressTextview.visibility = View.VISIBLE
        binding.downloadProgressTextview.isVisible = true

        binding.downloadProgressTextview.isVisible = isprogressVisible
        binding.chapterNumberTextview.setTextColor(binding.root.context.resources.getColor(R.color.secondaryTextColor))
        binding.chapterNameTextview.setTextColor(binding.root.context.resources.getColor(R.color.secondaryTextColor))
        binding.durationTextview.setTextColor(binding.root.context.resources.getColor(R.color.secondaryTextColor))
    }

    private fun ChapterViewholder.makeViewActive(color : Int? = null) {

        binding.downloadProgressTextview.isVisible = false
        binding.downloadStatusImageview.isVisible = true
        binding.downloadStatusImageview.setImageResource(R.drawable.ic_check_circle_black_24dp)

        binding.chapterNumberTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.secondaryTextColor))
        binding.chapterNameTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.primaryTextColor))
        binding.durationTextview.setTextColor(binding.root.context.resources.getColor(color ?: R.color.primaryTextColor))
    }
}