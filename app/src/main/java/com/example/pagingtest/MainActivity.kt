package com.example.pagingtest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pagingtest.room.AppDatabase
import com.example.pagingtest.room.User
import com.example.pagingtest.service.DBService
import com.example.pagingtest.ui.RoomAdapter

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getUserRepository(this))
    }

    private val rv_db: RecyclerView by lazy { findViewById(R.id.rv_db) }
    private val roomAdapter by lazy { RoomAdapter() }

    private val tv_insert: TextView by lazy { findViewById(R.id.tv_insert) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObserver()

        rv_db.layoutManager = LinearLayoutManager(this)
        rv_db.adapter = roomAdapter

        tv_insert.setOnClickListener {
            mainViewModel.insertUser(User.newUser())
        }

        startService(Intent(this, DBService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, DBService::class.java))
    }

    private fun initObserver() {
        mainViewModel.allUsers.observe(this) {
            it.let {
                roomAdapter.submitList(it)
            }
        }
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }
}