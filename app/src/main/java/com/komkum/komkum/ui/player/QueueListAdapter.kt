package com.komkum.komkum.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.databinding.PlayerImageListItemBinding


class QueueListAdapter() : ListAdapter<String , QueueListAdapter.QueueViewholder>(QueueLIstAdapterDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewholder {
        var binding  = PlayerImageListItemBinding.inflate(LayoutInflater.from(parent.context),  parent , false)
        return QueueViewholder(binding)
    }

    override fun onBindViewHolder(holder: QueueViewholder, position: Int) {
        var imageUrl = getItem(position).replace("localhost" , AdapterDiffUtil.URL)
        Picasso.get().load(imageUrl).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(holder.imageview)
    }

    inner class QueueViewholder(var binding : PlayerImageListItemBinding) : RecyclerView.ViewHolder(binding.root){
        var imageview = binding.queueSongImageView
    }
}

class QueueLIstAdapterDiffUtil : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem.equals(newItem)
    }
}