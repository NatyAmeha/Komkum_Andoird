package com.zomatunes.zomatunes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zomatunes.zomatunes.data.model.AudioBookDbInfo
import com.zomatunes.zomatunes.data.model.EbookDbInfo

@Dao
interface EbookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveBook(bookInfo : EbookDbInfo)

    @Query("SELECT * FROM EbookDbInfo WHERE _id = :bookId")
    suspend fun getBook(bookId : String) : EbookDbInfo?

    @Query("SELECT * FROM EbookDbInfo")
    suspend fun getBooks() : List<EbookDbInfo>?

    @Query("UPDATE EbookDbInfo SET lastReadingPage = :pageNumber WHERE _id = :bookId")
    suspend fun updateLastReadingPage(bookId : String , pageNumber : Int)

    @Query("DELETE FROM EbookDbInfo WHERE _id = :bookId")
    suspend fun deleteBook(bookId: String)
}