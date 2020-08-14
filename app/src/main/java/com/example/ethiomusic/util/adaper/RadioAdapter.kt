package com.example.ethiomusic.util.adaper

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.IMainActivity
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.MusicBrowse
import com.example.ethiomusic.data.model.Radio
import com.example.ethiomusic.data.model.RecyclerViewHelper
import com.example.ethiomusic.databinding.BrowseCategoryListItemBinding
import com.example.ethiomusic.databinding.BrowseListItemBinding
import com.example.ethiomusic.databinding.DownloadStyleListitemBinding
import com.example.ethiomusic.databinding.MiniViewListItemBinding
import com.example.ethiomusic.ui.album.adapter.SongAdapter
import com.squareup.picasso.Picasso

class RadioAdapter : ListAdapter<Radio , RadioAdapter.RadioViewHolder>(RadioDiffUtil()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        iMainActivity = recyclerView.context as MainActivity
    }

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
            binding.subtittleTextview.text = "by ${radio.creatorName} - ${radio.listenersId?.size} Listeners"
            binding.radioDescriptionTextview.text = radio.description
            Picasso.get().load(R.drawable.backimage).fit().centerCrop().into(binding.thumbnailImageview)

            with(binding.root){
                setOnClickListener {
                    iMainActivity.movetoSingleRadioStation(null , radio)
                }
            }
        }
    }
}


class BrowseAdapter(var info : RecyclerViewHelper<MusicBrowse> , var list : List<MusicBrowse>) : RecyclerView.Adapter< BrowseAdapter.BrowseViewHolder>() {
    var gradientList = listOf(R.drawable.gradent2 , R.drawable.gradient3 , R.drawable.gradient4 , R.drawable.subscription_gradient , R.drawable.gradient5)
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
            Picasso.get().load(musicBrowse.imagePath?.replace("localhost" , "192.168.43.166")).rotate(45f).fit().centerCrop().into(binding.browseImageView)
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
            binding.browseCategoryTextview.text = browseItem[0].category
            binding.browseItemRecyclerview.layoutManager = GridLayoutManager(binding.root.context ,  2)
            var browseAdapter = BrowseAdapter(info , browseItem)
            binding.browseItemRecyclerview.adapter = browseAdapter
        }
    }
}