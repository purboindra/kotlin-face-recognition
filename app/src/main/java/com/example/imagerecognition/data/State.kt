package com.example.imagerecognition.data

sealed class State<out T> {
    data object Idle : State<Nothing>()
    data class Success<T>(val data: T) : State<T>()
    data class Error(val throwable: Throwable) : State<Nothing>()
    data object Loading : State<Nothing>()
}