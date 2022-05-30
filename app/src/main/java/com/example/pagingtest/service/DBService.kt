package com.example.pagingtest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.pagingtest.room.AppDatabase
import com.example.pagingtest.room.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/*TODO service coroutineScope*/
class DBService : Service() {

    private val userRepository by lazy {
        AppDatabase.getUserRepository(this)
    }

    private var job: Job? = null

    /**
     * 创建service，后续不再创建
     * */
    override fun onCreate() {
        super.onCreate()

        job = GlobalScope.launch {
            repeat(Int.MAX_VALUE) {
                val result = System.currentTimeMillis() and 1
                if (result == 1L) {
                    insert()
                } else {
                    delete()
                }
                delay(100L)
            }
        }
    }

    /**
     * 创建service后执行 / startService
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job?.start()
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 销毁service
     * */
    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    /**
     * @return 用于与Activity通信
     * */
    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun insert() {
        userRepository.insertUser(User.newUser())
        delay(100L)
    }

    private suspend fun delete() {
        val userList = userRepository.allUsers.first()
        if (userList.isNotEmpty()) {
            val user = userList[0]
            userRepository.deleteUser(user)
        }
        delay(200L)
    }

    companion object {
        val TAG = DBService::class.java.simpleName
    }
}