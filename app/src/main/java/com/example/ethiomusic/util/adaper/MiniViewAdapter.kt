package com.example.ethiomusic.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.IMainActivity
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.databinding.MiniViewListItemBinding
import com.example.ethiomusic.util.viewhelper.CircleTransformation
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager
import com.squareup.picasso.Picasso

class MiniViewAdapter<T : BaseModel>(var stateManager: RecyclerviewStateManager<T>? = null , var interaction : IRecyclerViewInteractionListener<T>? = null,
                                     var owner: LifecycleOwner? = null , var type : String? = null) : ListAdapter<T , MiniViewAdapter<T>.MiniViewHolder>(
    AdapterDiffUtil<T>()
) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        iMainActivity = recyclerView.context as MainActivity
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
            binding.subtittleTextview.text = data.baseSubTittle
            var imageurl = data.baseImagePath?.replace("localhost" , "192.168.43.166")

            if(data.baseSubTittle == "ARTIST" || type =="ARTIST") Picasso.get().load(imageurl).transform(
                CircleTransformation()
            ).placeholder(R.drawable.circularimg).fit().centerCrop().into(binding.thumbnailImageview)
            else Picasso.get().load(imageurl).placeholder(R.drawable.circularimg).fit().centerCrop().into(binding.thumbnailImageview)

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
                        if((it == "ALBUM")){
                            iMainActivity.movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false)
                        }
                        else if(data.baseSubTittle =="ARTIST"){
                            iMainActivity.moveToArtist(data.baseId!!)
                        }
//                        else if(type == "ALBUM") iMainActivity.movefromArtisttoAlbum(data.baseId!!)
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