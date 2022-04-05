package com.komkum.komkum.ui.album.adapter

import android.content.Context
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.exoplayer2.offline.Download
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.SongListItemBinding

import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.adaper.MenuAdapter
import com.komkum.komkum.util.viewhelper.*
import com.squareup.picasso.Picasso
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import kotlinx.coroutines.*
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.concurrent.fixedRateTimer

class SongAdapter(var info : RecyclerViewHelper<Song<String,String>>? = null) :
    ListAdapter<Song<String,String>, SongAdapter.SongViewHolder>(AdapterDiffUtil<Song<String,String>>()) ,
    ItemTouchHelperLIstener {

    var downloadTrackerScope  = CoroutineScope(Dispatchers.Main)
//    var mainOptions = listOf("Play" , "Remove")
//    var failedDownloadOPtion = listOf("Resume" , "Remove Download")
//    var runningDownloadOption = listOf("Remove Download")

    var completedDownloades = mutableSetOf<Song<String,String>>()
    var timers : MutableList<Timer> = mutableListOf()
    lateinit var itemTouchHelper : ItemTouchHelper

    companion object{
        const val NO_OPTION = -1
        const val PLAY_OPTION = 0
        const val ADD_TO_OPTION =  1
        const val DOWNLOAD_OPTION = 2
        const val GO_TO_ARTIST_OPTION = 3
        const val GO_TO_ALBUM_OPTION = 4
        const val SONG_INFO_OPTION = 5
        const val REMOVE_DOWNLOAD_OPTION = 6
        const val RESUME_DOWNLOAD_OPTION = 7
        const val SONG_RADIO_OPTION = 8
        const val ARTIST_RADIO_OPTION = 9

        //download states
        const val RUNNING = 0
        const val COMPLETED = 1
        const val FAILED = 2
        const val ONLINE = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        var binding = SongListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        downloadTrackerScope.cancel()
//        timer?.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }




    inner class SongViewHolder(var binding: SongListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song : Song<String,String>) {
//            binding.tittleTextview.ellipsize =  TextUtils.TruncateAt.MARQUEE
//            binding.tittleTextview.isSelected = true
//            binding.tittleTextview.marqueeRepeatLimit = -1
            binding.lifecycleOwner = info?.owner
            binding.state = info?.stateManager
            binding.song = song
            binding.type = info?.type

            var duration = song.trackLength?.toLong()?.let { DateUtils.formatElapsedTime(it) }
            var text = song.artistsName?.joinToString(", ") ?: "Artists name span$duration"
            var spannerText = Spanner(text).span("span" , Spans.custom {
                RadioSpan(binding.root.context , R.drawable.tab_item_not_active , binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
            binding.subtittleTextview.text = spannerText

            if(info?.type != "ALBUM"){
                var imageurl = song.thumbnailPath
                Picasso.get().load(imageurl).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(binding.thumbnailImageview)
            }

            // download handler
            if(info?.stateManager?.state?.value is RecyclerviewState.DownloadState){
                handleDownloadState(song , binding.root.context)
            }else {
                makeViewActive()
                showSongOption(binding.root.context , song , ONLINE , binding.root.context.musicMenuItem.toMutableList())
            }

            // multiselection handler
            info?.owner?.let {
                info?.stateManager?.multiselectedItems?.observe(it , Observer { selectedItems ->
                    binding.playingIndicatorIv.isVisible = false
                    if(!selectedItems.isNullOrEmpty()) binding.selectionCheckBox.isChecked = selectedItems.contains(song)
                })

                info?.stateManager?.selectedItem?.observe(it , Observer{
                    binding.selectedSong = it
                    if(it == song){
                        binding.selected = true
                        makeViewActive(R.color.primaryColor)
                        binding.playingIndicatorIv.visibility = View.VISIBLE
                    }
                    else {
                        binding.selected = false
                        if(info?.stateManager?.state?.value is RecyclerviewState.DownloadState){
                            if(completedDownloades.contains(song)){
                                makeViewActive()
                                binding.playingIndicatorIv.visibility = View.GONE
                            }
                            else{
                                makeViewInactive(false)
                                binding.playingIndicatorIv.visibility = View.GONE
                            }
                        }
                        else{
                            makeViewActive()
                            binding.playingIndicatorIv.visibility = View.GONE
                        }
                    }
                })
            }
            // click handler
            setupOnclickListener(song)

        }


        fun handleDownloadState(song : Song<String,String> , context: Context){
            var isDownloadFailed = false

            var preference = PreferenceHelper.getInstance(context)
            var qualitySelector = preference.get("download_quality", "64")
//           observe download progress
            var downloadProgress = MutableLiveData<Int>(0)
            var prevProgress = 0
            showSongOption(binding.root.context , song , RUNNING , MenuItem.offlineSongMenuItem)
            downloadProgress.observe(info!!.owner!!, Observer { progress ->
                if(progress > prevProgress){
                    prevProgress = progress
                    makeViewInactive(true)
                    binding.downloadProgressTextview.text = "${prevProgress}%"
                }
                if (progress == 100){
                    info?.stateManager?.removeFromDownload(song.mpdPath!!)
                    makeViewActive()

                    showSongOption(binding.root.context , song , COMPLETED , MenuItem.offlineSongMenuItem)
                }
            })



            info?.stateManager?._downloadingItems?.observe(info!!.owner!!, Observer { downloadItems ->
                var downloadUrl = downloadItems.map { item -> item.request.uri }
                var songUri = when(qualitySelector){
                    "64" -> Uri.parse(song.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8"))
                    "160" -> Uri.parse(song.mpdPath?.replace("index.m3u8" , "hls_audio_160k_v4.m3u8"))
                    else -> Uri.parse(song.mpdPath?.replace("index.m3u8" , "hls_audio_64k_v4.m3u8"))
                }

                if(downloadUrl.contains(songUri)) {
                    Log.i("current download" , adapterPosition.toString())
                    binding.songDonwloadImageview.visibility = View.VISIBLE
                    makeViewInactive(true)

                    info?.downloadTracker?.getNotMetRequirements()?.let {
                        binding.downloadProgressTextview.isVisible = false
                        binding.songDonwloadImageview.setImageResource(R.drawable.ic_baseline_wifi_off_24)
                        makeViewInactive(false)
                        showSongOption(binding.root.context , song , RUNNING , MenuItem.offlineSongMenuItem)
                        return@Observer
                    }

                    var pausedDownloadsUri = downloadItems.filter { download -> download.state == Download.STATE_STOPPED }
                        .map { download -> download.request.uri }
                    if(pausedDownloadsUri.contains(songUri)){
                        binding.downloadProgressTextview.text = ""
                        binding.songDonwloadImageview.setImageResource(R.drawable.exo_controls_pause)
//                        timer?.cancel()
                        return@Observer
                    }

                    var timer = fixedRateTimer("SONG_DONWLOAD_PROGRESS", false, 0, 1000) {
                        Log.i("download from song" , "requested")
                        var songDownload = downloadItems.first { download -> download.request.uri == songUri }
                        var progress = songDownload.percentDownloaded.toInt()
                        downloadProgress.postValue(progress)
                        if(progress == 100 || isDownloadFailed) cancel()
                    }
                    timers.add(timer)
                }
                else{

                    info?.downloadTracker?.getFailedDownloads()?.observe(info!!.owner!! , Observer{ downloads ->
                        var isFailed =   downloads.map {  download -> download.request.uri }.contains(songUri)
                        if(isFailed){
                            binding.songDonwloadImageview.visibility = View.VISIBLE
                            binding.songDonwloadImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            Log.i("current download fa" , adapterPosition.toString())
                            makeViewInactive(false)
                            isDownloadFailed = isFailed
                            showSongOption(binding.root.context , song , FAILED , MenuItem.offlineSongMenuItem)
                        }
                        else{
                            binding.songDonwloadImageview.visibility = View.INVISIBLE
                            completedDownloades.add(song)
                            makeViewActive()
                            showSongOption(binding.root.context , song , COMPLETED , MenuItem.offlineSongMenuItem)
                        }
                    })

                }

            })
        }


        fun showSongOption(context : Context , data : Song<String,String> ,  state : Int , options : List<MenuItem>){
            binding.moreOptionsImageview.setOnClickListener {
                var dialog = MaterialDialog(context , BottomSheet(LayoutMode.WRAP_CONTENT))
                    . cancelOnTouchOutside(true)
                    .cornerRadius(literalDp = 14f)
                    .customView(R.layout.custom_menu_layout , scrollable = true)

                var interactionListener = registerRecyclerviewINteractionListner(dialog ,context , state , data , adapterPosition)
                var reyclerviewHelper = RecyclerViewHelper("MENU" , interactionListener = interactionListener)

                var customview =  dialog.getCustomView()
                Picasso.get().load(data.thumbnailPath)
                    .placeholder(R.drawable.music_placeholder).fit().centerCrop()
                    .into(customview.findViewById<ImageView>(R.id.menu_imageview))
                customview.findViewById<TextView>(R.id.menu_title_textview).text = data.tittle
                customview.findViewById<TextView>(R.id.menu_sub_title_textview).text = data.artistsName?.joinToString(", ") ?: "Artist names"
                customview.findViewById<RecyclerView>(R.id.menu_recyclerview).layoutManager = LinearLayoutManager(context)
                customview.findViewById<RecyclerView>(R.id.menu_recyclerview).adapter = MenuAdapter(reyclerviewHelper    , options)

                dialog.show()
            }
        }



        fun setupOnclickListener(data : Song<String,String>){

            binding.songDragImageview.setOnClickListener {
                info?.let {
                    if(it.type == "PLAYLIST" || it.type =="QUEUE") itemTouchHelper.startDrag(this)
                }
            }

            binding.selectionCheckBox.setOnClickListener {
                info?.let {
                    it.interactionListener?.onItemClick(data , adapterPosition , NO_OPTION)
                }
            }

            binding.root.setOnClickListener {
                info?.let {
                    it.interactionListener?.onItemClick(data, adapterPosition, NO_OPTION)
                }
            }
            binding.root.setOnLongClickListener {
                info?.interactionListener?.activiateMultiSelectionMode()
                info?.interactionListener?.onItemClick(data, adapterPosition, NO_OPTION)
                true
            }
        }
    }

    private fun SongViewHolder.makeViewInactive(isprogressVisible : Boolean) {
        binding.songContainer.alpha = 0.4f
        binding.downloadProgressTextview.isVisible = isprogressVisible

    }

    private fun SongViewHolder.makeViewActive(color : Int? = null) {
        binding.songContainer.alpha = 1.0f
        binding.downloadProgressTextview.isVisible = false
        color?.let {
//            binding.tittleTextview.setTextColor(color)
//            binding.subtittleTextview.setTextColor(color)
        }

//        binding.downloadProgressTextview.isVisible = false
//        binding.songDonwloadImageview.visibility = View.INVISIBLE
    }

    override fun onSwiped(position: Int) {
        info?.interactionListener?.onSwiped(position)
        notifyItemRemoved(position)
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        info?.interactionListener?.onMoved(prevPosition , newPosition)
        notifyItemMoved(prevPosition , newPosition)
    }


//    fun showSongInfoDialog(context : Context , song : Song<String,String>){
//        var info  = song.songCredit?.toMutableList()
//        var a = listOf(song.tags.toString() , song.genre.toString())
//        info?.addAll(a)
//        info?.let {
//            MaterialDialog(context).show {
//                cornerRadius(literalDp = 10f)
//                title(text = song.tittle)
//                listItems(items = info)
//            }
//        }
//    }

    fun registerRecyclerviewINteractionListner(dialog : MaterialDialog , context: Context , state : Int , songData : Song<String, String>, songPosition : Int) =  object : IRecyclerViewInteractionListener<MenuItem>{
            override fun onItemClick(data: MenuItem, position: Int, option: Int?) {
               when(state){
                   ONLINE ->{  // steram from server
                       when(position){
                           0 -> info?.interactionListener?.onItemClick(songData , songPosition , DOWNLOAD_OPTION)
                           1 -> info?.interactionListener?.onItemClick(songData , songPosition , SONG_RADIO_OPTION)
                           2 -> info?.interactionListener?.onItemClick(songData , songPosition , ARTIST_RADIO_OPTION)
                           3 -> info?.interactionListener?.onItemClick(songData , songPosition , GO_TO_ALBUM_OPTION)
                           4 ->  info?.interactionListener?.onItemClick(songData , songPosition , GO_TO_ARTIST_OPTION)
                           5 -> info?.interactionListener?.onItemClick(songData , songPosition , SONG_INFO_OPTION)
                           6 -> info?.interactionListener?.onItemClick(songData , songPosition , ADD_TO_OPTION)
                       }
                   }
                   else  -> {
                       when(position){
                           0 -> info?.interactionListener?.onItemClick(songData , songPosition , REMOVE_DOWNLOAD_OPTION)
                       }
                   }
               }
                dialog.dismiss()
            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }


    fun addTouchHelper(touchHelper : ItemTouchHelper){
        itemTouchHelper = touchHelper
    }

    override fun onViewDetachedFromWindow(holder: SongViewHolder) {
        Log.i("currentdownloadfa" , "detach called")
        timers.forEach { timer -> timer.cancel() }
        super.onViewDetachedFromWindow(holder)
    }




}