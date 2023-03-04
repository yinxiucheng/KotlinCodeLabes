package com.example.kotlincoroutineflow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoroutineTest02 {

    suspend fun performRequest(request:Int) :String{
        delay(1000)
        return "reponse $request"
    }

    fun simpleSequence(): Sequence<Int> = sequence {
    }

    @Test
    fun `test transform flow operator`() = runBlocking {
//        (1..3).asFlow()
//            .map { request -> performRequest(request) }
//            .collect{value -> println(value) }

        (1..3).asFlow()
            .transform { request -> //做更复杂的转换。
                emit("Marking request $request")
                emit(performRequest(request))
            }
            .collect{value -> println(value) }
    }

    fun numbers() = flow<Int> {
        try {
            emit(1)
            emit(2)
            println("This line will not execute")
            emit(3)
        } finally {
            println("Finally in numbers")
        }
    }

    @Test //take()
    fun `test limit length operator`() = runBlocking {
        numbers().take(2).collect{value -> println(value) }
    }

    //末端操作符. collect / reduce/ fold
    @Test
    fun `test terminal operator`() = runBlocking {
        val sum = (1..5).asFlow()
            .map { it * it }
            .reduce{
                a, b -> a + b
             }

        println(sum)
    }


    @Test
    fun `test zip`() = runBlocking {
        val numbs = (1..3).asFlow().onEach { delay(300) }
        val strs = flowOf("One", "Two", "Three").onEach { delay(400) }
        val startTime = System.currentTimeMillis()

        numbs.zip(strs){a, b -> "$a -> $b"}.collect{
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }

//        1 -> One at 410 ms from start
//        2 -> Two at 815 ms from start
//        3 -> Three at 1222 ms from start
    }

    fun requestFlow(i:Int) = flow<String> {
        emit("$i: First")
        delay(500)
        emit("$i: Second")
    }

//    1: First at 110 ms from start
//    1: Second at 616 ms from start
//    2: First at 720 ms from start
//    2: Second at 1225 ms from start
//    3: First at 1330 ms from start
//    3: Second at 1835 ms from start
    @Test
    fun `test flatMapConcat`() = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
//            .map { requestFlow(it) }//Flow<Flow<String>>
            .flatMapConcat { requestFlow(it) }
            .collect{ println("$it at ${System.currentTimeMillis() - startTime} ms from start")}
    }


//    1: First at 132 ms from start
//    2: First at 231 ms from start
//    3: First at 336 ms from start
//    1: Second at 637 ms from start
//    2: Second at 731 ms from start
//    3: Second at 843 ms from start
    @Test
    fun `test flatMapMerge`() = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
//            .map { requestFlow(it) }//Flow<Flow<String>>
            .flatMapMerge { requestFlow(it) }
            .collect{ println("$it at ${System.currentTimeMillis() - startTime} ms from start")}
    }

//    1: First at 132 ms from start
//    2: First at 255 ms from start
//    3: First at 361 ms from start
//    3: Second at 867 ms from start
    @Test
    fun `test flatMapLatest`() = runBlocking {
        val startTime = System.currentTimeMillis()
        (1..3).asFlow()
            .onEach { delay(100) }
//            .map { requestFlow(it) }//Flow<Flow<String>>
            .flatMapLatest { requestFlow(it) }
            .collect{ println("$it at ${System.currentTimeMillis() - startTime} ms from start")}
    }
}