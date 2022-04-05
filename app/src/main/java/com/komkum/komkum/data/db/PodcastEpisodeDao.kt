package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.EpisodeDBInfo

@Dao
interface PodcastEpisodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveEpisode(episode : EpisodeDBInfo)

    @Query("SELECT * FROM EpisodeDBInfo")
    suspend fun getEpisodes() : List<EpisodeDBInfo>

    @Query("SELECT * FROM EpisodeDBInfo WHERE _id = :episodeId")
    suspend fun getEpisode(episodeId : String) : EpisodeDBInfo?

    @Delete
    suspend fun deleteEpisode(episode: EpisodeDBInfo)

    @Query("DELETE  FROM EpisodeDBInfo")
    suspend fun deleteAllEpisodes()
}