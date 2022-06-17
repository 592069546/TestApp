package com.example.pagingtest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pagingtest.room.User
import com.example.pagingtest.room.UserRepository
import kotlinx.coroutines.Dispatchers.IO
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
    }

    fun onPost() {
        viewModelScope.launch {
            conflateFlow.emit((System.currentTimeMillis() % 1000).toInt())
        }
    }

    fun insertUser(user: User) = viewModelScope.launch { userRepository.insertUser(user) }

    companion object {
        val TAG = MainViewModel::class.java.simpleName
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