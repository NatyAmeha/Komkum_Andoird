package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.PodcastListItemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter

class PodcastAdapter(var info : RecyclerViewHelper<Podcast<String>> , var podcastList : List<Podcast<String>> )
: RecyclerView.Adapter<PodcastAdapter.PodcastViewholder>(){

    inner class PodcastViewholder(var binding : PodcastListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(podcast : Podcast<String>){
            binding.podcastNameTextview.text = podcast.name
            binding.podcastPublisherTextview.text = podcast.publisherName
            Picasso.get().load(podcast.image).placeholder(R.drawable.podcast_placeholder)
                .fit().centerCrop().into(binding.podcastCoverImageview)


            binding.root.setOnClickListener {
                info.interactionListener?.onItemClick(podcast , adapterPosition, SongAdapter.NO_OPTION)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewholder {
        var binding = PodcastListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return PodcastViewholder(binding)
    }

    override fun onBindViewHolder(holder: PodcastViewholder, position: Int) {
       holder.bind(podcastList[position])
    }

    override fun getItemCount(): Int {
       return podcastList.size
    }
}