package com.example.kotlincoroutineexception

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class GlobalCoroutineExceptionHandler: CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler
    
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Log.d("ning", "UnHandled Coroutine Exception: $exception")
    }
}