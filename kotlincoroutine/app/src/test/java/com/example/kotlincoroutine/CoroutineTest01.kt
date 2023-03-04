package com.example.kotlincoroutine

import kotlinx.coroutines.*
import  org.junit.Test
import kotlin.system.measureTimeMillis

class CoroutineTest01 {

    @Test
    fun testCoroutineBuilder() = runBlocking {//包装了一个主协程
        val job = launch {
            delay(200)
            println("job1 finished")

        }

        val job2 = async {
            delay(200)
            println("job2 finished.")
            "job2 result"
        }

        println(job2.await())
    }


    @Test
    fun testCoroutineJoin() = runBlocking {//包装了一个主协程
        val job = launch {
            delay(200)
            println("One")
        }
        job.join()//等待作业

        val job2 = launch {
            delay(200)
            println("Two")

        }

        val job3 = launch {
            delay(200)
            println("Three")

        }
    }

    @Test
    fun testCoroutineAwait() = runBlocking {//包装了一个主协程
        val job = async {
            delay(200)
            println("One")
        }
        job.await()//等待作业。

        val job2 = async {
            delay(200)
            println("Two")

        }

        val job3 = async {
            delay(200)
            println("Three")

        }
    }


    @Test
    fun testSync() = runBlocking {
       val time = measureTimeMillis {
            val one = doOne()
            val two = doTwo()
           println("The result:${one + two}")
        }

        println("Completed in $time ms")
    }

    @Test
    fun testCombineSync() = runBlocking { // runBlocking 主线程 Main
        val time = measureTimeMillis {
            val one = async { doOne() }
            val two = async { doTwo() }
            println("The result:${one.await() + two.await()}")
        }

        println("Completed in $time ms")
    }

    private suspend fun doOne():Int{
        delay(1000)
        return 14
    }

    private suspend fun doTwo():Int{
        delay(1000)
        return 15
    }

    @Test
    fun testStartMode() = runBlocking { // runBlocking 主线程 Main
        val job = launch(start = CoroutineStart.DEFAULT) {
            delay(1000)
            println("Job finished")
        }
        delay(1000)

        //CoroutineStart.UNDISPATCHED 启动模式. UNDISPATCHED 立即执行，不是立即调度。
        val job1 = async(context = Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            println("thread：" + Thread.currentThread().name)
        }


        val job2 = async(context = Dispatchers.IO, start = CoroutineStart.DEFAULT) {
            println("thread：" + Thread.currentThread().name)
        }

        val job3 = async(context = Dispatchers.IO, start = CoroutineStart.LAZY) {
            println("thread：" + Thread.currentThread().name)
        }

        job3.await()
    }

    @Test
    fun testCoroutineScopeBuilder() = runBlocking {
        coroutineScope { //要等待它里面的子协程都执行完成， 属于挂起函数。
            //原子性的。
            val job1 = launch {
                delay(200)
                println("job1 finished")

            }

            val job2 = async {
                delay(200)
                println("job2 finished.")
                "job2 result"
                throw IllegalArgumentException()
            }
        }
    }

    @Test
    fun testSupervisorScopeBuilder() = runBlocking {
        supervisorScope { //要等待它里面的子协程都执行完成， 属于挂起函数。
            //原子性的。
            val job1 = launch {
                delay(200)
                println("job1 finished")
            }

            val job2 = async {
                delay(200)
                println("job2 finished.")
                "job2 result"
                throw IllegalArgumentException()
            }
        }
    }



}