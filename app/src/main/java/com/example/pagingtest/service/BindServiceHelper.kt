package com.example.pagingtest.service

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import com.example.multimodule.MultiBinder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeoutException

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
            owner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                callbackFlow {
                    val connectionTimer: Job = this.launch {
                        flow {
                            delay(5000)
                            emit(false)
                        }.collect {
                            Log.e(TAG, "bind service out time")
                            trySend(Result(-1))
                        }
                    }
                    val binding = object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                            this@callbackFlow.launch {
//                                delay(5000)
                                connectionTimer.cancel()
                                trySend(Result(0, name, service))
                            }
                        }

                        override fun onServiceDisconnected(name: ComponentName?) {
                            trySend(Result(1, name))
                        }
                    }
                    context.bindService(serviceName, action, binding)
                    awaitClose {
                        Log.d(TAG, "call back flow is close")
                    }
                }.onEach {
                    if (it.status == -1)
                        throw TimeoutException()
                }.retryWhen { e, time ->
                    Log.e(TAG, "retry time: $time")
                    e is TimeoutException && time < 2
                }.catch { e: Throwable ->

                }.collect {
                    Log.d(TAG, "collect $it")
                }
            }
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