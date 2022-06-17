package com.example.pagingtest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: Int = 0,
    @ColumnInfo(name = "first_name") var firstName: String?,
    @ColumnInfo(name = "last_name") var lastName: String?
) {
    companion object {
        fun newUser() = User(
            (System.currentTimeMillis() % 100).toInt(),
            (System.currentTimeMillis() % 120).toString(),
            (System.currentTimeMillis() % 220).toString()
        )
    }

    fun update() {
        firstName = (System.currentTimeMillis() % 120).toString()
        lastName = (System.currentTimeMillis() % 220).toString()
    }
}