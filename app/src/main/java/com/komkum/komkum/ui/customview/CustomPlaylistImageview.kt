package com.komkum.komkum.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.komkum.komkum.R
import com.squareup.picasso.Picasso
import com.komkum.komkum.databinding.CustomPlaylistImageviewBinding


class CustomPlaylistImageview @JvmOverloads constructor(context : Context ,  attrSet : AttributeSet? = null , attrValue : Int = 0 , attrRes : Int = 0)
    : ConstraintLayout(context , attrSet , attrValue , attrRes) {

    var binding : CustomPlaylistImageviewBinding =
        CustomPlaylistImageviewBinding.inflate(LayoutInflater.from(context) , this)

    init {
        //        var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        inflater.inflate(R.layout.custom_playlist_imageview , this)
    }

    fun loadImage(imageUris : List<String> , placeholder : Int? = null){
        if(imageUris.isEmpty()){
            Picasso.get().load(placeholder ?:  R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage1)
        }
        else if(imageUris.size < 4){
            Picasso.get().load(imageUris.first()).placeholder(placeholder ?: R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage1)

        }
        else{
            binding.playlistImage2.visibility = View.VISIBLE
            binding.playlistImage3.visibility = View.VISIBLE
            binding.playlistImage4.visibility = View.VISIBLE
            Picasso.get().load(imageUris[0]).placeholder(placeholder ?: R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage1)
            Picasso.get().load(imageUris[1]).placeholder(placeholder ?: R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage2)
            Picasso.get().load(imageUris[2]).placeholder(placeholder ?: R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage3)
            Picasso.get().load(imageUris[3]).placeholder(placeholder ?: R.drawable.music_placeholder)
                .fit().centerCrop().into(binding.playlistImage4)
        }
    }

}