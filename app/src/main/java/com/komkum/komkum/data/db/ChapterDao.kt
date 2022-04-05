package com.komkum.komkum.data.db

import androidx.room.*
import com.komkum.komkum.data.model.ChapterDbInfo

@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveChapters(chapterInfo : List<ChapterDbInfo>)

    @Query("SELECT * FROM ChapterDbInfo WHERE bookId = :bookId")
    suspend fun getBookChapters(bookId : String) : List<ChapterDbInfo>

    @Query("UPDATE ChapterDbInfo SET currentPosition = :position , duration = :duration WHERE _id = :chapterId")
    suspend fun updateChapterCurrentPositionAndDuration(chapterId : String, position : Long, duration: Int )

    @Query("UPDATE ChapterDbInfo SET currentPosition = :position WHERE _id = :chapterId")
    suspend fun updateChapterCurrentPosition(chapterId : String, position : Long )

    @Query("DELETE  FROM ChapterDbInfo WHERE bookId = :bookId")
    suspend fun deleteBookChapters(bookId: String)

    @Query("UPDATE ChapterDbInfo SET completed = :status WHERE _id = :chapterId" )
    suspend fun updateChapterCompletionState(chapterId: String , status : Boolean)
}