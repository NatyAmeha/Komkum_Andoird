package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

import org.jetbrains.annotations.NotNull

@Parcelize
@Keep
data class Chapter(
    override var _id: String,
    var chapterNumber: Int? = null,
    var name: String? = null,
    var duration: Int? = null,
    override var mpdPath: String? = null,
    var chapterfilePath: String? = null,
    override var songFilePath: String?,
    override var tittle: String? = name,
    override var genre: String? = "",
    override var thumbnailPath: String? = "",
    override var lyrics: String? = "",
    override var isAd: Boolean? = false,
    override var adType: Int? = null,
    override var adContent: String? = null,

    var authorNames : String? = null,
    var currentPosition : Long = 0L,
    var completed: Boolean = false,
    override var tags: List<String>? = null,
    override var ownerName: List<String>? = listOf("Author names")
) : Streamable<String,String> , Parcelable{
    init {
        this.ownerName = authorNames?.split(",")
    }
}

@Entity(
    foreignKeys = [
      ForeignKey(entity = AudioBookDbInfo::class , childColumns = ["bookId"] , parentColumns = ["_id"] , onDelete = ForeignKey.NO_ACTION)
    ]
)
data class ChapterDbInfo(
    @PrimaryKey
    @NotNull
    var _id: String,
    var chapterNumber: Int,
    var name: String,
    var duration: Int,
    var mpdPath: String,
    var chapterfilePath: String,
    var bookId: String,
    var currentPosition : Long = 0L,   // for storing last posiiton to continue where the user left
    var completed : Boolean = false

)