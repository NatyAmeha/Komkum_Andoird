package com.example.ethiomusic.util.viewhelper

import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.example.ethiomusic.R
import com.google.android.exoplayer2.offline.Download


sealed class RecyclerviewState {
    class MultiSelectionState : RecyclerviewState()
    class SingleSelectionState : RecyclerviewState()
    class DownloadState : RecyclerviewState()
}


class RecyclerviewStateManager<T> {
    var state = MutableLiveData<RecyclerviewState>(RecyclerviewState.SingleSelectionState())
    var isStateinMultiSelectionMode = state.switchMap { istate ->
        liveData {
           if(istate is RecyclerviewState.MultiSelectionState) emit(true)
            else  emit(false)
        }
    }
    var isStateInDownloadState = state.switchMap { istate ->
        liveData {
            if(istate is RecyclerviewState.DownloadState) emit(true)
            else  emit(false)
        }
    }



    var selectedItem = MutableLiveData<T>()

    var _multiselectedItems = MutableLiveData<List<T>>(emptyList())
    var multiselectedItems : LiveData<List<T>> = _multiselectedItems

    var _downloadingItems = MutableLiveData<List<Download>>()

    fun changeState(value: RecyclerviewState) {
        _multiselectedItems.value = emptyList()
        selectedItem.value = null
        state.value = value
    }

    fun addOrRemoveItem(item: T) : Int {
        var currentList = _multiselectedItems.value?.toMutableList() ?: mutableListOf()
        if (currentList.contains(item)) currentList.remove(item)
        else currentList.add(item)
        _multiselectedItems.value = currentList
        return (_multiselectedItems.value as MutableList<T>).size
    }

    fun removeFromDownload(songUri : String){
        if(!_downloadingItems.value.isNullOrEmpty()){
            var downloadList = _downloadingItems.value!!.toMutableList()
            var download = downloadList.find { download -> download.request.uri == Uri.parse(songUri) }
            downloadList.remove(download)
            _downloadingItems.value = downloadList
        }
    }
}


fun createAlertDialog(context : Context , positiveTitle : String , message : String  , positiveAction : () -> Unit) : AlertDialog{
    var alertDialog = AlertDialog.Builder(context)
        .setTitle("Create Playlist")
        .setIcon(R.drawable.ic_audiotrack_black_24dp)
        .setMessage(message)
        .setPositiveButton("Create") { _, _ ->
            positiveAction()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
    return alertDialog.create()
}


