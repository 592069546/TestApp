package com.example.pagingtest.room

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class FuckerRepository(private val dao: FuckerDao) {
    val fuckers = dao.getAllFucker()

    suspend fun insertFucker(fucker: Fucker) {
        withContext(IO) {
            dao.insertFucker(fucker)
        }
    }
}