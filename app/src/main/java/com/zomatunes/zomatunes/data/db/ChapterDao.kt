package com.zomatunes.zomatunes.data.db

import androidx.room.*
import com.zomatunes.zomatunes.data.model.ChapterDbInfo

@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveChapters(chapterInfo : List<ChapterDbInfo>)

    @Query("SELECT * FROM ChapterDbInfo WHERE bookId = :bookId")
    suspend fun getBookChapters(bookId : String) : List<ChapterDbInfo>

    @Query("UPDATE ChapterDbInfo SET currentPosition = :position , duration = :duration WHERE _id = :chapterId")
    suspend fun updateChapterCurrentPosition(chapterId : String , position : Long , duration: Int )

    @Query("DELETE  FROM ChapterDbInfo WHERE bookId = :bookId")
    suspend fun deleteBookChapters(bookId: String)
}