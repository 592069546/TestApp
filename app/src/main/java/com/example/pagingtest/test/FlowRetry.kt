package com.example.pagingtest.test

import android.util.Log
import com.example.pagingtest.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FlowRetry {
    class RetryTest1(
        var emitJob: Job?,
        private val scope: CoroutineScope,
        private val emitFlow: Flow<Int>
    ) {
        fun test(retry: Int = 0) {
            emitJob?.let {
                if (it.isActive)
                    it.cancel()
            }
            if (retry >= 3)
                return
            emitJob = scope.launch {
                emitFlow
                    .collect {
                        if (it == 3) {
                            delay(1000)
                            test(retry + 1)
                            return@collect
                        }
                        Log.d(MainViewModel.TAG, "****** flow emit $emitFlow $it")
                    }
            }
        }
    }

    class RetryTest2(
        var emitJob: Job?,
        private val scope: CoroutineScope,
        private val emitFlow: Flow<Int>
    ) {
        private val flow = MutableStateFlow(-1)

        fun test(retry: Int = 0) {
            emitJob?.let {
                if (it.isActive)
                    it.cancel()
            }
            if (retry >= 3)
                return
            scope.launch {
                flow.emit(3)
            }
            emitJob = scope.launch {
                flow
                    .onEach {
                        if (retry > 0) {
                            delay(1000)
                            Log.d(MainViewModel.TAG, "****** flow retry $retry")
                        }
                    }
                    .collect {
                        if (it == 3) {
                            test(retry + 1)
                            return@collect
                        }
                        Log.d(MainViewModel.TAG, "****** flow emit $flow $it")
                    }
            }
        }
    }
}