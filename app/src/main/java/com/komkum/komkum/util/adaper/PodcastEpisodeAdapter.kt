package com.komkum.komkum.util.adaper

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Podcast
import com.komkum.komkum.data.model.PodcastEpisode
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.PodcastEpisodeListItemBinding
import java.util.*

class PodcastEpisodeAdapter(var info : RecyclerViewHelper<PodcastEpisode> , var episodes : List<PodcastEpisode> , var podcastUpdateDate : Date? = null) : RecyclerView.Adapter<PodcastEpisodeAdapter.PocastEpisodeViewholer>() {

    inner class PocastEpisodeViewholer(var binding : PodcastEpisodeListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(episode : PodcastEpisode){
            binding.lifecycleOwner = info.owner
            binding.episode = episode
            binding.info = info
            binding.episodeTitleTextview.text = episode.tittle
            binding.episodeSdescriptionTextview.text = episode.description
            binding.episodeDurationImageview.text = episode.duration?.toLong()?.let { DateUtils.formatElapsedTime(it) }
            Picasso.get().load(episode.thumbnailPath?.replace("localhost" , AdapterDiffUtil.URL))
                .placeholder(R.drawable.ic_baseline_mic_24).fit().centerCrop().into(binding.episodeImageview)

            val cal = Calendar.getInstance()
             episode.dateCreated?.let{
                 binding.newEpisodeBadgeView.isVisible = (podcastUpdateDate ?: Date()) < it
                 cal.time = it
                 binding.episodeReleaseDateTextview.text = "${cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())}  ${cal.get(Calendar.DATE)}"

             }
            if(episode.isInFavorte == true){
                binding.likeEpisodeImageview.visibility = View.INVISIBLE
                binding.unlikeEpisodeImageview.visibility = View.VISIBLE
            }
            else{
                binding.likeEpisodeImageview.visibility = View.VISIBLE
                binding.unlikeEpisodeImageview.visibility = View.INVISIBLE
            }

            info.stateManager?.selectedItem?.observe(info.owner!!){
                binding.selected = it == episode
            }

            if(episode.isInDownloads== true){
              binding.downloadEpisodeImageview.visibility = View.INVISIBLE
              binding.removeDownloadImageview.visibility = View.VISIBLE
            }
            else{
                binding.downloadEpisodeImageview.visibility = View.VISIBLE
                binding.removeDownloadImageview.visibility = View.INVISIBLE
            }



            binding.root.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_NONE)
            }

            binding.playEpisodeImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_PLAY)
            }
            binding.pauseEpisodeImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_PAUSE)
            }

            binding.likeEpisodeImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_LIKE)
            }

            binding.unlikeEpisodeImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_UNLIKE)
            }

            binding.downloadEpisodeImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_DOWNLOAD)
            }
            binding.removeDownloadImageview.setOnClickListener {
                info.interactionListener?.onItemClick(episode , absoluteAdapterPosition , Podcast.PODCAST_ACTION_REMOVE_DOWNLOAD)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PocastEpisodeViewholer {
       var binding = PodcastEpisodeListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return PocastEpisodeViewholer(binding)
    }

    override fun onBindViewHolder(holder: PocastEpisodeViewholer, position: Int) {
        holder.bind(episodes[position])
    }


    override fun getItemCount(): Int {
        return episodes.size
    }
}