package com.example.pagingtest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pagingtest.room.AppDatabase
import com.example.pagingtest.service.DBService
import com.example.pagingtest.ui.ItemTouchHelperCallback
import com.example.pagingtest.ui.RoomAdapter
import com.example.pagingtest.view.ItemExpandAnimator

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppDatabase.getUserRepository(this))
    }

    private val rv_db: RecyclerView by lazy { findViewById(R.id.rv_db) }
    private val roomAdapter by lazy { RoomAdapter() }

    private val tv_insert: TextView by lazy { findViewById(R.id.tv_insert) }
    private val tv_clear: TextView by lazy { findViewById(R.id.tv_clear) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObserver()

        rv_db.layoutManager = LinearLayoutManager(this)
        rv_db.adapter = roomAdapter

//        ItemSwipeTouchHelper(ItemTouchHelperCallback(), rv_db)

//        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback())
//        itemTouchHelper.attachToRecyclerView(rv_db)

        rv_db.itemAnimator = ItemExpandAnimator()

        tv_insert.setOnClickListener {
            startService(Intent(this, DBService::class.java))
//            mainViewModel.onPost()
        }

        tv_clear.setOnClickListener {
            mainViewModel.delete(roomAdapter.getList().subList(0, 6))
        }

        startService(Intent(this, DBService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, DBService::class.java))
    }

    private fun initObserver() {
        mainViewModel.allUsers.observe(this) {
            Log.d(TAG, "all ****** ${it.size}")
            it.let {
                roomAdapter.submitList(it)
            }
        }

        mainViewModel.singleHalfUsers.observe(this) {
            Log.d(TAG, "single half ****** " + it.size)
        }

        mainViewModel.twiceHalfUsers.observe(this) {
            Log.d(TAG, "twice half ****** ${it.size}")
        }

        mainViewModel.combineUsers.observe(this) {
            Log.d(TAG, "user ****** ${it.size}")
        }
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }
}