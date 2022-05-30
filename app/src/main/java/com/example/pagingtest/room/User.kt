package com.example.pagingtest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
) {
    companion object {
        fun newUser() = User(
            (System.currentTimeMillis() % 100).toInt(),
            (System.currentTimeMillis() % 120).toString(),
            (System.currentTimeMillis() % 220).toString()
        )
    }
}