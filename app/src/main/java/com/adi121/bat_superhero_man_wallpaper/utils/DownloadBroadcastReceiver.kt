package com.adi121.bat_superhero_man_wallpaper.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadBroadcastReceiver :BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        val action= intent?.action
        if(DownloadManager.ACTION_DOWNLOAD_COMPLETE==action){
            Toast.makeText(p0, "Download completed", Toast.LENGTH_SHORT).show()
        }
    }
}