package com.komkum.komkum.util.viewhelper

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import androidx.annotation.DrawableRes
import com.komkum.komkum.R

class RadioSpan(var context: Context, @DrawableRes var resourceId: Int , var bound : Int = 50) : DynamicDrawableSpan() {

    override fun getDrawable(): Drawable? {
        val drawable = context.resources.getDrawable(resourceId)
        drawable?.setBounds(0, 0, bound , bound)
        return drawable
    }
}