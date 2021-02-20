package com.zomatunes.zomatunes.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.zomatunes.zomatunes.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.custom_playlist_imageview.view.*

class CustomPlaylistImageview @JvmOverloads constructor(context : Context ,  attrSet : AttributeSet? = null , attrValue : Int = 0 , attrRes : Int = 0)
    : ConstraintLayout(context , attrSet , attrValue , attrRes) {

    init {
        var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.custom_playlist_imageview , this)
    }

    fun loadImage(imageUris : List<String>){
        if(imageUris.isNullOrEmpty()){
            playlist_image_2.visibility = View.GONE
            playlist_image_3.visibility = View.GONE
            playlist_image_4.visibility = View.GONE
        }
        else if(imageUris.size < 4){
            Picasso.get().load(imageUris.first()).placeholder(R.drawable.ic_baseline_playlist_play_24)
                .fit().centerCrop().into(playlist_image_1)
            playlist_image_2.visibility = View.GONE
            playlist_image_3.visibility = View.GONE
            playlist_image_4.visibility = View.GONE
        }
        else{
            Picasso.get().load(imageUris[0]).placeholder(R.drawable.ic_baseline_playlist_play_24)
                .fit().centerCrop().into(playlist_image_1)
            Picasso.get().load(imageUris[1]).placeholder(R.drawable.ic_baseline_playlist_play_24)
                .fit().centerCrop().into(playlist_image_2)
            Picasso.get().load(imageUris[2]).placeholder(R.drawable.ic_baseline_playlist_play_24)
                .fit().centerCrop().into(playlist_image_3)
            Picasso.get().load(imageUris[3]).placeholder(R.drawable.ic_baseline_playlist_play_24)
                .fit().centerCrop().into(playlist_image_4)
        }
    }

}