package com.example.ethiomusic.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.IMainActivity
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.Artist
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.MiniViewListItemBinding
import com.example.ethiomusic.util.adaper.AdapterDiffUtil
import com.example.ethiomusic.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class ArtistAdapter(var info: RecyclerViewHelper<Artist<String,String>>)
    : ListAdapter<Artist<String,String> , ArtistAdapter.ArtistViewHolder>(AdapterDiffUtil<Artist<String,String>>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
          if(info.type == "ARTIST") iMainActivity = recyclerView.context as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        var binding = MiniViewListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArtistViewHolder(var binding : MiniViewListItemBinding ) : RecyclerView.ViewHolder(binding.root){

        fun bind(artist : Artist<String,String>){
            binding.tittleTextview.text = artist.name
            binding.subtittleTextview.text = "${artist.followersCount} followers"


            var profileimage = artist.profileImagePath!![0]
            var a =  profileimage.replace("localhost" , "192.168.43.166")
            Picasso.get().load(a).transform(CircleTransformation()).placeholder(R.drawable.circularimg).fit().centerInside().into(binding.thumbnailImageview)


            info.owner?.let {
                info.stateManager?.multiselectedItems?.observe(it , Observer{selectedItems ->
                    binding.artistSelectedImageview.isVisible = selectedItems.contains(artist)
                })
            }
            clickLIstener(artist)
        }


        fun clickLIstener(artist : Artist<String,String>){
            with(binding.root){
                setOnClickListener {
                    info.interactionListener?.onItemClick(artist, adapterPosition,null)
                }
            }
        }


    }
}