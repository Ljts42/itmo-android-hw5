package com.github.ljts42.hw5_1ch_with_mem

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class ImageModel : ViewModel() {
    private var image: Bitmap? = null

    fun getImage(): Bitmap? = image
    fun setImage(img: Bitmap) {
        image = img
    }
}