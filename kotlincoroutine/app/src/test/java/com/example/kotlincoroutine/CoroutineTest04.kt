package com.example.kotlincoroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test

class CoroutineTest04 {
    @Test
    fun `test deal with timeout`() = runBlocking {
        withTimeout(1300){
            repeat(1000){ i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        }
    }

    @Test
    fun `test deal with timeout return null`() = runBlocking {
        val result = withTimeoutOrNull(1300){
            repeat(1000){ i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
            "Done"
        }?: "Jeak"

        println("I'm $result ..")
    }
}