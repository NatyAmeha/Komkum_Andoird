package com.example.ethiomusic.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.jetbrains.annotations.NotNull
import java.util.*

@Parcelize
data class Album<Songtype , ArtistType>(

    var _id : String,
    var name: String? = null,
    var genre: String? = null,
    var category: String? = null,

    var language : List<String>? = null,
    var tags: List<String>? = null,

    var dateCreated: Date? = null,
    var albumCoverPath: String? = null,
    var exclusive : Boolean? = false,
    var favoriteCount: Int? = null,

    var songs: List<@RawValue Songtype>? = null,
    var artists: List<@RawValue ArtistType>? = null

) : BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category , baseImagePath = albumCoverPath) , Parcelable{

    constructor() : this("")

    fun toBaseModel() = BaseModel(baseId = _id , baseTittle = name , baseSubTittle = category , baseImagePath = albumCoverPath ,
        baseListOfInfo = artists as List<String> ,
        baseListofInfo2 = songs as List<String>)


}
@Entity
data class AlbumDbInfo(
    @PrimaryKey
    @NotNull
    var _id : String,
    var name : String ,
    var dateCreated : String,
    var albumCoverPath: String,
    var genre: String,
    var category: String
)

data class AlbumIdObject(var _id : String)
