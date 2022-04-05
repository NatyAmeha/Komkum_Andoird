package com.komkum.komkum.data.model

import android.content.Context
import androidx.annotation.Keep
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem

@Keep
data class MenuItem(var icon : Int , var title : String , var order : Int? = null){
    companion object{
        const val DOWNLOAD_SONG = 0
        const val SONG_RADIO = 1
        const val ARTIST_RADIO = 2
        const val GO_TO_ALBUM = 3
        const val GO_TO_ARTIST = 4
        const val SONG_INFO = 5
        const val ADD_TO = 6

        const val DOWNLOAD_EPISODE = 7
        const val LIKE_EPISODE = 8
        const val UNLIKE_EPISODE = 9
        const val GO_TO_PODCAST = 10
        const val GO_TO_EPISODE = 11

        var Context.musicMenuItem: MutableSet<MenuItem>
            get() = mutableSetOf(
                MenuItem(R.drawable.ic_file_download_black_24dp, this.getString(R.string.download) , DOWNLOAD_SONG),
                MenuItem(R.drawable.ic_baseline_graphic_eq_24 , this.getString(R.string.song_radio) , SONG_RADIO),
                MenuItem(R.drawable.ic_baseline_person_pin_24 , this.getString(R.string.artist_radio) , ARTIST_RADIO),
                MenuItem(R.drawable.ic_baseline_album_24 , this.getString(R.string.go_to_album) , GO_TO_ALBUM),
                MenuItem(R.drawable.ic_person_black_24dp , this.getString(R.string.go_to_artist) , GO_TO_ARTIST),
                MenuItem(R.drawable.ic_baseline_info_24 , this.getString(R.string.song_info) , SONG_INFO),
                MenuItem(R.drawable.ic_baseline_add_24 , this.getString(R.string.add_song_to) , ADD_TO)
            )
            set(value) = TODO()

        var Context.podcastEpisodeMenuItem: MutableSet<MenuItem>
            get() = mutableSetOf(
                MenuItem(R.drawable.ic_file_download_black_24dp , this.getString(R.string.download) , DOWNLOAD_EPISODE),
                MenuItem(R.drawable.ic_gray_favorite_border_24, this.getString(R.string.add_to_favorite) , LIKE_EPISODE),
                MenuItem(R.drawable.ic_baseline_favorite_24, this.getString(R.string.remove_from_favorite) , UNLIKE_EPISODE),
                MenuItem(R.drawable.ic_baseline_poll_24, this.getString(R.string.go_to_podcast) , GO_TO_PODCAST),
                MenuItem(R.drawable.ic_baseline_headset_24, this.getString(R.string.go_to_episode) , GO_TO_EPISODE),

            )
            set(value) = TODO()

        var offlineSongMenuItem   = listOf(
            MenuItem(R.drawable.ic_round_remove_circle_outline_24 , "Remove" )
        )
    }
}