package com.zomatunes.zomatunes.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.zomatunes.zomatunes.R

class CustomCharImageview @JvmOverloads constructor(context : Context, attrSet : AttributeSet? = null, attrValue : Int = 0, attrRes : Int = 0)
    : ConstraintLayout(context , attrSet , attrValue , attrRes) {

    init {
        var inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.custom_chart_imageview, this)
    }
}