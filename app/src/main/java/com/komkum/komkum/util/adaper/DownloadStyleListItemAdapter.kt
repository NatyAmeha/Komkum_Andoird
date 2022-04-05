package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.DownloadStyleListitemBinding
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.RadioSpan
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

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
            var spanner = Spanner(data.baseSubTittle).span("span" , Spans.custom { RadioSpan(binding.root.context , R.drawable.tab_item_not_active , binding.root.context.resources.getInteger(R.integer.bullet_margin)) })

            binding.tittleTextview.text = data.baseTittle
            binding.subtittleTextview.text = spanner
            var imageurl = data.baseImagePath
            when (info.type) {
                "PLAYLIST" -> {
                    Picasso.get().load(imageurl).placeholder(R.drawable.music_placeholder)
                        .fit().centerCrop().into(binding.thumbnailImageview)
                    binding.radioDescriptionTextview.text = "${data.baseListOfInfo?.size ?: 0} ${binding.root.context.getString(R.string.followers)}"
                }
                "ARTIST" -> Picasso.get().load(imageurl).transform(CircleTransformation()).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(binding.thumbnailImageview)
                else -> Picasso.get().load(imageurl).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(binding.thumbnailImageview)
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