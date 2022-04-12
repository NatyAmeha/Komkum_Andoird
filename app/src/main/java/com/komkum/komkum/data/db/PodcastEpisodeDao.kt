package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.EpisodeDBInfo

@Dao
interface PodcastEpisodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun saveEpisode(episode : EpisodeDBInfo)

    @Query("SELECT * FROM EpisodeDBInfo")
     fun getEpisodes() : List<EpisodeDBInfo>

    @Query("SELECT * FROM EpisodeDBInfo WHERE _id = :episodeId")
     fun getEpisode(episodeId : String) : EpisodeDBInfo?

    @Delete
     fun deleteEpisode(episode: EpisodeDBInfo)

    @Query("DELETE  FROM EpisodeDBInfo")
     fun deleteAllEpisodes()
}