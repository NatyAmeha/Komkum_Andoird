package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.PodcastHomeViewpagerListitemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter

class PodcastHomeViewpagerAdapter(var info : RecyclerViewHelper<Podcast<String>> , var podcastLists : List<Podcast<String>>)
    : RecyclerView.Adapter<PodcastHomeViewpagerAdapter.PodcastHomeViewholder>() {

    inner class PodcastHomeViewholder(var binding : PodcastHomeViewpagerListitemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(podcast : Podcast<String>){
            Picasso.get().load(podcast.image?.replace("localhost" , AdapterDiffUtil.URL)).placeholder(R.drawable.album_placeholder)
                .fit().centerCrop().into(binding.podcastImage)
            binding.root.setOnClickListener {
                info.interactionListener?.onItemClick(podcast , adapterPosition , SongAdapter.NO_OPTION)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastHomeViewholder {
        var binding = PodcastHomeViewpagerListitemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return PodcastHomeViewholder(binding)
    }

    override fun onBindViewHolder(holder: PodcastHomeViewholder, position: Int) {
       holder.bind(podcastLists[position])

    }

    override fun getItemCount(): Int {
        return podcastLists.size
    }
}