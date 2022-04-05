package com.komkum.komkum.util.adaper

import androidx.recyclerview.widget.DiffUtil
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.DbDownloadInfo
import com.komkum.komkum.data.model.Radio

class AdapterDiffUtil<T : BaseModel> : DiffUtil.ItemCallback<T>() {

    companion object{
        const val URL = "api.zomatunes.com"
    }

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.baseId == newItem.baseId
    }
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
       return oldItem.equals(newItem)
    }
}


class DownloadDiffUtil() : DiffUtil.ItemCallback<DbDownloadInfo>(){
    override fun areItemsTheSame(oldItem: DbDownloadInfo, newItem: DbDownloadInfo): Boolean {
       return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: DbDownloadInfo, newItem: DbDownloadInfo): Boolean {
        return oldItem == newItem
    }

}

class RadioDiffUtil() : DiffUtil.ItemCallback<Radio>(){
    override fun areItemsTheSame(oldItem: Radio, newItem: Radio): Boolean {
        return oldItem._id == newItem._id
    }

    override fun areContentsTheSame(oldItem: Radio, newItem: Radio): Boolean {
        return oldItem == newItem
    }
}


