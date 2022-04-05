package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.*
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

import org.jetbrains.annotations.NotNull
import java.util.*
@Keep
interface Streamable<T,K>{
    var _id: String
    var mpdPath : String?
    var songFilePath : String?
    var tittle: String?
    var genre: String?
    var lyrics : String?
    var ownerName : List<String>?
    var thumbnailPath: String?
    var tags: List<String>?

    var isAd : Boolean?
    var adType : Int?
    var adContent : String?
}

@Parcelize
@Keep
data class Song<AlbumType , ArtistType> constructor(
    override var _id: String,
    override var tittle: String? = null,
    override var genre: String? = null,

    var language : List<String>? = null,
    var category:String? = null,

    override var tags: List<String>? = null,
    var trackNumber: Int? = null,
    var trackLength: String? = null,
    var dateCreated: Date? = null,
    var songCredit: List<String>? = null,
    var type : Int? = null,
    var exclusive : Boolean? = false,
    var licenseKey: String? = null,


    override var thumbnailPath: String? = null,
    override var mpdPath: String? = null,
    override var songFilePath: String? = null,
    override var lyrics: String? = null,
    override var isAd: Boolean? = false,
    override var adType: Int? = null,
    override var adContent: String? = null,

    var album: @RawValue AlbumType? = null,
    var albumName : String? = null,

    var artists: List<@RawValue ArtistType>? = null,   // contain either array of artist id or artist object
    var artistsName : List<String>? = null,
    override var ownerName: List<String>? = listOf(genre ?: "genre name"),

    var totalStreamCount: Int? = 0,
    var monthlyStreamCount: Int? = 0,
    var totalDownloadCount: Int? = 0,
    var monthlyDownloadCount: Int? = 0,
    var previousMonthStreamCount: Int? = 0,
    var previousMonthDownloadCount: Int? = 0

) : BaseModel(baseId = _id , baseTittle = tittle , baseSubTittle = genre , baseImagePath = thumbnailPath)  , Streamable<String , String> ,  Parcelable{
    constructor() : this("")
    init {
        this.ownerName = artistsName
    }
    fun toBaseModel() = BaseModel(baseId = _id , baseTittle = tittle , baseSubTittle = genre , baseImagePath = thumbnailPath)
}




@Entity(
    foreignKeys = [
        ForeignKey(entity = AlbumDbInfo::class , parentColumns = arrayOf("_id") , childColumns = arrayOf("albumId")   , onDelete = ForeignKey.NO_ACTION),
        ForeignKey(entity = PlaylistDbInfo::class , parentColumns = arrayOf("_id") , childColumns = arrayOf("playlistId") ,  onDelete = ForeignKey.NO_ACTION)
    ],
    indices = [
        Index("albumId" , "playlistId")
    ])
data class SongDbInfo(
    @PrimaryKey
    @NotNull
    val _id : String,
    var tittle: String? = null,
    var trackNumber: Int? = null,
    var trackLength: String? = null,
    var dateCreated: String? = null,
    var thumbnailPath: String? = null,
    var songFilePath: String? = null,
    var mpdPath: String? = null,
    var artistName : String? = null,
    var albumName : String? = null,
    var albumId : String? = null, // for storing album id which is always string to the db
    var playlistName : String? = null,
    var playlistId : String? = null,  // for storing playlist id which is always string to the db
    var lyricsPath: String? = null
    )

