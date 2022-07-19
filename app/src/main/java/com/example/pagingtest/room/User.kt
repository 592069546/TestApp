package com.example.pagingtest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val uid: Int = 0,
    @ColumnInfo(name = "first_name") var firstName: String?,
    @ColumnInfo(name = "last_name") var lastName: String?,
    @ColumnInfo(name = "age", defaultValue = "0") var age: Int = (System.currentTimeMillis() % 232).toInt()
) {
    companion object {
        fun newUser(
            uid: Int = ((System.currentTimeMillis() + IntRange(0, 99).random()) % 100).toInt()
        ) = User(
            uid,
            firstName = (System.currentTimeMillis() % 120).toString(),
            lastName = (System.currentTimeMillis() % 220).toString()
        )

        fun newUserList(
            id: Int = ((System.currentTimeMillis() + IntRange(0, 99).random()) % 100).toInt(),
            size: Int = 8
        ): List<User> = MutableList(size) {
            newUser(id + it)
        }.toList()
    }

    fun update() {
        firstName = (System.currentTimeMillis() % 120).toString()
        lastName = (System.currentTimeMillis() % 220).toString()
        age = (System.currentTimeMillis() % 232).toInt()
    }
}