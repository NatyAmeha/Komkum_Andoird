package com.example.ethiomusic.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.data.model.Playlist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.data.model.Song
import com.example.ethiomusic.databinding.HorizontalPlaylistListItemBinding
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.squareup.picasso.Picasso

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
             binding.descriptionTextview.text  = "${playlist.songs?.size} songs"
             playlist.coverImagePath?.let{
                 var imageUri =  it[0].replace("localhost" , "192.168.43.166")
                 Picasso.get().load(imageUri).fit().centerCrop().into(binding.mediumThumbnailImageview)
             }
             with(binding.root){
                 setOnClickListener {
                     recyclerviewInfo.interactionListener?.onItemClick(playlist , adapterPosition , SongAdapter.NO_OPTION)
                 }
             }
         }
    }
}