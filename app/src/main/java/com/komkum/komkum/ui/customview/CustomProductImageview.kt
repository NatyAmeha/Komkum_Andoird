package com.komkum.komkum.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.komkum.komkum.R
import com.komkum.komkum.databinding.CustomProductImageViewBinding
import com.squareup.picasso.Picasso

class CustomProductImageview @JvmOverloads constructor(context : Context, attrSet : AttributeSet? = null, attrValue : Int = 0, attrRes : Int = 0)
    : ConstraintLayout(context , attrSet , attrValue , attrRes) {


    var binding : CustomProductImageViewBinding =
        CustomProductImageViewBinding.inflate(LayoutInflater.from(context) , this)

    fun loadImage(imageUris : List<String> , placeholder : Int? = null){
        binding.root.setBackgroundColor(context.resources.getColor(R.color.light_background))
        if(imageUris.isEmpty()){
            Picasso.get().load(placeholder ?:  R.drawable.product_placeholder)
                .fit().into(binding.productImage1)
        }
        else if(imageUris.size == 1){
            binding.productImage2.isVisible = false
            binding.productImage3.isVisible = false
            binding.productImage4.isVisible = false
            Picasso.get().load(imageUris.getOrNull(0)).placeholder(placeholder ?: R.drawable.product_placeholder)
                .fit().into(binding.productImage1)
        }

        else{
            Picasso.get().load(imageUris.getOrNull(0)).placeholder(placeholder ?: R.drawable.product_placeholder)
                .fit().into(binding.productImage1)
            Picasso.get().load(imageUris.getOrNull(1)).placeholder(placeholder ?: R.drawable.product_placeholder)
                .fit().into(binding.productImage2)
            Picasso.get().load(imageUris.getOrNull(2)).placeholder(placeholder ?: R.drawable.product_placeholder)
                .fit().into(binding.productImage3)
            Picasso.get().load(imageUris.getOrNull(3)).placeholder(placeholder ?: R.drawable.product_placeholder)
                .fit().into(binding.productImage4)
        }
    }
}