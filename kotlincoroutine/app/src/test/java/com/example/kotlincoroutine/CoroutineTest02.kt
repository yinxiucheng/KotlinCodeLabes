package com.example.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test

class CoroutineTest02 {

    @Test
    fun testScopeCancel() = runBlocking {
        //这样创建的不是继承 runBlocking的上下文。
        val scope = CoroutineScope(Dispatchers.Default)
        val job1 = scope.launch {
            delay(1000)
            println("job 1")
        }

        val job2 = scope.launch {
            delay(1000)
            println("job 2")
        }
        delay(100)
        scope.cancel()
        delay(2000)
    }


    @Test
    fun testBrotherCancel() = runBlocking {
        //这样创建的不是继承 runBlocking的上下文。
        val scope = CoroutineScope(Dispatchers.Default)
        val job1 = scope.launch {
            delay(1000)
            println("job 1")
        }

        val job2 = scope.launch {
            delay(1000)
            println("job 2")
        }
        delay(100)
        job1.cancel()
        delay(2000)
    }


    @Test
    fun testCancellationException() = runBlocking {
        //这样创建的不是继承 runBlocking的上下文。
        val job1 = GlobalScope.launch {
            try {
                delay(1000)
                println("job 1")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        delay(100)
        job1.cancel(CancellationException("取消"))// 会抛出一个异常
        job1.join()
    }


    @Test
    fun testCancelCpuTaskByIsActive() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while (i < 5 && isActive){
                if (System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++}...")
                    nextPrintTime += 500
                }
            }
        }

        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }

    @Test
    fun testCancelCpuTaskByEnsureActive() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while (i < 5){
                ensureActive()
                if (System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++}...")
                    nextPrintTime += 500
                }
            }
        }
        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }

    @Test
    fun testCancelCpuTaskByYield() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while (i < 5){
                yield()//出让执行权给其它协程。
                if (System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++}...")
                    nextPrintTime += 500
                }
            }
        }
        delay(1300)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }


}