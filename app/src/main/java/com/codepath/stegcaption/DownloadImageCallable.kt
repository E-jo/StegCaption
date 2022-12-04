package com.codepath.stegcaption

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.util.concurrent.Callable

class DownloadImageCallable(var imageUrl: String) : Callable<Bitmap?> {
    override fun call(): Bitmap? {
        var image: Bitmap? = null
        try {
            val inputStream = java.net.URL(imageUrl).openStream()
            image = BitmapFactory.decodeStream(inputStream)
            Log.d("Attempted to download image from: ", imageUrl)
            Log.d("Image is null: ", "${image == null}")
        } catch (e: Exception) {
            e.message?.let { Log.e("Error", it) }
            e.printStackTrace()
        }
        return image;
    }
}