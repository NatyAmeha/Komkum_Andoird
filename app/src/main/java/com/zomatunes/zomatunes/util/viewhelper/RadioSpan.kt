package com.zomatunes.zomatunes.util.viewhelper

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import androidx.annotation.DrawableRes
import com.zomatunes.zomatunes.R

class RadioSpan(var context: Context, @DrawableRes var resourceId: Int) : DynamicDrawableSpan() {

    override fun getDrawable(): Drawable? {
        val drawable = context.getDrawable(resourceId)
        drawable?.setBounds(0, 0, 50, 50)
        return drawable
    }
}