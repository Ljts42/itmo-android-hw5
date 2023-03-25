package com.github.ljts42.hw5_1ch_with_mem

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

class FullImage : AppCompatActivity() {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var imageModel: ImageModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)
        imageModel = ViewModelProvider(this)[ImageModel::class.java]

        val url = intent.getStringExtra("imageUrl")
        val imageView: ImageView = findViewById(R.id.image_big)
        if (imageModel.getImage() == null) {
            imageView.setImageResource(R.drawable.ic_broken_image)

            executor.execute {
                val cacheDir = applicationContext.cacheDir
                val file = File(cacheDir, "img/$url")
                var image: Bitmap? = null
                if (file.exists()) {
                    image = BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    image = MyInternetUtility.loadImage("img/$url")
                    if (image != null) {
                        file.parentFile?.mkdirs()
                        file.createNewFile()
                        FileOutputStream(file).use {
                            image.compress(Bitmap.CompressFormat.PNG, 100, it)
                            it.flush()
                        }
                    }
                }
                runOnUiThread {
                    if (image != null) {
                        imageModel.setImage(image)
                        imageView.setImageBitmap(image)
                    }
                }
            }
        } else {
            imageView.setImageBitmap(imageModel.getImage())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}