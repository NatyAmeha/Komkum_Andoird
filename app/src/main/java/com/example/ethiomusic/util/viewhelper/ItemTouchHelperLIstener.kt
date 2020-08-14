package com.example.ethiomusic.util.viewhelper

import android.content.Context

interface ItemTouchHelperLIstener {
    fun onSwiped(position : Int)
    fun onMoved(prevPosition : Int , newPosition : Int)

}