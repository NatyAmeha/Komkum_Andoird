package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Song
import com.komkum.komkum.databinding.HorizontalPlaylistListItemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.squareup.picasso.Picasso
import com.komkum.komkum.util.viewhelper.RadioSpan
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

class PlaylistAdapter(var recyclerviewInfo : RecyclerViewHelper<Playlist<String>> , var playlistList : List<Playlist<String>>) : RecyclerView.Adapter<PlaylistAdapter.HorizontalPlaylistViewHolder>(){

    companion object{
        const val HORIZONTAL_LIST_ITEM = 0
        const val VERTICAL_LIST_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalPlaylistViewHolder {
        var horizontalBinding = HorizontalPlaylistListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
         return HorizontalPlaylistViewHolder(horizontalBinding)
    }

    override fun getItemCount(): Int {
      return playlistList.size
    }

    override fun onBindViewHolder(holder: HorizontalPlaylistViewHolder, position: Int) {
        holder.bind(playlistList[position])
    }

    inner class HorizontalPlaylistViewHolder(var binding : HorizontalPlaylistListItemBinding) : RecyclerView.ViewHolder(binding.root){
         fun bind(playlist : Playlist<String>){
             binding.tittleTextview.text = playlist.name
//             binding.subtittleTextview.text = "playlist creator name"
             var songSize = if(playlist.type == "CHART") "${playlist.followersId?.size ?: 0} ${binding.root.context.getString(R.string.followers)}"  else  "${playlist.songs?.size} songs"
             var subtitle = "$songSize span ${playlist.creatorName?: "Komkum"}"
             var spanner = Spanner(subtitle).span("span" , Spans.custom { RadioSpan(binding.root.context , R.drawable.tab_item_not_active , binding.root.context.resources.getInteger(R.integer.bullet_margin)) })
             binding.descriptionTextview.text =  spanner

            if(!playlist.coverImagePath.isNullOrEmpty()){
                 var imageUri =  playlist.coverImagePath!!.distinct()
                 var imageview = binding.mediumThumbnailImageview
                 imageview.loadImage(imageUri.take(4))
//                 Picasso.get().load(imageUri).fit().centerCrop().into(binding.mediumThumbnailImageview)
            }
            else{
                binding.mediumThumbnailImageview.loadImage(listOf(R.drawable.music_placeholder.toString()))
            }
             with(binding.root){
                 setOnClickListener {
                     recyclerviewInfo.interactionListener?.onItemClick(playlist , adapterPosition , SongAdapter.NO_OPTION)
                 }
             }
         }
    }
}