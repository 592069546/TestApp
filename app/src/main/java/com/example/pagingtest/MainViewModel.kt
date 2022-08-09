package com.example.pagingtest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pagingtest.MainViewModel.Companion.TAG
import com.example.pagingtest.room.Fucker
import com.example.pagingtest.room.FuckerRepository
import com.example.pagingtest.room.User
import com.example.pagingtest.room.UserRepository
import com.example.pagingtest.test.FlowRetry
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val userRepository: UserRepository,
    private val fuckerRepository: FuckerRepository
) : ViewModel() {
    val allUsers = userRepository.allUsers
        .asLiveData() /*TODO flow livedata*/

    val singleHalfUsers = userRepository.singleHalfUsers.asLiveData()

    val twiceHalfUsers = userRepository.twiceHalfUsers.asLiveData()

    val combineUsers =
        userRepository.allUsers.combine(userRepository.twiceHalfUsers) { allUser: List<User>, twiceHalf: List<User> ->
            val list = mutableListOf<User>()
            list.addAll(allUser)
            list.addAll(twiceHalf)
            return@combine list.toList()
        }.asLiveData()

    val mergeUsers = merge(
        userRepository.allUsers,
        userRepository.twiceHalfUsers,
        userRepository.singleHalfUsers
    ).asLiveData()

    private val conflateFlow = MutableStateFlow(0)

    val emitFlow = flow {
        emit(1)
        delay(500)
        emit(2)
        delay(500)
        emit(3)
        delay(500)
        emit(4)
        delay(500)
        emit(5)
    }
    var emitJob: Job? = null

    init {
        viewModelScope.launch(IO) {
            conflateFlow.debounce(1000).collectLatest {
                if (it == 0)
                    return@collectLatest
                Log.d(TAG, "$it")
            }
        }

        if (test_distinct)
            testFlowDistinct(userRepository)

        if (retry)
            retry()

        viewModelScope.launch(IO) {
            userRepository.clear()
            userRepository.insertUser(User.newUser())
        }

        viewModelScope.launch(IO) {
            fuckerRepository.insertFucker(Fucker())
            fuckerRepository.insertFucker(Fucker())
        }

        viewModelScope.launch {
            fuckerRepository.fuckers.collect {
                Log.d(TAG, "fuckers ${it.size}")
            }
        }
    }

    fun onPost() {
        viewModelScope.launch {
            conflateFlow.emit((System.currentTimeMillis() % 1000).toInt())
        }
    }

    fun insertUser(user: User) = viewModelScope.launch { userRepository.insertUser(user) }

    fun delete(users: List<User>) {
        viewModelScope.launch(IO) { userRepository.deleteUsers(users) }
    }

    companion object {
        val TAG: String = MainViewModel::class.java.simpleName

        private const val test_distinct = false
        private const val retry = false
    }
}

fun MainViewModel.retry() {
    viewModelScope.launch {
        flow {
            emit(0)
            delay(100)
            val sss = 1 / 0
            emit(sss)
            delay(100)
            emit(2)
        }
            .retryWhen { e: Throwable, tryTime: Long ->
                Log.d(TAG, "***** retry $tryTime $e")
                if (tryTime < 2)
                    true
                else {
                    emit(3)
                    false
                }
            }
            .catch {
                Log.d(TAG, "***** catch")
            }
            .collect {
                Log.d(TAG, "***** collect $it")
            }
    }
}

fun MainViewModel.testFlowDistinct(userRepository: UserRepository) {
    viewModelScope.launch(IO) {
        userRepository.singleHalfUsers
            .distinctUntilChanged()
            .collect {
                Log.d(TAG, "single ${it.size}")
            }
    }

    viewModelScope.launch(IO) {
        userRepository.twiceHalfUsers
            .distinctUntilChanged()
            .collect {
                Log.d(TAG, "twice ${it.size}")
            }
    }
}

fun MainViewModel.flowRetry() {
    FlowRetry.RetryTest2(emitJob, scope = viewModelScope, emitFlow).test()
}

class MainViewModelFactory(
    private val userRepository: UserRepository,
    private val fuckerRepository: FuckerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(userRepository, fuckerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}