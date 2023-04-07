package com.example.pagingtest.service

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import com.example.paging.base.logD

fun Context.bindService(
    serviceName: String,
    action: String = ACTION_AIDL,
    connection: ServiceConnection
) {
    val tag: String = javaClass.simpleName
    try {
        val intent = Intent(action)
        packageManager.resolveServiceCompat(intent)?.serviceInfo?.also { info ->
            "info: $info ${info.packageName}".logD(tag)
            intent.component = ComponentName(info.packageName, info.name)
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun PackageManager.resolveServiceCompat(intent: Intent): ResolveInfo? {
    val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        resolveService(intent, PackageManager.ResolveInfoFlags.of(flag.toLong()))
    else
        resolveService(intent, flag)
}

const val ACTION_AIDL = "android.intent.action.AIDLService"
const val SERVICE_AIDL = "com.example.multimodule.AIDLService"