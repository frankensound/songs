package com.frankensound.utils.messaging

import com.frankensound.utils.EventBus
import com.rabbitmq.client.*
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.net.ssl.SSLContext

fun Application.messagingModule() {
    val config = environment.config
    val host = config.property("ktor.rabbitmq.host").getString()
    val port = config.property("ktor.rabbitmq.port").getString()
    val user = config.property("ktor.rabbitmq.user").getString()
    val password = config.property("ktor.rabbitmq.password").getString()

    RabbitMQManager.init(host, port, user, password)
    
    if (RabbitMQManager.isConnected()) {
        val deletionQueue = config.property("ktor.rabbitmq.queue.deletion").getString()
        RabbitMQManager.consumeMessages(deletionQueue)
    } else {
        println("RabbitMQ connection failed, skipping message consumption setup")
    }
}

object RabbitMQManager {
    private var connection: Connection? = null
    private var channel: Channel? = null

    fun init(host : String, port : String, user : String, password : String, ) {
        try {
            val factory = ConnectionFactory()
            factory.host = host
            factory.port = port.toInt()
            factory.virtualHost = "/"
            factory.username = user
            factory.password = password

            // Enable SSL
            val sslContext = SSLContext.getDefault()
            factory.useSslProtocol(sslContext)

            connection = factory.newConnection()
            channel = connection?.createChannel()
            println("RabbitMQ connected successfully")
        } catch (e: Exception) {
            println("Failed to initialize RabbitMQ: ${e.message}")
        }
    }

    fun publishMessage(queueName: String, message: String) {
        channel?.queueDeclare(queueName, true, false, false, null)
        channel?.basicPublish("", queueName, null, message.toByteArray())
        println(" [x] Sent '$message'")
    }

    fun consumeMessages(queueName: String) {
        val channel = this.channel
        if (channel == null) {
            println("RabbitMQ channel is not initialized")
            return
        }
        channel.queueDeclare(queueName, true, false, false, null)
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    val messageJson = Json.parseToJsonElement(String(body, Charsets.UTF_8))
                    println(" [x] Received message: $messageJson")
                    EventBus.emit(messageJson)
                }
            }
        }
        channel.basicConsume(queueName, true, consumer)
    }

    fun isConnected(): Boolean {
        return this.connection != null && this.channel != null
    }

    fun close() {
        try {
            channel?.close()
            connection?.close()
            println("RabbitMQ connection closed")
        } catch (e: Exception) {
            println("Error closing RabbitMQ connection: ${e.message}")
        }
    }
}