package com.komkum.komkum.util.adaper

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.BrowseCategoryListItemBinding
import com.komkum.komkum.databinding.BrowseListItemBinding
import com.komkum.komkum.databinding.DownloadStyleListitemBinding
import com.komkum.komkum.databinding.MiniViewListItemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.RadioSpan
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import com.squareup.picasso.Picasso
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

class RadioAdapter(var info : RecyclerViewHelper<Radio>) : ListAdapter<Radio , RadioAdapter.RadioViewHolder>(RadioDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        var binding = DownloadStyleListitemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return RadioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RadioViewHolder(var binding : DownloadStyleListitemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(radio : Radio){
            binding.tittleTextview.text = radio.name
            var radioInfo = "by ${radio.creatorName}span${radio.listenersId?.size} Listeners"

            var spanner = Spanner(radioInfo).span("span" , Spans.custom { RadioSpan(binding.root.context , R.drawable.tab_item_not_active , binding.root.context.resources.getInteger(R.integer.bullet_margin)) })
            binding.subtittleTextview.text = spanner
            binding.radioDescriptionTextview.text = radio.description

           if(radio.stationType == Radio.STATION_TYPE_ARTIST) binding.radioDescriptionTextview.text = "Radio station curated by Komkum based on Artist ${radio.name} and related artists"
           else if(radio.stationType == Radio.STATION_TYPE_SONG) binding.radioDescriptionTextview.text = "Radio station curated by Komkum based on Song ${radio.name} and related Song moods and genre"


            binding.thumbnailImageview.alpha = 0.5f
            Picasso.get().load(radio.coverImage)
                .placeholder(R.drawable.radio_placeholder).fit().centerCrop().into(binding.thumbnailImageview)


            with(binding.root){
                setOnClickListener {
                   info.interactionListener!!.onItemClick(radio , adapterPosition , SongAdapter.NO_OPTION)
                }
            }
        }
    }
}


class BrowseAdapter(var info : RecyclerViewHelper<MusicBrowse> , var list : List<MusicBrowse>) : RecyclerView.Adapter< BrowseAdapter.BrowseViewHolder>() {
    var gradientList = listOf(R.drawable.gradent2 , R.drawable.gradient3 , R.drawable.gradient4 , R.drawable.subscription_gradient , R.drawable.gradient5,
    R.drawable.gradient6 , R.drawable.gradient7 , R.drawable.gradient8 , R.drawable.gradient9)
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseViewHolder {
        var binding = BrowseListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return BrowseViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return  list.size
    }

    override fun onBindViewHolder(holder: BrowseViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class BrowseViewHolder(var binding : BrowseListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(musicBrowse: MusicBrowse){
            binding.browseTitleTextview.text = musicBrowse.name
            binding.browseContainer.setBackgroundResource(gradientList[adapterPosition % gradientList.size])
            Picasso.get().load(musicBrowse.imagePath)
                .fit().centerCrop().rotate(45f).transform(RoundImageTransformation()).into(binding.browseImageView)
            info.stateManager?.multiselectedItems?.observe(info.owner!! , Observer{browseItemList ->
                binding.browseitemCheckedImageview.isVisible = browseItemList.contains(musicBrowse)
            })

            with(binding.root){
                setOnClickListener {
                    info.interactionListener?.onItemClick(musicBrowse , adapterPosition , SongAdapter.NO_OPTION)
                }
            }
        }
    }

}


class BrowseCategoryAdapter(var info : RecyclerViewHelper<MusicBrowse> ,  var browseCategoryMap : Map<String? , List<MusicBrowse>>) : RecyclerView.Adapter< BrowseCategoryAdapter.BrowseCategoryViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseCategoryViewHolder {
        var binding = BrowseCategoryListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return BrowseCategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return browseCategoryMap.size
    }

    override fun onBindViewHolder(holder: BrowseCategoryViewHolder, position: Int) {
       var key = this.browseCategoryMap.keys.toList()[position]
        var data = this.browseCategoryMap[key]
        data?.let {
            holder.bind(it)
        }
    }

    inner class BrowseCategoryViewHolder(var binding : BrowseCategoryListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(browseItem : List<MusicBrowse>){
//            if(browseItem[0].category.equals("GENRE" ,true))
                binding.browseCategoryTextview.visibility = View.INVISIBLE
//            else binding.browseCategoryTextview.text = browseItem[0].category?.toLowerCase()
            binding.browseItemRecyclerview.layoutManager = GridLayoutManager(binding.root.context ,  2)
            val browseAdapter = BrowseAdapter(info , browseItem)
            binding.browseItemRecyclerview.adapter = browseAdapter
        }
    }
}