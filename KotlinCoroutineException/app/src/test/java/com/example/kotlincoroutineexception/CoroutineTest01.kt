package com.example.kotlincoroutineexception

import android.provider.Settings.Global
import kotlinx.coroutines.*
import org.junit.Test
import java.io.IOException

class CoroutineTest01 {

    @Test
    fun `test CoroutineContext`() = runBlocking<Unit>{
        launch(Dispatchers.Default + CoroutineName("Test")) {
            //这里会把协程的名字打印出来
            println("I'm working in thread ${Thread.currentThread().name}")
        }
    }


    @Test
    fun `test CoroutineContext extend`() = runBlocking{
        val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("test"))
        val job = scope.launch {
            println("${coroutineContext[Job]} ${Thread.currentThread().name}")
            val result = async {
                println("${coroutineContext[Job]} ${Thread.currentThread().name}")
                "OK"
            }.await()
        }
        job.join()
    }

    @Test
    fun `test CoroutineContext extend2`() = runBlocking{
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception")
        }

        val scope = CoroutineScope(
            Job() + Dispatchers.Main + coroutineExceptionHandler)

        val job = scope.launch(Dispatchers.IO){
            //新协程

        }
    }

    @Test
    fun `test exception propagation`() = runBlocking<Unit>{
        val job = GlobalScope.launch {
            try {
                throw java.lang.IndexOutOfBoundsException()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        job.join()

        val deferred = GlobalScope.async {
            throw java.lang.ArithmeticException()
        }

        try {
            deferred.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Test
    fun `test exception propagation2`() = runBlocking<Unit>{
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                //这里会往外面抛， 给到 scope.launch, 会立即 抛出， 不会在等待 await()的调用。
                throw java.lang.IllegalArgumentException()
            }
        }
        job.join()
    }

    @Test
    fun `test SupervisorJob`() = runBlocking<Unit>{
        val supervisor = CoroutineScope(SupervisorJob())
        val job1 = supervisor.launch {
            delay(100)
            println("child 1")
            throw java.lang.IllegalArgumentException()
        }

        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("child 2 finished.")
            }
        }

        joinAll(job1, job2)
    }

    @Test
    fun `test supervisorScope`() = runBlocking<Unit>{
        supervisorScope {
            launch {
                delay(100)
                println("child 1")
                throw java.lang.IllegalArgumentException()
            }

            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    println("child 2 finished.")
                }
            }
        }
    }

    @Test
    fun `test supervisorScope2`() = runBlocking<Unit>{
        supervisorScope {
            val child = launch {
                try {
                    println("the child is sleeping")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("the child is canceled")
                }
            }
            yield()
            println("Throwing an exception from the scope")
            //作用域 内有异常时，子协程会被取消掉。
            throw java.lang.AssertionError()
        }
    }


    @Test
    fun `test CoroutineExceptionHandler`() = runBlocking<Unit>{
        val handler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            throw java.lang.AssertionError()
        }

        val deferred = GlobalScope.async(handler) {
            throw java.lang.ArithmeticException()
        }

        job.join()
        deferred.await()
    }


    @Test
    fun `test CoroutineExceptionHandler2`() = runBlocking<Unit>{
        val handler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch(handler){
            launch {
                throw java.lang.IllegalArgumentException()
            }
        }
        job.join()
    }


    @Test
    fun `test CoroutineExceptionHandler3`() = runBlocking<Unit>{
        val handler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch{
            //这种情况下捕捉不到。
            launch(handler) {
                throw java.lang.IllegalArgumentException()
            }
        }
        job.join()
    }

    //取消与异常
    @Test
    fun `test cancel and exception`() = runBlocking<Unit>{
        val job = launch {
            val child = launch {
                try {
                    try {
                        delay(Long.MAX_VALUE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } finally {
                    println("Child is Cancelled.")
                }
            }
            yield()
            println("Cancelling child")
            child.cancelAndJoin()
            yield()
            println("Parent is not cancelled")
        }
        job.join()
    }


    @Test
    fun `test cancel and exception2`() = runBlocking<Unit>{
        val handler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable){
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block.")
                    }
                }
            }

            launch {
                delay(100)
                println("Second child throws an exception")
                throw java.lang.ArithmeticException()
            }
        }
        job.join()
    }

    @Test
    fun `test exception aggregation`() = runBlocking {
        val handler = CoroutineExceptionHandler{_, exception ->
            println("Caught $exception ${exception.suppressed.contentToString()}")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw ArithmeticException()
                }
            }

            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw IndexOutOfBoundsException()
                }
            }

            launch {
                delay(1000)
                throw IOException()
            }
        }
    }


    fun getSequence() = sequence { println("Add 1") yield(1) println("Add 2") yield(2) println("Add 3") yield(3) println("Add 4") yield(4)}

}