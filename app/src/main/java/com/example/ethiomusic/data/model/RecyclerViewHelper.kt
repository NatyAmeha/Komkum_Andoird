package com.example.ethiomusic.data.model

import androidx.lifecycle.LifecycleOwner
import com.example.ethiomusic.Downloader.DownloadTracker
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager

class RecyclerViewHelper<T>(
    var type : String?=null , var stateManager: RecyclerviewStateManager<T>?=null ,
    var interactionListener : IRecyclerViewInteractionListener<T>?=null , var owner: LifecycleOwner?=null ,
    var downloadTracker: DownloadTracker? = null , var listItemType : Int? = null
)