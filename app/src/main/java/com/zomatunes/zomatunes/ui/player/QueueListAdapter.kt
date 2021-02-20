package com.zomatunes.zomatunes.ui.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.player_image_list_item.view.*

class QueueListAdapter(var imageList : List<String>) : RecyclerView.Adapter<QueueListAdapter.QueueViewholder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewholder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.player_image_list_item ,  parent , false)
        return QueueViewholder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: QueueViewholder, position: Int) {
        var imageUrl = imageList[position].replace("localhost" , AdapterDiffUtil.URL)
        Picasso.get().load(imageUrl).placeholder(R.drawable.ic_audiotrack_black_24dp).fit().centerInside().into(holder.imageview)
    }

    inner class QueueViewholder(view : View) : RecyclerView.ViewHolder(view){
             var imageview = view.queue_song_image_view
    }
}