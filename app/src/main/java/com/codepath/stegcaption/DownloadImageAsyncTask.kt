package com.codepath.stegcaption

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView

class DownloadImageAsyncTask(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String?): Bitmap? {
        val imageUrl = params[0]
        var image: Bitmap? = null
        try {
            val inputStream = java.net.URL(imageUrl).openStream()
            image = BitmapFactory.decodeStream(inputStream)
            Log.d("Attempted to download image from: ", imageUrl!!)
            Log.d("Image is null: ", "${image == null}")
        } catch (e: Exception) {
            e.message?.let { Log.e("Error", it) }
            e.printStackTrace()
        }
        return image;
    }
    @Deprecated("Deprecated in Java", ReplaceWith("imageView.setImageBitmap(result)"))
    override fun onPostExecute(result: Bitmap?) {
        imageView.setImageBitmap(result)
    }
}

