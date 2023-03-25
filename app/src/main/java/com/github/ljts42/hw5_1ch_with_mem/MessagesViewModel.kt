package com.github.ljts42.hw5_1ch_with_mem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream

class MessagesViewModel : ViewModel() {
    private val messagesList: MutableList<Message> = mutableListOf()
    private var lastMessageId = 5000

    fun getImage(cacheDir: File, url: String): Bitmap? {
        var image: Bitmap? = null
        if (image == null) {
            val file = File(cacheDir, url)
            if (file.exists()) {
                image = BitmapFactory.decodeFile(file.absolutePath)
            } else {
                image = MyInternetUtility.loadImage(url)
                if (image != null) {
                    file.parentFile?.mkdirs()
                    file.createNewFile()
                    FileOutputStream(file).use {
                        image.compress(Bitmap.CompressFormat.PNG, 100, it)
                        it.flush()
                    }
                }
            }
        }
        return image
    }

    fun getMessages(): MutableList<Message> {
        return messagesList
    }

    fun addMessages(messages: List<Message>) {
        messagesList.addAll(messages)
    }

    fun getLastId() = lastMessageId

    fun updateLastId(newLast: Int) {
        lastMessageId = newLast
    }
}