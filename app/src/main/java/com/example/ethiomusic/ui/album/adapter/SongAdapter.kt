package com.example.ethiomusic.ui.album.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.list.listItems
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.SongListItemBinding

import com.example.ethiomusic.util.adaper.AdapterDiffUtil
import com.example.ethiomusic.util.viewhelper.ItemTouchHelperLIstener
import com.example.ethiomusic.util.viewhelper.RecyclerviewState
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.concurrent.fixedRateTimer

class SongAdapter(var info : RecyclerViewHelper<Song<String,String>>? = null) :
    ListAdapter<Song<String,String>, SongAdapter.SongViewHolder>(AdapterDiffUtil<Song<String,String>>()) , ItemTouchHelperLIstener {

    var downloadTrackerScope  = CoroutineScope(Dispatchers.Main)
    var mainOptions = listOf("Play" , "Add to"  , "Download" ,  "Go to Artist" , "Go to Album" , "Song Info")
    var failedDownloadOPtion = listOf("Resume" , "Remove Download")
    var runningDownloadOption = listOf("Remove Download")

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

        //download states
        const val RUNNING = 0
        const val COMPLETED = 1
        const val FAILED = 2
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

            if(info?.type != "ALBUM"){
                var imageurl = song.thumbnailPath?.replace("localhost" , "192.168.43.166")
//                imageurl?.replace("public" , "http://192.168.43.166:4000")
                Picasso.get().load(imageurl).transform(RoundImageTransformation()).placeholder(R.drawable.backimage).fit().centerInside().into(binding.thumbnailImageview)
            }

            // download handler
            if(info?.stateManager?.state?.value is RecyclerviewState.DownloadState){
                handleDownloadState(song)
            }else {
                showSongOption(binding.root.context , song , COMPLETED)
            }

            // multiselection handler
            info?.owner?.let {
                info?.stateManager?.multiselectedItems?.observe(it , Observer { selectedItems ->
                    binding.selectionCheckBox.isChecked = selectedItems.contains(song)
                })
            }
            // click handler
            setupOnclickListener(song)

        }


        fun handleDownloadState(song : Song<String,String>){
            var isDownloadFailed = false
//           observe download progress
            var downloadProgress = MutableLiveData<Int>(0)
            var prevProgress = 0
            showSongOption(binding.root.context , song , RUNNING)
            downloadProgress.observe(info!!.owner!!, Observer { progress ->
                if(progress > prevProgress){
                    prevProgress = progress
                    binding.downloadProgressTextview.text = "${prevProgress}%"
                }
                if (progress == 100){
                    info?.stateManager?.removeFromDownload(song.songFilePath!!.replace("localhost" , "192.168.43.166"))
                    showSongOption(binding.root.context , song , COMPLETED)
                }
            })

            info?.stateManager?._downloadingItems?.observe(info!!.owner!!, Observer { downloadItems ->
                var downloadUrl = downloadItems.map { item -> item.request.uri }
                var songUri = Uri.parse(song.songFilePath?.replace("localhost" , "192.168.43.166"))

                if(downloadUrl.contains(songUri)) {
                    binding.songDonwloadImageview.visibility = View.VISIBLE
                    binding.downloadProgressTextview.visibility = View.VISIBLE

                    info?.downloadTracker?.getNotMetRequirements()?.let {
                        binding.downloadProgressTextview.text = ""
                        binding.songDonwloadImageview.setImageResource(R.drawable.ic_signal_wifi_off_black_24dp)
                        showSongOption(binding.root.context , song , RUNNING)
                        return@Observer
                    }

                    fixedRateTimer("SONG_DONWLOAD_PROGRESS", false, 0, 1000) {
                        var songDownload = downloadItems.first { download -> download.request.uri == songUri }
                        var progress = songDownload.percentDownloaded.toInt()
                        downloadProgress.postValue(progress)
                        if(progress == 100 || isDownloadFailed) cancel()
                    }
                }
                else{
                    info?.downloadTracker?.getFailedDownloads()?.observe(info!!.owner!! , Observer{ downloads ->
                        var isFailed =   downloads.map {  download -> download.request.uri }.contains(songUri)
                        if(isFailed){
                            binding.songDonwloadImageview.visibility = View.VISIBLE
                            binding.songDonwloadImageview.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            isDownloadFailed = isFailed
                            showSongOption(binding.root.context , song , FAILED)
                        }
                        else{
                            binding.songDonwloadImageview.visibility = View.INVISIBLE
                            binding.downloadProgressTextview.visibility = View.GONE
                            showSongOption(binding.root.context , song , COMPLETED)
                        }
                    })

                }

            })
        }


        fun showSongOption(context : Context , data : Song<String,String> ,  state : Int){
            binding.moreOptionsImageview.setOnClickListener {
                MaterialDialog(context , BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                    cancelOnTouchOutside(true)
                    cornerRadius(literalDp = 12f)
                    when(state){
                        COMPLETED ->{
                            listItems(items = mainOptions){ dialog, index, item ->
                                when(index){

                                    PLAY_OPTION -> info?.interactionListener?.onItemClick(data , adapterPosition , PLAY_OPTION)
                                    ADD_TO_OPTION -> info?.interactionListener?.onItemClick(data , adapterPosition , ADD_TO_OPTION)
                                    DOWNLOAD_OPTION -> info?.interactionListener?.onItemClick(data , adapterPosition , DOWNLOAD_OPTION)
                                    GO_TO_ARTIST_OPTION -> info?.interactionListener?.onItemClick(data , adapterPosition , GO_TO_ARTIST_OPTION)
                                    GO_TO_ALBUM_OPTION -> info?.interactionListener?.onItemClick(data , adapterPosition , GO_TO_ALBUM_OPTION)
                                    SONG_INFO_OPTION ->{
                                        showSongInfoDialog(binding.root.context , data)
//                                info?.interactionListener?.onItemClick(data , adapterPosition , SONG_INFO_OPTION)
                                    }
                                }
                            }
                        }
                        FAILED ->{
                            listItems(items = failedDownloadOPtion){ _, index, _ ->
                                when(index){
                                    0 -> info?.interactionListener?.onItemClick(data , adapterPosition , RESUME_DOWNLOAD_OPTION)
                                    1 -> info?.interactionListener?.onItemClick(data , adapterPosition , REMOVE_DOWNLOAD_OPTION)
                                }
                            }
                        }
                        RUNNING ->{
                            listItems(items = runningDownloadOption){ _, index, _ ->
                                when(index){
                                    0 -> info?.interactionListener?.onItemClick(data , adapterPosition , REMOVE_DOWNLOAD_OPTION)
                                }
                            }
                        }
                    }

                }
            }
        }



        fun setupOnclickListener(data : Song<String,String>){

            binding.songDragImageview.setOnLongClickListener {
                info?.let {
                    if(it.type == "PLAYLIST" || it.type =="QUEUE") itemTouchHelper.startDrag(this)
                }
                true
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

    override fun onSwiped(position: Int) {
        info?.interactionListener?.onSwiped(position)
        notifyItemRemoved(position)
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        info?.interactionListener?.onMoved(prevPosition , newPosition)
        notifyItemMoved(prevPosition , newPosition)
    }





    fun showSongInfoDialog(context : Context , song : Song<String,String>){
        var info  = song.songCredit?.map { entry -> "${entry.key}  :  ${entry.value}" }?.toMutableList()
        var a = listOf(song.tags.toString() , song.genre.toString())
        info?.addAll(a)
        info?.let {
            MaterialDialog(context).show {
                cornerRadius(literalDp = 10f)
                title(text = song.tittle)
                listItems(items = info)
            }
        }
    }

    fun addTouchHelper(touchHelper : ItemTouchHelper){
        itemTouchHelper = touchHelper
    }


}