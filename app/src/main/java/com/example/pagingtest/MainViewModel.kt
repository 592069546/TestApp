package com.example.pagingtest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pagingtest.room.User
import com.example.pagingtest.room.UserRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {
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

    init {
        viewModelScope.launch(IO) {
            conflateFlow.debounce(1000).collectLatest {
                if (it == 0)
                    return@collectLatest
                Log.d(TAG, "$it")
            }
        }

        viewModelScope.launch(IO) {
            userRepository.singleHalfUsers
                .distinctUntilChanged()
                .collect {
                    Log.d(TAG, "single ${it.size}")
                }
        }

        if (retry) {
            retry()
        }

        viewModelScope.launch(IO) {
            userRepository.twiceHalfUsers
                .distinctUntilChanged()
                .collect {
                    Log.d(TAG, "twice ${it.size}")
                }
        }

        viewModelScope.launch(IO) {
            userRepository.clear()
            userRepository.insertUser(User.newUser())
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
        val TAG = MainViewModel::class.java.simpleName

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
                Log.d(MainViewModel.TAG, "***** retry $tryTime $e")
                if (tryTime < 2)
                    true
                else {
                    emit(3)
                    false
                }
            }
            .catch {
                Log.d(MainViewModel.TAG, "***** catch")
            }
            .collect {
                Log.d(MainViewModel.TAG, "***** collect $it")
            }
    }
}

class MainViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}