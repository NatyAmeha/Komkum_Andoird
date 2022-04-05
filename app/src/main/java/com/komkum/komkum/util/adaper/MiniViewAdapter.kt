package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.databinding.MiniViewListItemBinding
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.Radio
import dagger.hilt.android.internal.managers.ViewComponentManager

class MiniViewAdapter<T : BaseModel>(var stateManager: RecyclerviewStateManager<T>? = null , var interaction : IRecyclerViewInteractionListener<T>? = null,
                                     var owner: LifecycleOwner? = null , var type : String? = null)
    : ListAdapter<T , MiniViewAdapter<T>.MiniViewHolder>(AdapterDiffUtil<T>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        var fragmentContextWrapper : ViewComponentManager.FragmentContextWrapper = recyclerView.context as ViewComponentManager.FragmentContextWrapper
        iMainActivity = fragmentContextWrapper.fragment.requireActivity() as MainActivity
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniViewHolder {
        var binding = MiniViewListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return MiniViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MiniViewHolder, position: Int) {
       holder.bind(getItem(position))
    }


    inner class MiniViewHolder(var binding : MiniViewListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : T){
            binding.tittleTextview.text = data.baseTittle
            binding.subtittleTextview.text = data.baseSubTittle?.toLowerCase()
            var imageurl = data.baseImagePath?.replace("localhost" , AdapterDiffUtil.URL)

            if(type == "RECENT"){
                binding.tittleTextview.maxLines = 1
                binding.subtittleTextview.maxLines = 1
            }

            when{
                (data.baseSubTittle == "ARTIST") || (type =="ARTIST") -> {
                    Picasso.get().load(imageurl).transform(CircleTransformation()).placeholder(R.drawable.artist_placeholder)
                        .fit().centerCrop().into(binding.thumbnailImageview)
                }
                data.baseSubTittle == "ALBUM"  || type =="ALBUM" ->{
                    Picasso.get().load(imageurl).placeholder(R.drawable.album_placeholder)
                        .fit().centerCrop().into(binding.thumbnailImageview)

                }
                data.baseSubTittle == "PLAYLIST" -> {
                    Picasso.get().load(imageurl).placeholder(R.drawable.music_placeholder)
                        .fit().centerCrop().into(binding.thumbnailImageview)
                }
                data.baseSubTittle == "RADIO" ->{
                    Picasso.get().load(R.drawable.radio_placeholder).placeholder(R.drawable.radio_placeholder)
                        .fit().centerCrop().into(binding.thumbnailImageview)

                }
                data.baseSubTittle == "LIKED_SONG" -> {
                    Picasso.get().load(R.drawable.ic_baseline_favorite_24).fit().placeholder(R.drawable.ic_baseline_favorite_24).into(binding.thumbnailImageview)
                    binding.thumbnailImageview.setBackgroundResource(R.drawable.gradent2)
                }
                else ->   Picasso.get().load(imageurl).placeholder(R.drawable.music_placeholder)
                    .fit().centerCrop().into(binding.thumbnailImageview)
            }

            owner?.let {
                stateManager?.multiselectedItems?.observe(it, Observer{ selectedItems ->
                    binding.artistSelectedImageview.isVisible = selectedItems.contains(data)
                })
            }

            setupclickListener(data)
        }


        fun setupclickListener(data : T){
            with(binding.root){
                setOnClickListener {

                    interaction?.onItemClick(data, adapterPosition,null)

                    data.baseSubTittle?.let {
                        if((it == "ALBUM")){ iMainActivity.movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false) }
                        else if(it == "LIKED_SONG") iMainActivity.movetoPlaylist(PlaylistFragment.LOAD_LIKED_SONG , loadFromCache = false)
                        else if(it == "PLAYLIST") iMainActivity.movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.baseId , loadFromCache = false)
                        else if(it =="ARTIST"){ iMainActivity.moveToArtist(data.baseId!!) }
                        else if(it == "RADIO"){
                            var radioData = Radio(_id = data.baseId)
                            iMainActivity.movetoSingleRadioStation(null , radioData)
                        }
                        else {}
                    }
                }

                setOnLongClickListener {
                    interaction?.activiateMultiSelectionMode()
                    interaction?.onItemClick(data, adapterPosition,null)
                    true
                }

            }
        }
    }

}