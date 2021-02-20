package com.zomatunes.zomatunes.data.model

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
    var creatorName : String? = null,
    var coverImagePath : List<String>? = null,
    var date: Date? = null,
    var songs: List<@RawValue T>? = null,
    var type : String? = null,        //value = CHART or null
    var category : String? = null,   // used also for chart selection
    var language : String? = null,   // used also for chart selection
    var filter : String? = null,    // used for chart selection based on today stream ,  monthly stream ,  most liked   value = Today , Month , liked
    var isPublic: Boolean? = null,
    var followersId: List<String>? = null
) : Parcelable ,  BaseModel(baseId = _id , baseTittle = name){
    constructor() : this("")
    fun toBaseModel() : BaseModel{
        var image = coverImagePath?.let {
            if (it.isNotEmpty())  return@let it.first()
            else return@let null
        }
        return  BaseModel(baseId = _id , baseTittle = name , baseSubTittle = "${songs?.size} songs" , baseListOfInfo = followersId  ,
            baseListofInfo2 = songs as List<String> ,  baseImagePath = image)
    }

    companion object{
        const val LOAD_USER_PLAYLIST = 0
        const val FROM_PERVIOUS_FRAGMENT = 1
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



