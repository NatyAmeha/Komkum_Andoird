package com.zomatunes.zomatunes.data.model

import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager

class RecyclerViewHelper<T>(
    var type : String?=null , var stateManager: RecyclerviewStateManager<T>?=null ,
    var interactionListener : IRecyclerViewInteractionListener<T>?=null , var owner: LifecycleOwner?=null ,
    var downloadTracker: DownloadTracker? = null , var listItemType : Int? = null,
    var playbackState : LiveData<PlaybackStateCompat>? = null, var layoutOrientation : Int? = null
){
    companion object{
        const val RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL = 0
        const val RECYCLERVIEW_ORIENTATION_GRID_2 = 1
        const val RECYCLERVIEW_ORIENTATION_GRID_3 = 2
        const val RECYCLERVIEW_ORIENTATION_LINEAR_VERTICAL = 3
    }
}