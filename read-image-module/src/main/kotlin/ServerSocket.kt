package org.example

import java.net.ServerSocket

fun startServer(port: Int, imagePath: String) {
    val serverSocket = ServerSocket(port)
    println("Сервер запущен на порту $port")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Клиент подключен: ${clientSocket.inetAddress}")

        val byteArray = readImageToByteArray(imagePath)

        clientSocket.getOutputStream().use { outputStream ->
            outputStream.write(byteArray)
            outputStream.flush()
        }
        clientSocket.close()
    }
}