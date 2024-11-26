package org.example

import io.ktor.network.sockets.*
import java.io.File
import java.net.Socket

fun receiveImageServer(serverAddress: String, port: Int, outputPatch: String) {
    val socket = Socket(serverAddress,port)
    println("Подключение к серверу: $serverAddress на порту $port")

    socket.getInputStream().use { inputStream ->
        val byteArray = inputStream.readBytes() //Чтение массива байтов
        File(outputPatch).writeBytes(byteArray) //Запись массива в файл
    }
    socket.close()
    println("Изображение сохранено в $outputPatch")
}