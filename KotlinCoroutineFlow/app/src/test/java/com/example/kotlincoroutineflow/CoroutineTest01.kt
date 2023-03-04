package com.example.kotlincoroutineflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import kotlin.system.measureTimeMillis

class CoroutineTest01 {

    fun simpleList(): List<Int> = listOf(1, 2, 3)

    //返回了多个值，是同步的
    fun simpleSequence(): Sequence<Int> = sequence {
        for (i in 1..3){
//            Thread.sleep(1000)// 这个是阻塞的。
            yield(i)
        }
    }

    suspend fun simpleList2(): List<Int>{
        delay(1000)
        return listOf(1, 2, 3)
    }

   fun  simpleFlow() = flow<Int>{
        for (i in 1..3){
            delay(1000)//假装在做一些耗时的操作。
            emit(i)
        }
    }
    @Test
    fun `test multiple values`(){
        simpleSequence().forEach { value ->
            println(value)
        }
    }


    @Test
    fun `test multiple values2`() = runBlocking {
        simpleList2().forEach { value -> println(value) }
    }


    @Test
    fun `test multiple values3`() = runBlocking {
        launch {
            for (k in 1..3){
                println("I'm not blocked")
                delay(1500)
            }
        }
        simpleFlow().collect{ value -> println(value) }
    }

    fun  simpleFlow2() = flow<Int>{
        println("Flow started")
        for (i in 1..3){
            delay(1000)
            emit(i)
        }
    }
    @Test
    fun `test Flow is cold`() = runBlocking {
        val flow = simpleFlow2()
        println("Calling collect...")
        flow.collect{value -> println(value) }
        println("Calling collect again...")
        flow.collect{value -> println(value) }
    }

    @Test
    fun `test Flow continuation`() = runBlocking {
        (1..5).asFlow().filter {
            it % 2 == 0
        }.map { "String $it" }.collect{
            println("Collect $it")
        }
    }

    @Test
    fun `test flow builder`() = runBlocking {
        flowOf("One", "Two", "Three").onEach { delay(1000) }
            .collect{
                println(it)
            }

        (1..5).asFlow().collect{
            println(it)
        }
    }

    fun  simpleFlow3() = flow<Int>{
        println("Flow started ${Thread.currentThread().name}")
        for (i in 1..3){
            delay(1000)
            emit(i)
        }
    }

    fun  simpleFlow4() = flow<Int>{
        withContext(Dispatchers.IO){
            println("Flow started ${Thread.currentThread().name}")
            for (i in 1..3){
                delay(1000)
                emit(i)
            }
        }
    }



    @Test
    fun `test flow context`() = runBlocking {
        simpleFlow3().collect{
            value -> println("Collected $value ${Thread.currentThread().name}")
        }
        simpleFlow4().collect{ //error
                value -> println("Collected $value ${Thread.currentThread().name}")
        }
    }


    fun  simpleFlow5() = flow<Int>{
        println("Flow started ${Thread.currentThread().name}")
        for (i in 1..3){
            delay(1000)
            emit(i)
        }
    }.flowOn(Dispatchers.Default)//切换协程

    @Test
    fun `test flow on`() = runBlocking {
        simpleFlow5().collect{
                value -> println("Collected $value ${Thread.currentThread().name}")
        }
    }

    //事件源
    fun events() = (1..3)
        .asFlow()
        .onEach { delay(100) }
        .flowOn(Dispatchers.Default)


    @Test
    fun `test flow launch`() = runBlocking {
        val job = events()
            .onEach { event -> println("Event: $event ${Thread.currentThread().name}") }
//            .collect{}
//            .launchIn(CoroutineScope(Dispatchers.IO))
            .launchIn(this)
//            .join()

        delay(1500)
        job.cancelAndJoin()
    }


    fun  simpleFlow6() = flow<Int>{
        for (i in 1..3){
            delay(1000)
            emit(i)
            println("Emitting $i")
        }
    }

    @Test
    fun `test cancel flow`() = runBlocking {
        withTimeoutOrNull(2500){
            simpleFlow6().collect{value-> println(value) }
        }
        println("Done")
    }

    fun  simpleFlow7() = flow<Int>{
        for (i in 1..5){
            emit(i)// 这里会调用ensureActive（）
            println("Emitting $i")
        }
    }

    @Test
    fun `test cancel flow check`() = runBlocking {
        simpleFlow7().collect{value->
            println(value)
            if (value == 3) cancel()
        }

        (1..5).asFlow().collect{// 这个取消不掉
            println(it)
            if (it == 3) cancel()
        }

        (1..5).asFlow().cancellable().collect{// 加上cancellable, 但是影响效率
            println(it)
            if (it == 3) cancel()
        }
    }

    private fun simpleFlow8() = flow<Int> {
        for (i in 1..5) {
            delay(100)
            emit(i)// 这里会调用ensureActive（）
            println("Emitting $i ${Thread.currentThread().name}")
        }
    }

    @Test
    fun `test flow back pressure`() = runBlocking {
        val time = measureTimeMillis {
            simpleFlow8()
                .flowOn(Dispatchers.Default)//自带默认的缓冲。
//                .buffer(50)// 并发， 主线程并行发送三个数据。
                .collect { value -> //收集还在主线程
                    delay(300) //处理这个元素消耗300 毫秒
                    println("Collected $value ${Thread.currentThread().name}")
                }
        }
        println("Collected in $time ms")
    }


}