package com.komkum.komkum.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.komkum.komkum.data.model.AudioBookDbInfo

@Dao
interface AudiobookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveBook(audioBookInfo : AudioBookDbInfo)

    @Query("SELECT * FROM AudioBookDbInfo WHERE _id = :bookId")
    suspend fun getBook(bookId : String) : AudioBookDbInfo?

    @Query("SELECT * FROM AudioBookDbInfo")
    suspend fun getBooks() : List<AudioBookDbInfo>?

    @Query("UPDATE audiobookdbinfo SET lastListeningChapterIndex = :chapterIndex WHERE _id = :bookId")
    suspend fun updateLastListeningChapter(bookId : String , chapterIndex : Int)

    @Query("DELETE FROM AudioBookDbInfo WHERE _id = :bookId")
    suspend fun deleteBook(bookId: String)
}