package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.*


@Parcelize
@Keep
data class Podcast<T>(
    var _id: String,
    var name  :  String? =  null,
    var description  :  String? =  null,
    var publisher  : String? =  null,
    var publisherName : String? =  null,
    var category  :  List<String>? =  null,
    var subCategory  :  List<String>? =  null,
    var episods  :  @RawValue List<T>? =  null,
    var image  :  String? =  null,
    var date  : Date? = Date(),
    var podcastListeners  :  List<String>? =  null,
    var exclusive  : Boolean? = false,
    var isVerified  : Boolean? = true,
    var featured  : Boolean? = false,
    var isInFavorte : Boolean? = null,
    var lastChecked : Date = Date()
) : Parcelable{
    companion object{
        const val PODCAST_ACTION_PLAY = 0
        const val PODCAST_ACTION_PAUSE = 1
        const val PODCAST_ACTION_LIKE = 2
        const val PODCAST_ACTION_UNLIKE = 3
        const val PODCAST_ACTION_DOWNLOAD = 4
        const val PODCAST_ACTION_REMOVE_DOWNLOAD = 5
        const val PODCAST_ACTION_NONE = 6
    }
}


@Parcelize
@Keep
data class PodcastPublisher<PodcastType>(
    var _id: String,
    var name : String? =  null,
    var image : String? =  null,
    var description: String? = null,
    var podcasts : @RawValue List<PodcastType>? =  null,
    var isVerified : Boolean? =  null,
    var dateCreated : Date? =  null,
    var donationEnabled : Boolean? = true,
    var user : String? = null
) : Parcelable


@Keep
data class PodcastHomeViewmodel(
    var popularPodcasts:  List<Podcast<String>>? = null,
    var featuredPodcasts:  List<Podcast<String>>? = null,
    var podcastCategories:  List<String>? = null,
    var topPodcasterByDonation : List<DonationLeaderBoard>? = null,
//    var newEpisodes:  IPodcastEpisodeModel,
//    var trendingEpisodes:  IPodcastEpisodeModel[]
    var podcastsByCategory1:  List<Podcast<String>>? = null,
    var podcastsByCategory2:  List<Podcast<String>>? = null,
    var podcastsByCategory3:  List<Podcast<String>>? = null,
    var podcastsByCategory4:  List<Podcast<String>>? = null

)
