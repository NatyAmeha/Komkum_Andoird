package com.example.ethiomusic.util.viewhelper

interface IRecyclerViewInteractionListener<T> {
    fun onItemClick(data: T, position: Int, option: Int?)
    fun activiateMultiSelectionMode()
    fun onSwiped(position: Int)
    fun onMoved(prevPosition : Int , newPosition : Int)
}