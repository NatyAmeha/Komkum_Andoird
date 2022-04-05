package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity

import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import org.jetbrains.annotations.NotNull

import java.util.*

@Parcelize
@Keep
data class PodcastEpisode (
    override var _id: String,
    override var tittle : String? = null,
    var description  : String? = null,
    var image : String? = null,
    var duration : Int? = 0,
    var podcast : String? = null,
    var podcastName : String? = null,
    var dateCreated : Date? = Date(),
    override var mpdPath : String? = null,
    var filePath : String? = null,

    var comments : MutableList<EpisodeComment<String>>? = null,

    var totalStreamCount: Int? = 0,
    var totalDownloadCount: Int? = 0,
    var likeCount : Int? = 0,

    var isInFavorte : Boolean? = false,
    var isInDownloads : Boolean? = false,
    var isPlayed : Boolean? = false,

    //unwanted fields
    override var songFilePath: String? = null,
    override var genre: String? = null,
    override var lyrics: String? = null,
    override var ownerName: List<String>? = null,
    override var thumbnailPath: String? = null,
    override var tags: List<String>? = null,
    override var isAd: Boolean? = false,
    override var adType: Int? = null,
    override var adContent: String? = null
) : Streamable<String , String> ,  Parcelable{
    init {
        this.songFilePath = this.filePath
        this.ownerName = listOf(this.podcastName!!)
    }
}


@Parcelize
@Keep
data class EpisodeComment<T>(
    var _id: String? = null,
    var text : String? = null,
    var date : Date? = Date(),
    var userId : @RawValue T? = null,
    var username : String? = null,
    var profileImage : String? = null,
    var replies : @RawValue List<CommentReply>? = null
): Parcelable

@Parcelize
@Keep
data class CommentReply(
    var text : String? = null,
    var date : Date? = Date()
    ):Parcelable{}


@Entity
data class EpisodeDBInfo(
    @PrimaryKey
    @NotNull
    var _id: String,
    var tittle : String? = null,
    var description  : String? = null,
    var image : String? = null,
    var duration : Int? = 0,
    var podcast : String? = null,
    var podcastName : String? = null,
    var mpdPath : String? = null,
    var filePath : String? = null,
    var thumbnail : String? = null,
    var date : String? = null,
    var currentPosition : Long = 0L   // for storing last posiiton to continue where the user left
//    var completed : Boolean = false
){}