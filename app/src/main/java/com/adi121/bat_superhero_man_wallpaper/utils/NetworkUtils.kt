package com.adi121.bat_superhero_man_wallpaper.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkUtils {
    companion object{
        fun isConnected(context: Context?):Boolean{
            return if(context!=null){
                val cm: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo=cm.activeNetworkInfo
                networkInfo!=null && networkInfo.isConnectedOrConnecting
            }else false
        }

    }
}