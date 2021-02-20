package com.zomatunes.zomatunes.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = ["albumId" , "songId"],
    foreignKeys = [
        ForeignKey(
            entity = AlbumDbInfo::class,
            parentColumns = ["_id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SongDbInfo::class,
            parentColumns = ["_id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ], indices = [
        Index("albumId", "songId")
    ]
)
data class AlbumSongDbJoin(
    var albumId: String,
    var songId: String
)