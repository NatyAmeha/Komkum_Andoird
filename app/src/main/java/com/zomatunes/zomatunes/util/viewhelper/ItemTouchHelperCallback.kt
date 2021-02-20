package com.zomatunes.zomatunes.util.viewhelper

import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.data.model.Song

class ItemTouchHelperCallback(var listener : ItemTouchHelperLIstener , var type : String) : ItemTouchHelper.Callback() {

    companion object{
       const val MOVE_ACTION = 0
       const val SWIPE_ACTION = 1
    }
    var action = MOVE_ACTION
//    var position = -1
//    var newPosition = -1

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
       var dragMovement = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        var swipeMovement = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragMovement , swipeMovement)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        action = MOVE_ACTION  //moveaction
//        position = viewHolder.adapterPosition
//        newPosition = target.adapterPosition
        listener.onMoved(viewHolder.adapterPosition , target.adapterPosition)
//        recyclerView.adapter?.notifyItemMoved(position , newPosition)
//        listener.onMoved(viewHolder.adapterPosition , target.adapterPosition)
//
        return false
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        action = SWIPE_ACTION
//        position = viewHolder.adapterPosition
        listener.onSwiped(viewHolder.adapterPosition)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return type == "QUEUE"
    }

    override fun isLongPressDragEnabled(): Boolean {
       return false
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
       viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
        Log.i("movereleased" , action.toString()  )
        if(action == MOVE_ACTION){
//            listener.onMoved(position , newPosition)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {

       viewHolder?.itemView?.setBackgroundColor(Color.LTGRAY)
    }


}