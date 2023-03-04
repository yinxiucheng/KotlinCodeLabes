package com.example.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test
import java.io.BufferedReader
import java.io.FileReader

class CoroutineTest03 {

    @Test
    fun testReleaseResources() = runBlocking {
        val job = launch {
            try {
                repeat(1000){ i ->
                    println("job: I'm sleeping $i ...")
                }
            } finally {
                //这里释放资源
                println("job: I'm running finally.")
            }
        }
        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }


    @Test
    fun testUseFunction() = runBlocking {
        val br = BufferedReader(FileReader(""))
        with(br){
            var line:String?
            try {
                while (true){
                    line = readLine()?:break;
                    println(line)
                }
            } finally {
                close()
            }
        }

        BufferedReader(FileReader("")).use { //标准函数包含了 finally 分支下 close()
            var line:String?
            while (true){
                line = readLine()?:break;
                println(line)
            }
        }

    }


    @Test
    fun `test cancel with NonCancellable`() = runBlocking {
        val job = launch {
            try {
                repeat(1000){ i ->
                    println("job: I'm sleeping $i ...")
                }
            } finally {
                withContext(NonCancellable){
                    println("job: I'm running finally.")
                    delay(1000L)
                    println("job: And I've just delayed for 1 sec because I'm non-cancellable")
                }

            }
        }
        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")

    }
}