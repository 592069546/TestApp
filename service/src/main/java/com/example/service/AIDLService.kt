package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AIDLService : Service() {

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "service onBind")
        return AIDLBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    private class AIDLBinder : MultiBinder.Stub() {
        override fun getFromBinder() = 3223
    }

    companion object {
        private val TAG = AIDLService::class.java.simpleName
    }
}