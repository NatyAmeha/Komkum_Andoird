package com.komkum.komkum.ui.download

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.data.db.AppDb
import com.komkum.komkum.data.model.DbDownloadInfo
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.DownloadViewpagerListItemBinding
import com.komkum.komkum.util.adaper.DownloadAdapter
import java.util.*

class DownloadViewpagerAdapter(var info : RecyclerViewHelper<DbDownloadInfo> , var downloadInfoMap : Map<Int , List<DbDownloadInfo>>? ,
                               var appDb : AppDb , var tracker: TimerTracker)
    : RecyclerView.Adapter<DownloadViewpagerAdapter.DownloadViewPagerViewholder>() {
    var timers = mutableListOf<Timer>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewPagerViewholder {
        var binding = DownloadViewpagerListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return DownloadViewPagerViewholder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewPagerViewholder, position: Int) {
        holder.bind(downloadInfoMap?.get(position))
    }

    override fun getItemCount(): Int {
       return 4
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        Log.i("currentdviewpager" , "attach viewpager" )
        super.onAttachedToRecyclerView(recyclerView)
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        Log.i("currentdviewpager" , "viewpager detach called   ${timers.size}")
//        timers.forEach { timer -> timer.cancel() }
        super.onDetachedFromRecyclerView(recyclerView)
    }


    inner class DownloadViewPagerViewholder(var binding : DownloadViewpagerListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(downloadList : List<DbDownloadInfo>?){
            downloadList?.let {
                binding.downloadEmptyTextview.visibility = View.GONE
                var downloadAdapter = DownloadAdapter(info , info.downloadTracker!! , appDb , tracker)

                binding.downloadRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
                binding.downloadRecyclerview.adapter = downloadAdapter
                downloadAdapter.submitList(it)
            }

        }
    }

}

interface TimerTracker {
    fun onAdd(timer : Timer)
}

