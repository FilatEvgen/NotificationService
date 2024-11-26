package org.example

import kotlinx.io.files.Path
import java.io.File

fun readImageToByteArray(imagePath: String): ByteArray {
    val file = File(imagePath)
    return file.readBytes()  //читаем файл в массив байтов
}