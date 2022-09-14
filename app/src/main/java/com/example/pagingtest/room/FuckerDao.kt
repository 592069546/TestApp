package com.example.pagingtest.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.example.pagingtest.room.DBConst.FuckerId
import com.example.pagingtest.room.DBConst.FuckerTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface FuckerDao {
    @Query("SELECT * FROM $FuckerTableName")
    fun getAllFucker(): Flow<List<Fucker>>

    @Insert(onConflict = REPLACE)
    suspend fun insertFucker(fucker: Fucker): Long

    @Query("SELECT * FROM $FuckerTableName WHERE id = :id")
    suspend fun queryFucker(id: Int): Fucker?
}