package com.example.pagingtest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.pagingtest.room.AppDatabase
import com.example.pagingtest.room.User
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/*TODO service coroutineScope*/
class DBService : Service() {

    private val userRepository by lazy {
        AppDatabase.getUserRepository(this)
    }

//    private var job: Job? = null

    /**
     * 创建service，后续不再创建
     * */
    override fun onCreate() {
        super.onCreate()

//        job = GlobalScope.launch {
////            repeat(Int.MAX_VALUE) {
////                when (System.currentTimeMillis() and 0b11) {
////                    0b00L -> {
////                        insert()
////                    }
////                    0b01L -> {
////                        delete()
////                    }
////                    else -> {
////                        modify()
////                    }
////                }
////                delay(100L)
////            }
//            when (System.currentTimeMillis() and 0b11) {
//                0b00L -> {
//                    insert()
//                }
//                0b01L -> {
//                    delete()
//                }
//                else -> {
//                    modify()
//                }
//            }
//            delay(100L)
//        }
    }

    /**
     * 创建service后执行 / startService
     * */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "${job?.isActive} ${job?.isCancelled} ${job?.isCompleted}")
        MainScope().launch(IO) {
//            repeat(Int.MAX_VALUE) {
//                when (System.currentTimeMillis() and 0b11) {
//                    0b00L -> {
//                        insert()
//                    }
//                    0b01L -> {
//                        delete()
//                    }
//                    else -> {
//                        modify()
//                    }
//                }
//                delay(100L)
//            }
//            insert()
            modify()
            delay(100L)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 销毁service
     * */
    override fun onDestroy() {
        super.onDestroy()
//        job?.cancel()
    }

    /**
     * @return 用于与Activity通信
     * */
    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun insert() {
        Log.d(TAG, "insert")
//        userRepository.insertUser(User.newUser())
        userRepository.insertUsers()
        delay(100L)
    }

    private suspend fun delete() {
        val userList = userRepository.allUsers.first()
        if (userList.isNotEmpty()) {
            val pos = userList.indices.random()
            Log.d(TAG, "delete $pos")
            val user = userList[pos]
            userRepository.deleteUser(user)
        }
        delay(200L)
    }

    private suspend fun modify() {
        val userList = userRepository.allUsers.first()
        if (userList.isNotEmpty()) {
            val pos = userList.indices.random()
            val user = userList[pos]
            user.update()
            Log.d(TAG, "modify $pos ${userRepository.updateUser(user)}")
        } else {
            Log.d(TAG, "modify create new")
            userRepository.updateUser(User.newUser())
        }
        delay(50L)
    }

    companion object {
        val TAG = DBService::class.java.simpleName
    }
}