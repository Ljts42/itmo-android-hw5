package com.github.ljts42.hw5_1ch_with_mem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset


object MyInternetUtility {

    private const val serverLink = "http://213.189.221.170:8008"

    fun getMessages(start: Int, count: Int): List<Message> {
        try {
            val url = URL("$serverLink/1ch?lastKnownId=$start&limit=$count")
            val array = JSONArray(url.readText())
            return (0 until array.length()).map {
                val el = array.getJSONObject(it)
                val data = el.getJSONObject("data")
                Message(
                    el.getString("id"),
                    el.getString("from"),
                    el.getString("to"),
                    if (data.has("Text")) MessageType.TEXT else MessageType.IMAGE,
                    if (data.has("Text")) data.getJSONObject("Text").getString("text")
                    else data.getJSONObject("Image").getString("link"),
                    el.getString("time")
                )
            }.toList()
        } catch (e: IOException) {
            Log.e("GetMessages", "Failed to get messages: ${e.message}")
            return emptyList()
        }
    }

    fun sendMessage(from: String, data: String) {
        try {
            val url = URL("$serverLink/1ch")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            val jsonMessage = JSONObject().put("from", from)
                .put("data", JSONObject().put("Text", JSONObject().put("text", data))).toString()
            connection.connect()
            val outputStream = connection.outputStream
            outputStream.write(jsonMessage.toByteArray(Charset.defaultCharset()))
            outputStream.close()

            if (connection.responseCode == 200) {
                Log.d("SendMessage", "Message successfully sent")
            } else {
                Log.e(
                    "SendMessage",
                    "Failed to send message, response code: ${connection.responseCode}"
                )
            }
        } catch (e: IOException) {
            Log.e("SendMessage", "Failed to send message: ${e.message}")
        }
    }

    fun loadImage(path: String): Bitmap? {
        try {
            val url = URL("$serverLink/$path")
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            Log.e("DownloadImage", "Failed to download image: ${e.message}")
        } catch (e: FileNotFoundException) {
            Log.e("DownloadImage", "Failed to download image: ${e.message}")
        }
        return null
    }

    fun sendImage(from: String, image: Bitmap) {
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()

            val url = URL("$serverLink/1ch")
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            val boundary = MultipartTool.generateBoundary()
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            connection.outputStream.use {
                MultipartTool(it, boundary).apply {
                    appendJsonField("msg", "{\"from\":\"$from\"}")
                    appendFile("pic", bytes, "image/PNG", "${System.currentTimeMillis()}.png")
                    close()
                }
                it.flush()
            }

            if (connection.responseCode == 200) {
                Log.d("SendImage", "Image successfully sent")
            } else {
                Log.e(
                    "SendImage", "Failed to send image, response code: ${connection.responseCode}"
                )
            }
        } catch (e: IOException) {
            Log.e("SendImage", "Failed to send image: ${e.message}")
        } catch (e: FileNotFoundException) {
            Log.e("SendImage", "Failed to send image: ${e.message}")
        }
    }
}
