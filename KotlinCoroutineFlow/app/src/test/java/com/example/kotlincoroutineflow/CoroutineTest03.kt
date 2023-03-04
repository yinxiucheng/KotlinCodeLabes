package com.example.kotlincoroutineflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import kotlin.system.measureTimeMillis

class CoroutineTest03 {

   fun  simpleFlow() = flow<Int>{
        for (i in 1..3){
            println("Emitting $i")
            emit(i)
        }
    }

    @Test
    fun `test flow exception`() = runBlocking {
        try {
            simpleFlow().collect{ value ->
                println(value)
                //收集的时候抛出了Exception
                check(value <= 1) {"Collected $value"}
            }
        } catch (e: Exception) {// 只捕获了下游的异常
            e.printStackTrace()
        }
    }


    @Test
    fun `test flow exception2`() = runBlocking {
//        flow {
//            emit(1)
//            throw java.lang.ArithmeticException("Div 0")
//        }.catch { e:Throwable -> println("Caught $e") } //捕获了上游的异常
//            .flowOn(Dispatchers.IO)
//            .collect{
//                println(it)
//            }

        flow {
            emit(1)
            throw java.lang.ArithmeticException("Div 0")
        }.catch { e: Throwable ->
            println("Caught $e")
            emit(10)
        } //捕获了上游的异常, 异常中恢复，重新发送 一个 default value 10.
            .flowOn(Dispatchers.IO)
            .collect {
                println(it)
            }
    }

    fun simpleFlow2() = (1..3).asFlow()


    @Test
    fun `test flow complete in finally`() = runBlocking {
        try {
            simpleFlow2().collect { println(it) }
        } finally {
            println("Done")
        }
    }

    fun simpleFlow3() = flow<Int> {
        emit(1)
        throw java.lang.RuntimeException()
    }

    @Test
    fun `test flow complete in onCompletion`() = runBlocking {
//        simpleFlow3()
//            .onCompletion { exception -> println(exception) }
//            .catch { exception -> println("Caught $exception") }
//            .collect { println(it) }


        try {
            simpleFlow3()
                .onCompletion { exception -> if (exception != null) println("Flow completed exception") }
                .collect{
                    println(it)
                    check(it <= 1){"Collected $it"}
                }
        } catch (e: Exception) {
            println("Exception ${e}")
        }
    }
}