package com.example.pagingtest.test

import com.example.paging.base.logD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

object FlowTransformWhile {
    private val TAG = FlowTransformWhile::class.java.simpleName

    fun testTransFormWhile(scope: CoroutineScope) {
        scope.launch {
            (0 until 20).asFlow().transformWhile {
                "do in transformWhile $it".logD("FlowTransformWhile")
                (it < 10).also { collected ->
                    if (collected)
                        emit(it)
                }
            }.collect {
                "collect in transformWhile $it".logD(TAG)
            }
        }
    }

    fun testCallbackFlow(scope: CoroutineScope) {
        scope.launch {
            callbackFlow {
                trySend(0)
                delay(500)
                trySend(1)
                delay(500)
                trySend(2)

                awaitClose()
            }.collect {
                "collect $it in callbackFlow".logD(TAG)
            }
        }
    }
}