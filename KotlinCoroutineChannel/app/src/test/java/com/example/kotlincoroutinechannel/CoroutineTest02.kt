package com.example.kotlincoroutinechannel

import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.selects.select
import org.junit.Test
import java.io.File

private val cachePath = "~/coroutine.cache"
private val gson = Gson()

data class Response<T>(val value:T, val isLocal:Boolean)

fun CoroutineScope.getUserFromLocal(name:String) = async(Dispatchers.IO){
    delay(1000)
    File(cachePath).readText().let { gson.fromJson(it, User::class.java) }
}

fun CoroutineScope.getUserFromRemote(name:String) = async(Dispatchers.IO){
    delay(100)
    User(name)
//    userServiceApi.getUser(name)
}

class CoroutineTest02 {
    @Test
    fun `test select await`() = runBlocking {
        GlobalScope.launch {
            val localRequest = getUserFromLocal("xxx")
            val remoteRequest = getUserFromRemote("yyy")

            val userResponse = select<Response<User>> {
                localRequest.onAwait{Response(it, true)}
                remoteRequest.onAwait{Response(it, false)}
            }
            userResponse.value?.let { println(it) }
        }.join()
    }

    @Test
    fun `test select channel`() = runBlocking {
        val channels = listOf(Channel<Int>(), Channel<Int>())
        GlobalScope.launch {
            delay(100)
            channels[0].send(200)
        }

        GlobalScope.launch {
            delay(50)
            channels[1].send(100)
        }

        val result = select<Int?> {
            channels.forEach{
                channel ->  channel.onReceive{it}
            }
        }
        println(result)

        delay(1000)
    }

    @Test
    fun `test SelectClause0`() = runBlocking {
        val job1 = GlobalScope.launch {
            delay(100)
            println("job 1")
        }

        val  job2 = GlobalScope.launch {
            delay(10)
            println("job 2")
        }

        select<Unit> {
            job1.onJoin{ println("job 1 onJoin") }
            job2.onJoin{ println("job 2 onJoin") }
        }
        delay(1000)
    }

    @Test
    fun `test SelectClause2`() = runBlocking {
        val channels = listOf(Channel<Int>(), Channel<Int>())
        println(channels)

        launch(Dispatchers.IO) {
            select {  }
        }
        delay(1000)
    }

    // flow 模拟多路复用。
    @Test
    fun `test select flow`() = runBlocking<Unit> {
        val name = "guest"
        coroutineScope {
            listOf(::getUserFromLocal, ::getUserFromRemote)
                .map { function -> function.invoke(name) }
                .map { deferred ->  flow{emit(deferred.await())}}
                .merge().collect{user -> println(user) }
        }
    }


}

data class User(val name:String="")