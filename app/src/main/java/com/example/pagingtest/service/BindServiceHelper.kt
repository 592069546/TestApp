package com.example.pagingtest.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.service.MultiBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume

class BindServiceHelper(private val context: Context) {
    private val owner: LifecycleOwner =
        if (context is LifecycleOwner) context
        else throw IllegalArgumentException("context must be life cycle owner")
    private val scope = owner.lifecycleScope

    private val callback: ServiceConnectCallback = object : ServiceConnectCallback {
        override fun onConnected(binder: IBinder) {
            Log.d(TAG, "callback onConnected")
            val stub = MultiBinder.Stub.asInterface(binder)
            Log.d(TAG, "get ${stub.fromBinder}")
        }

        override fun onDisConnected() {
            Log.d(TAG, "callback onDisConnected")
        }

        override fun onFail() {
        }
    }

    fun init(initializeCallback: InitializeCallback? = null) {
        bindService(
            context,
            SERVICE_AIDL,
            ACTION_AIDL,
            initializeCallback,
            callback
        )
    }

    private fun bindService(
        context: Context,
        serviceName: String,
        action: String,
        initializeCallback: InitializeCallback?,
        callback: ServiceConnectCallback
    ) {
        scope.launchWhenCreated {
            val result = bindServiceImpl(context, serviceName, action, 3)
            println("****** $result")
        }
    }

    private suspend fun bindServiceImpl(
        context: Context,
        serviceName: String,
        action: String,
        retryTime: Int
    ): Result {
        println("****** $retryTime")
        if (retryTime == 0)
            return Result(-1)
        return try {
            withTimeout(5000) {
                return@withTimeout suspendCancellableCoroutine {
                    val binding = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                            it.resume(Result(0, name, service))
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            it.resume(Result(1, name))
                        }
                    }
                    context.bindService(serviceName, action, binding)
                }
            }
        } catch (e: TimeoutCancellationException) {
            bindServiceImpl(context, serviceName, action, retryTime - 1)
        }
    }

    interface InitializeCallback {
        fun onSuccess()

        fun onFail()
    }

    private interface ServiceConnectCallback {
        fun onConnected(binder: IBinder)

        fun onDisConnected()

        fun onFail()
    }

    private class AlarmServiceConnection(
        context: Context,
        private val owner: LifecycleOwner,
        private val send: (ConnectResult) -> Unit
    ) : ServiceConnection {
        private val alarmFlow = flow {
            delay(3000)
            emit(false)
        }
        private val alarmJob: Job = owner.lifecycleScope.launch {
            alarmFlow.collect {
                Log.e(TAG, "bind service out time")
                send(ConnectResult(false))
            }
        }

        private val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                try {
                    Log.d(TAG, "unbind service")
                    context.unbindService(this@AlarmServiceConnection)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "bind service onServiceConnected $name")
            alarmJob.cancel()
            send(ConnectResult(true, service))
            owner.lifecycle.addObserver(observer)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "bind service onServiceDisconnected $name")
            send(ConnectResult(true))
        }
    }

    private data class ConnectResult(
        val isConnected: Boolean,
        val service: IBinder? = null
    )

    private data class Result(
        val status: Int, val name: ComponentName? = null, val service: IBinder? = null
    )

    companion object {
        private val TAG = BindServiceHelper::class.java.simpleName
    }
}