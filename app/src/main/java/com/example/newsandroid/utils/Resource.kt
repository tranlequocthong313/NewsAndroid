package com.example.newsandroid.utils

sealed class Resource<T>(
    val body: T? = null,
    val message: String? = null
) {
    class Success<T>(body: T) : Resource<T>(body)
    class Error<T>(message: String, body: T? = null) : Resource<T>(body, message)
    class Loading<T> : Resource<T>()
}