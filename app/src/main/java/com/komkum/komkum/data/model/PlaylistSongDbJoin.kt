package com.komkum.komkum.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    primaryKeys = ["playlistId" , "songId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistDbInfo::class,
            parentColumns = ["_id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SongDbInfo::class,
            parentColumns = ["_id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ], indices = [
        Index("playlistId", "songId")
    ]
)
data class PlaylistSongDbJoin(
    var playlistId: String,
    var songId: String
)