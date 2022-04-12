package com.komkum.komkum.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.komkum.komkum.data.model.AudioBookDbInfo
import com.komkum.komkum.data.model.EbookDbInfo

@Dao
interface EbookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun saveBook(bookInfo : EbookDbInfo)

    @Query("SELECT * FROM EbookDbInfo WHERE _id = :bookId")
     fun getBook(bookId : String) : EbookDbInfo?

    @Query("SELECT * FROM EbookDbInfo")
     fun getBooks() : List<EbookDbInfo>?

    @Query("UPDATE EbookDbInfo SET lastReadingPage = :pageNumber WHERE _id = :bookId")
     fun updateLastReadingPage(bookId : String , pageNumber : Int)

    @Query("DELETE FROM EbookDbInfo WHERE _id = :bookId")
     fun deleteBook(bookId: String)
}