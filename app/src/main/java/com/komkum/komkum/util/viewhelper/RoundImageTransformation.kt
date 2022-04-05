package com.komkum.komkum.util.viewhelper

import android.graphics.*
import com.squareup.picasso.Transformation

class RoundImageTransformation : Transformation {
    override fun key(): String {
        return "round"
    }

    override fun transform(source: Bitmap?) : Bitmap {
        var size = Math.min(source!!.width , source!!.height)
        var x = (source.width - size)/2
        var y = (source.height - size)/2
        var squareBitmap = Bitmap.createBitmap(source , x , y , size , size)
        if(squareBitmap != source
        ){
            source.recycle()
        }
        var bitmap = Bitmap.createBitmap(size , size , source.config)
        var canvas = Canvas(bitmap)
        var paint = Paint()
        var shader = BitmapShader(squareBitmap , Shader.TileMode.CLAMP , Shader.TileMode.CLAMP )
        paint.shader = shader
        paint.isAntiAlias = true

        var r = size/8f
        canvas.drawRoundRect(RectF(0.0f , 0.0f , source.width.toFloat() ,source.height.toFloat()) , r , r, paint)
        squareBitmap.recycle()
        return bitmap
    }
}

