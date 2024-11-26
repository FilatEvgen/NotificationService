package org.example

fun main() {
    Thread {
        startServer(9999, "/home/user/Рабочий стол/notif.png")
    }.start()
    Thread.sleep(1000)

    receiveImageServer("localhost", 9999, "/home/user/Изображения/save/notification.png")
}