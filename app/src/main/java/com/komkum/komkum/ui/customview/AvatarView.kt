package com.komkum.komkum.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.compose.ui.text.toUpperCase

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.palette.graphics.Palette
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.komkum.komkum.R
import com.komkum.komkum.databinding.AvatarViewBinding
import com.komkum.komkum.util.viewhelper.CircleTransformation

class AvatarView @JvmOverloads constructor(context : Context, attrSet : AttributeSet? = null, attrValue : Int = 0, attrRes : Int = 0)
    : ConstraintLayout(context , attrSet , attrValue , attrRes){

    var binding : AvatarViewBinding = AvatarViewBinding.inflate(LayoutInflater.from(context) ,  this)


    fun displayAvatar(imagePath : String?, placeholder : String, textSize : Float = 14f , size : Int = 150, backgroundColor : Int = R.drawable.circle){
//        binding.placeholderTextview.width = size
//        binding.placeholderTextview.height = size

        binding.placeholderTextview.textSize = textSize
        binding.placeholderTextview.text = placeholder.toUpperCase()
        binding.placeholderTextview.setBackgroundResource(backgroundColor)
        binding.avatarImaeview.setBackgroundResource(backgroundColor)


        var target = object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    binding.avatarImaeview.isVisible = true
                    binding.avatarImaeview.setImageBitmap(bitmap)
                    binding.placeholderTextview.visibility = View.INVISIBLE
                }
            }
            override fun onBitmapFailed(e: java.lang.Exception?, errorDrawable: Drawable?) {}
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        }
        if(!imagePath.isNullOrEmpty())Picasso.get().load(imagePath).transform(CircleTransformation()).into(target)
    }
    }