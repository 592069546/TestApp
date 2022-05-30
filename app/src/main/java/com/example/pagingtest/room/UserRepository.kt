package com.example.pagingtest.room

import androidx.annotation.WorkerThread

/*TODO Singleton*/
class UserRepository(private val userDao: UserDao) {
    // 单例用于统一一个flow
    val allUsers = userDao.getAll()

    @WorkerThread
    suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    @WorkerThread
    suspend fun deleteUser(user: User) {
        userDao.delete(user)
    }
}