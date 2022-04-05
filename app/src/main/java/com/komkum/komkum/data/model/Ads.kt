package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize


@Parcelize
@Keep
data class Ads(
    override var _id: String,
    override var mpdPath: String? = null,
    override var songFilePath: String? = null,

    override var genre: String? = null,
    override var lyrics: String? = null,
    override var tags: List<String>? = null,

    override var tittle: String?,
    override var ownerName: List<String>?,
    override var thumbnailPath: String?,
    override var isAd: Boolean? = true,
    override var adContent: String? = null,
    var advertiser: String? = null,
    override var adType: Int? = null,

    var subtitle : String? = null,
    var video: String? = null,
    var banner: String? = null,
    var link: String? = null,

    var trivias : List<String>? = null,
    var prize: Int? = null,
    var minPlayer: Int? = null,
    var maxTeam : Int? = null,
    var discount : Int? = null,
    var discountFor : Int? = null,
    var duration : Int? = null,
    var description: String? = null,
    var teams : String? = null
) : Parcelable, Streamable<String,String>{
    companion object {
        const val AD_TYPE_PRODUCT = 0
        const val AD_TYPE_EVENT = 1
        const val AD_TYPE_GAME = 2
    }

    init {
        songFilePath = mpdPath
    }
}
