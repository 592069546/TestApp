package com.example.pagingtest.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Insert
    suspend fun insertAll(vararg users: User)

    @Insert(onConflict = REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)
}