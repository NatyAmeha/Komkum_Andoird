package com.komkum.komkum.util.extensions

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.support.v4.media.session.MediaSessionCompat
import androidx.fragment.app.FragmentActivity
import com.komkum.komkum.data.model.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import android.view.WindowManager

import android.util.DisplayMetrics
import android.widget.Toast
import com.komkum.komkum.R


val <T> T.exhaustive: T
    get() = this


fun <T : BaseModel> List<T>.checkItemExistence(data: BaseModel): Boolean {
    return contains(data)
}

fun List<MediaSessionCompat.QueueItem>.toStreamable(): MutableList<Streamable<String, String>> {
    var description = map { queueItem -> queueItem.description }
    return description.map { desc ->
        var _id = desc.mediaId
        var tittle = desc.title.toString()
        var ownerName = desc.subtitle.toString()
        var imagepath = desc.iconUri
        var lyrics = desc.extras?.getString("LYRICS")
        var isAd = desc.extras?.getBoolean("IS_AD")
        var adType = desc.extras?.getInt("AD_TYPE") ?: Ads.AD_TYPE_EVENT
        var adContent = desc.extras?.getString("AD_CONTENT")
        if (isAd == true) Ads(
            _id = _id!!,
            tittle = tittle,
            adType = adType,
            ownerName = ownerName.split(","),
            adContent = adContent,
            thumbnailPath = imagepath.toString(),
            isAd = true
        )
        else Song<String, String>(
            _id = _id!!,
            tittle = tittle,
            ownerName = ownerName.split(","),
            thumbnailPath = imagepath.toString(),
            isAd = false
        )
    }.toMutableList()
}

fun SongDbInfo.toSong(): Song<String, String> {
    return Song(
        _id = this._id,
        tittle = this.tittle,
        trackNumber = this.trackNumber,
        trackLength = this.trackLength,
        thumbnailPath = this.thumbnailPath,
        songFilePath = this.songFilePath,
        mpdPath = this.mpdPath
    )
}

fun List<SongDbInfo>.toSong(): List<Song<String, String>> {
    return this.map { info ->
        Song<String, String>(
            _id = info._id,
            tittle = info.tittle,
            trackNumber = info.trackNumber,
            trackLength = info.trackLength,
            thumbnailPath = info.thumbnailPath,
            songFilePath = info.songFilePath,
            mpdPath = info.mpdPath,
            artistsName = info.artistName?.split(", ")
        )
    }
}

fun EpisodeDBInfo.toEpisode(): PodcastEpisode {
    return PodcastEpisode(
        this._id,
        this.tittle,
        this.description,
        this.image,
        this.duration,
        this.podcast,
        this.podcastName,
        Date(this.date),
        filePath = this.filePath,
        thumbnailPath = this.thumbnail
    )
}

fun Date.convertoLocalDate(removeYear: Boolean = false , longMonthName : Boolean = true): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return if (removeYear)
        "${cal.getDisplayName(Calendar.MONTH, if (longMonthName) Calendar.LONG else Calendar.SHORT, Locale.getDefault())} ${
            cal.get(
                Calendar.DATE
            )
        }"
    else "${cal.getDisplayName(Calendar.MONTH, if (longMonthName) Calendar.LONG else Calendar.SHORT, Locale.getDefault())} ${
        cal.get(
            Calendar.DATE
        )
    } ${cal.get(Calendar.YEAR)}"

}


fun Date.getRemainingDay(context : Context , onlyDay: Boolean = false , includeSec : Boolean = false): String? {
    var currentDate = Calendar.getInstance().time
    var remainingDate = this.time.minus(currentDate.time)
    return if (remainingDate > 0) {
        var days = TimeUnit.MILLISECONDS.toDays(remainingDate)
        var hours = (TimeUnit.MILLISECONDS.toHours(remainingDate) - TimeUnit.DAYS.toHours(
            TimeUnit.MILLISECONDS.toDays(remainingDate)
        ))
        var minute = (TimeUnit.MILLISECONDS.toMinutes(remainingDate) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(remainingDate)
        ))

        var sec = (TimeUnit.MILLISECONDS.toSeconds(remainingDate) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(remainingDate)
        ))



        if (onlyDay) "${days}${context.getString(R.string.day)}"
        else if(includeSec) "${days}${context.getString(R.string.day)} ${hours}${context.getString(R.string.hour)} " +
                "${minute}${context.getString(R.string.minute)} ${sec}${context.getString(R.string.second)}"
        else "${days}${context.getString(R.string.day)} ${hours}${context.getString(R.string.hour)} ${minute}${context.getString(R.string.minute)}"
    } else null

}


fun Date.getRemainingDay(context : Context): String? {
    var currentDate = Calendar.getInstance().time
    var remainingDate = this.time.minus(currentDate.time)
    return if (remainingDate > 0) {
        var days = TimeUnit.MILLISECONDS.toDays(remainingDate)
       "$days ${if(remainingDate > 1) "${ context.getString(R.string.days)}" else "${context.getString(R.string.day)}"}"

    } else null

}

fun Date.getRemainingDayInNumber(context : Context): Int {
    var currentDate = Calendar.getInstance().time
    var remainingDate = this.time.minus(currentDate.time)
    var days = TimeUnit.MILLISECONDS.toDays(remainingDate)
    return days.toInt()
}

fun Context.adjustFontScale(config : Configuration){
    if (config.fontScale > 1.1f) {
        config.fontScale = 1.1f
        var metrics = getResources().getDisplayMetrics()
        var wm =  getSystemService(WINDOW_SERVICE) as WindowManager
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = config.fontScale * metrics.density;
        this.resources.updateConfiguration(config, metrics)
    }
}


inline fun <reified T> List<T>.toBaseModel(): List<BaseModel>? {
    return return when (T::class) {
        Album::class -> {
            var albumList = this as List<Album<String, String>>
            albumList.map { album -> album.toBaseModel() }
        }

        else -> {
            null
        }
    }
}