package com.adi121.bat_superhero_man_wallpaper.utils

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.bumptech.glide.Glide
import java.io.IOException

fun Context.showToast(message: String?){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

 fun Context.getBitmap(url: String): Bitmap? {
    var bitmap: Bitmap? = null
    try {
        bitmap = Glide.with(this)
            .asBitmap()
            .load(url)
            .submit()
            .get()
    } catch (e: IOException) {
    }
    return bitmap
}