package com.example.pagingtest.service

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class BindServiceActivity : AppCompatActivity() {

    private val helper = BindServiceHelper(this).init(
        object : BindServiceHelper.InitializeCallback {
            override fun onSuccess() {
                Log.d(TAG, "service success")
            }

            override fun onFail() {
                Log.d(TAG, "service fail")
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        private val TAG = BindServiceActivity::class.java.simpleName
    }
}