package com.example.pagingtest.room

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/*TODO Singleton*/
class UserRepository(private val userDao: UserDao) {
    // 单例用于统一一个flow
    val allUsers = userDao.getAll()

    val singleHalfUsers = userDao.getSingleHalf()

    val twiceHalfUsers = userDao.getTwiceHalf()

    suspend fun insertUser(user: User) {
        withContext(IO) {
            userDao.insert(user)
        }
    }

    suspend fun insertUsers() {
        withContext(IO) {
            val list = User.newUserList(65)
            userDao.insertUsers(list)
        }
    }

    suspend fun deleteUser(user: User) {
        withContext(IO) {
            userDao.delete(user)
        }
    }

    suspend fun deleteUsers(users: List<User>) {
        withContext(IO) {
            userDao.delete(users)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(IO) {
            userDao.update(user)
        }
    }

    suspend fun clear() {
        withContext(IO) {
            userDao.clear()
        }
    }
}