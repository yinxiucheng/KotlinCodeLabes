package com.example.kotlincoroutinechannel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.junit.Test

class CoroutineTest01 {

    @Test
    fun `test know channel`() = runBlocking {
        val channel = Channel<Int> ()
        val producer = GlobalScope.launch {
            var i = 0
            while (true){
                delay(1000)
                channel.send(++i)
                println("send $i")
            }
        }

        val consumer = GlobalScope.launch {
            while (true){
                val element = channel.receive()
                println("receive $element")
            }
        }
        joinAll(producer, consumer)
    }


    @Test
    fun `test know channel2`() = runBlocking {
        val channel = Channel<Int> ()
        val producer = GlobalScope.launch {
            var i = 0
            while (true){
                delay(1000)
                channel.send(++i)
                println("send $i")
            }
        }

        val consumer = GlobalScope.launch {
            while (true){
                delay(2000)
                val element = channel.receive()
                println("receive $element")
            }
        }
        joinAll(producer, consumer)
    }


    @Test
    fun `test iterate channel2`() = runBlocking {
        val channel = Channel<Int> (Channel.UNLIMITED)
        val producer = GlobalScope.launch {
            for (x in 1..5){
                channel.send(x * x)
                println("send ${x * x}")
            }
        }

        val consumer = GlobalScope.launch {
//            val iterator = channel.iterator()
//            while (iterator.hasNext()){
//                val element = iterator.next()
//                println("receive $element")
//                delay(2000)
//            }
            for (element in channel){
                println("receive $element")
                delay(2000)
            }
        }
        joinAll(producer, consumer)
    }

    @Test
    fun `test fast producer channel`() = runBlocking {
        val receiveChannel: ReceiveChannel<Int> = GlobalScope.produce { //生产者协程
            repeat(100){
                delay(1000)
                send(it)
            }
        }

        val consumer = GlobalScope.launch {
            for (i in receiveChannel){
                println("receiver: $i")
            }
        }
        consumer.join()
    }

    @Test
    fun `test fast consumer channel`() = runBlocking {
        val sendChannel: SendChannel<Int> = GlobalScope.actor<Int> { //生产者协程
            while (true){
                val element = receive()
                println(element)
            }
        }

        val producer = GlobalScope.launch {
            for (i in 0..3){
                sendChannel.send(i)
            }
        }
        producer.join()
    }

    @Test
    fun `test close channel`() = runBlocking {
        val channel = Channel<Int> (3)
        val producer = GlobalScope.launch {
            List(3){
                channel.send(it)
                println("send $it")
            }
            channel.close()
            println("""close channel.
                | -ClosedForSend: ${channel.isClosedForSend}
                | -ClosedForReceive: ${channel.isClosedForReceive}""".trimMargin())
        }

        val consumer = GlobalScope.launch {
            for (element in channel){
                println("receive $element")
                delay(1000)
            }
            println("""After consuming.
                | -ClosedForSend: ${channel.isClosedForSend}
                | -ClosedForReceive: ${channel.isClosedForReceive}""".trimMargin())
        }
        joinAll(producer, consumer)

//        send 0
//        receive 0
//        send 1
//        send 2
//        close channel.
//        -ClosedForSend: true
//        -ClosedForReceive: false
//        receive 1
//        receive 2
//        After consuming.
//        -ClosedForSend: true
//        -ClosedForReceive: true
    }


    @Test
    fun `test broadcast channel`() = runBlocking {
        val broadcastChannel = BroadcastChannel<Int>(Channel.BUFFERED)
        val producer = GlobalScope.launch {
            List(3){
                delay(100)
                broadcastChannel.send(it)
            }
            broadcastChannel.close()
        }

        List(3){index ->
            GlobalScope.launch {
                val receiveChannel = broadcastChannel.openSubscription()
                for (i in receiveChannel){
                    println("[$index] received: $i")
                }
            }
        }.joinAll()

    }

}