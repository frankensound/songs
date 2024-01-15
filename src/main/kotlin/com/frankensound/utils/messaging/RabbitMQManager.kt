package com.frankensound.utils.messaging

import com.frankensound.utils.EventBus
import com.rabbitmq.client.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

fun Application.messagingModule() {
    val config = environment.config
    val host = config.property("ktor.rabbitmq.host").getString()
    val port = config.property("ktor.rabbitmq.port").getString()
    val user = config.property("ktor.rabbitmq.user").getString()
    val password = config.property("ktor.rabbitmq.password").getString()

    RabbitMQManager.init(host, port, user, password)

    val deletionQueue = config.property("ktor.rabbitmq.queue.deletion").getString()
    RabbitMQManager.consumeMessages(deletionQueue)
}

object RabbitMQManager {
    private lateinit var connection: Connection
    private lateinit var channel: Channel

    fun init(host : String, port : String, user : String, password : String, ) {
        val factory = ConnectionFactory()
        factory.host = host
        factory.port = port.toInt()
        factory.virtualHost = "/"
        factory.username = user
        factory.password = password
        connection = factory.newConnection()
        channel = connection.createChannel()
    }

    fun publishMessage(queueName: String, message: String) {
        channel.queueDeclare(queueName, true, false, false, null)
        channel.basicPublish("", queueName, null, message.toByteArray())
        println(" [x] Sent '$message'")
    }

    fun consumeMessages(queueName: String) {
        channel.queueDeclare(queueName, true, false, false, null)
        val consumer = object : DefaultConsumer(channel) {
            override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray
            ) {
                val messageJson = Json.parseToJsonElement(String(body, Charsets.UTF_8))
                println(" [x] Received message: $messageJson")
                EventBus.emit(messageJson)
            }
        }
        channel.basicConsume(queueName, true, consumer)
    }

    fun close() {
        channel.close()
        connection.close()
    }
}