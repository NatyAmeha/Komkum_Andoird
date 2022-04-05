package com.komkum.komkum.ui.album.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Artist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.MiniViewListItemBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class ArtistAdapter(var info: RecyclerViewHelper<Artist<String,String>>)
    : ListAdapter<Artist<String,String> , ArtistAdapter.ArtistViewHolder>(AdapterDiffUtil<Artist<String,String>>()) {

//    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//          if(info.type == "ARTIST") iMainActivity = recyclerView.context as MainActivity
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
            binding.subtittleTextview.text = "${artist.followersCount} ${binding.root.context.getString(R.string.followers)}"


            var profileimage = artist.profileImagePath?.get(0)
            var a =  profileimage?.replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(a).transform(CircleTransformation()).placeholder(R.drawable.artist_placeholder)
                .fit().centerCrop().into(binding.thumbnailImageview)


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