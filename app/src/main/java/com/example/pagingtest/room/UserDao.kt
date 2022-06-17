package com.example.pagingtest.room

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE uid % 2 == 1")
    fun getSingleHalf(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE uid % 2 == 0")
    fun getTwiceHalf(): Flow<List<User>>

    @Insert
    suspend fun insertAll(vararg users: User)

    @Insert(onConflict = REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    @Update
    suspend fun update(user: User): Int
}