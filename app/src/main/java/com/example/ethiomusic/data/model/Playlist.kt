package com.example.ethiomusic.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.jetbrains.annotations.NotNull
import java.util.*

@Parcelize

data class Playlist<T> constructor(
    var _id: String? = null,
    var sid : String? = null,
    var name: String? = null,
    var creatorId: String? = null,
    var coverImagePath : List<String>? = null,
    var date: Date? = null,
    var songs: List<@RawValue T>? = null,
    var isPublic: Boolean? = null,
    var followerCount: Int? = 0
) : Parcelable ,  BaseModel(baseId = _id , baseTittle = name){
    constructor() : this("")
    fun toBaseModel() : BaseModel{
        var image = coverImagePath?.let {
            if (it.isNotEmpty())  return@let it.first()
            else return@let null
        }
        return  BaseModel(baseId = _id , baseTittle = name , baseSubTittle = "${songs?.size} songs"  ,  baseImagePath = image)
    }

}

@Entity
data class PlaylistDbInfo(
    @PrimaryKey
    @NotNull
    var _id : String ,
    var name : String ,
    var dateCreated : String
)



