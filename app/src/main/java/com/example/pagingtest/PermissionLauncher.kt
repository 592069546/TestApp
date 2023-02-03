package com.example.pagingtest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner


class PermissionLauncher(caller: ActivityResultCaller, context: Context, callback: PermissionCallback, vararg permissions: String) {
    private val launcher: ActivityResultLauncher<Array<String>>

    init {
        launcher = caller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allGranted = true
            for (entry in result.entries) {
                if (!entry.value) {
                    allGranted = false
                    break
                }
            }
            if (allGranted) {
                callback.allGranted()
            } else {
                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.permission_request_content))
                    .setTitle(context.getString(R.string.permission_request_title))
                    .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    .show()
            }
        }
        if (caller is LifecycleOwner) {
            (caller as LifecycleOwner).lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) {
                    val deniedPermissions = grantedAllPermission(context, *permissions)
                    if (deniedPermissions != null)
                        launcher.launch(deniedPermissions)
                    else
                        callback.allGranted()
                }
            })
        }
    }

    fun checkSelfPermission(context: Context, vararg permissions: String): Boolean {
        return grantedAllPermission(context, *permissions) == null
    }

    private fun grantedAllPermission(context: Context, vararg permissions: String): Array<String>? {
        val deniedPermission: MutableList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                deniedPermission.add(permission)
        }
        return if (deniedPermission.isEmpty())
            null
        else
            deniedPermission.toTypedArray()
    }

    interface PermissionCallback {
        fun allGranted()
    }
}