package com.example.ethiomusic.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.DownloadStyleListitemBinding
import com.example.ethiomusic.util.viewhelper.CircleTransformation
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso

class DownloadStyleListItemAdapter <T : BaseModel>(var info: RecyclerViewHelper<T>) : ListAdapter<T, DownloadStyleListItemAdapter<T>.DownloadStyleListitemViewholder>(AdapterDiffUtil<T>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadStyleListitemViewholder {
        var binding = DownloadStyleListitemBinding .inflate(LayoutInflater.from(parent.context) , parent , false)
        return DownloadStyleListitemViewholder(binding)
    }

    override fun onBindViewHolder(holder: DownloadStyleListitemViewholder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class DownloadStyleListitemViewholder(var binding : DownloadStyleListitemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : T){
            binding.tittleTextview.text = data.baseTittle
            binding.subtittleTextview.text = data.baseSubTittle
            var imageurl = data.baseImagePath?.replace("localhost" , "192.168.43.166")
            when (info.type) {
                "PLAYLIST" -> Picasso.get().load(imageurl).placeholder(R.drawable.backimage).transform(
                    RoundImageTransformation()
                ).fit().centerCrop().into(binding.thumbnailImageview)
                "ARTIST" -> Picasso.get().load(imageurl).transform(CircleTransformation()).placeholder(R.drawable.backimage).fit().centerInside().into(binding.thumbnailImageview)
                else -> Picasso.get().load(imageurl).placeholder(R.drawable.backimage).fit().centerInside().into(binding.thumbnailImageview)
            }

            setupclickListener(data)
        }


        fun setupclickListener(data : T){
            with(binding.root){
                setOnClickListener {
                    info.interactionListener?.onItemClick(data, adapterPosition,null)
                }
            }
        }
    }

}