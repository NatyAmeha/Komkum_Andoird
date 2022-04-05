package com.komkum.komkum.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

import java.util.*

@Parcelize
@Keep
data class Donation(
    var _id : String? = null,
    var amount : Int? = null,
    var doner : String? = null,
    var donerName : String? = null,
    var donerImage : String? = null,

    var recepient : String? = null,
    var recepientName : String? = null,

    var creator : String? = null,

    var donationFor : Int? = null,
    var recepientImage : String? = null,
    var created : Date? = null,

) : Parcelable{
    companion object{
        const val ARTIST_DONATION = 0
        const val PODCAST_DONATION = 1
        const val AUTHOR_DONATION = 2
    }


}

@Keep
data class DonationLeaderBoard(
    var totalAmount : String? = null,
    var participants : List<Leaderboard>
) : Leaderboard()

@Keep
open class Leaderboard(var _id : String? = null , var name: String? = null , var amount : Int? = null , var image: String? = null)

fun  List<Donation>.toLeaderboard(): List<Leaderboard> {
    var leaderboardREsult = this.map {
        Leaderboard(name = it.donerName , image = it.donerImage , amount = it.amount , _id = it.doner)
    }
    return leaderboardREsult
}

