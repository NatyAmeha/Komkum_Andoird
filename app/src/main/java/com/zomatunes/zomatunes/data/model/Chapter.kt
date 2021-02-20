package com.zomatunes.zomatunes.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
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

    var authorNames : String? = null,
    var currentPosition : Long = 0L, override

    var tags: List<String>? = null
) : Streamable<String,String> , Parcelable

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
    var currentPosition : Long = 0L   // for storing last posiiton to continue where the user left

)