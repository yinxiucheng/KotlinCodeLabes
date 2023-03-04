package com.example.kotlincoroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val continuation = suspend {
            5
        }.createCoroutine(object :Continuation<Int>{
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            override fun resumeWith(result: Result<Int>) {
                TODO("Not yet implemented")
            }
        })
        continuation.resume(Unit)



        // 基础设施层
        // 业务框架层

    }
}