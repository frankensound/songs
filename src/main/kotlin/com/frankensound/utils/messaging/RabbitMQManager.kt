package com.frankensound.utils.messaging

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Channel
import io.ktor.server.application.*

fun Application.messagingModule() {
    val config = environment.config
    val host = config.property("ktor.rabbitmq.host").getString()
    val port = config.property("ktor.rabbitmq.port").getString()
    val user = config.property("ktor.rabbitmq.user").getString()
    val password = config.property("ktor.rabbitmq.password").getString()

    RabbitMQManager.init(host, port, user, password)
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
        channel.queueDeclare(queueName, false, false, false, null)
        channel.basicPublish("", queueName, null, message.toByteArray())
        println(" [x] Sent '$message'")
    }

    fun close() {
        channel.close()
        connection.close()
    }
}