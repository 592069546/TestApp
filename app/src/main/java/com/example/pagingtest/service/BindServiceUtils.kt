package com.example.pagingtest.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.multimodule.logD

fun Context.bindService(serviceName: String) {
    val tag = javaClass.simpleName
    try {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(tag, "onServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(tag, "onServiceDisconnected")
            }
        }
        val clazz = Class.forName(serviceName)
        Intent(this, clazz).also {
            it.action = "android.intent.action.AIDLService"
            packageManager.resolveService(
                it,
                PackageManager.MATCH_DEFAULT_ONLY
            )?.serviceInfo?.also { info ->
                "info: $info ${info.packageName}".logD(tag)
                it.component = ComponentName(info.packageName, info.name)
                bindService(it, connection, AppCompatActivity.BIND_AUTO_CREATE)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}