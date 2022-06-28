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
        fun newUser(
            uid: Int = ((System.currentTimeMillis() + IntRange(0, 99).random()) % 100).toInt()
        ) = User(
            uid,
            firstName = (System.currentTimeMillis() % 120).toString(),
            lastName = (System.currentTimeMillis() % 220).toString()
        )

        fun newUserList(
            id: Int = ((System.currentTimeMillis() + IntRange(0, 99).random()) % 100).toInt()
        ): List<User> = listOf(
            newUser(id + 1),
            newUser(id + 2),
            newUser(id + 3),
            newUser(id + 4),
            newUser(id + 5),
            newUser(id + 6)
        )
    }

    fun update() {
        firstName = (System.currentTimeMillis() % 120).toString()
        lastName = (System.currentTimeMillis() % 220).toString()
    }
}