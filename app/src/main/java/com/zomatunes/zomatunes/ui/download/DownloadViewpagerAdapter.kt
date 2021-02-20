package com.zomatunes.zomatunes.ui.download

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.data.db.AppDb
import com.zomatunes.zomatunes.data.model.BaseModel
import com.zomatunes.zomatunes.data.model.DbDownloadInfo
import com.zomatunes.zomatunes.data.model.DownloadType
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.DownloadViewpagerListItemBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.adaper.DownloadAdapter

class DownloadViewpagerAdapter(var info : RecyclerViewHelper<DbDownloadInfo> , var downloadInfoMap : Map<Int , List<DbDownloadInfo>>? , var appDb : AppDb)
    : RecyclerView.Adapter<DownloadViewpagerAdapter.DownloadViewPagerViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewPagerViewholder {
        var binding = DownloadViewpagerListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return DownloadViewPagerViewholder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewPagerViewholder, position: Int) {
        holder.bind(downloadInfoMap?.get(position))
    }

    override fun getItemCount(): Int {
       return 3
    }

    inner class DownloadViewPagerViewholder(var binding : DownloadViewpagerListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(downloadList : List<DbDownloadInfo>?){
            downloadList?.let {
                binding.downloadEmptyTextview.visibility = View.GONE
                var downloadAdapter = DownloadAdapter(info , info.downloadTracker!! , appDb)
                binding.downloadRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
                binding.downloadRecyclerview.adapter = downloadAdapter
                downloadAdapter.submitList(it)
            }

        }
    }

}

