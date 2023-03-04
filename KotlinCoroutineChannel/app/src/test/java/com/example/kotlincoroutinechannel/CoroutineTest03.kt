package com.example.kotlincoroutinechannel

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class CoroutineTest03 {

    @Test
    fun `test not safe concurrent`() = runBlocking {
        var count = 0
        List(1000){
            GlobalScope.launch { count ++ }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test safe concurrent`() = runBlocking {
        var count = AtomicInteger(0)
        List(1000){
            GlobalScope.launch { count.incrementAndGet() }
        }.joinAll()
        println(count.get())
    }

    //Channel, 安全通道
    //Mutex: 轻量锁，挂起不是阻塞
    //Semaphore: 轻量级信号量。
    @Test
    fun `test safe concurrent tools`() = runBlocking {
        var count = 0
        val mutex = Mutex()
        List(1000) {
            GlobalScope.launch {
                mutex.withLock {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test safe concurrent tools2`() = runBlocking {
        var count = 0
        val semaphore = Semaphore(1)
        List(1000) {
            GlobalScope.launch {
                semaphore.withPermit {
                    count++
                }
            }
        }.joinAll()
        println(count)
    }

    @Test
    fun `test avoid access outer variable`() = runBlocking {
        var count = 0
        //count 不要进入到 协程里面去。
        val result = count + List(1000){
            GlobalScope.async { 1 }
        }.map { it.await() }.sum()
        println(result)
    }
}