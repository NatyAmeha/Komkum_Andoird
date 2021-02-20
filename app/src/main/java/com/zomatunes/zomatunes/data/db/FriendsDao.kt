package com.zomatunes.zomatunes.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zomatunes.zomatunes.data.model.Friend

@Dao
interface FriendsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFriends(friendsList : List<Friend>) : List<Long>?

    @Query("SELECT * FROM Friend WHERE userId = :userId")
    suspend fun getFriends(userId : String) : List<Friend>?
}