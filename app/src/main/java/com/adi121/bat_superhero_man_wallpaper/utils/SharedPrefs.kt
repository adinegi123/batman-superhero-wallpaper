package com.adi121.bat_superhero_man_wallpaper.utils
import android.content.Context
import android.content.SharedPreferences


class SharedPrefs private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor

    init {
        editor = sharedPreferences.edit()
    }
    companion object {
        private var sharedPrefs: SharedPrefs? = null
        fun getInstance(context: Context): SharedPrefs? {
            if (sharedPrefs == null) {
                sharedPrefs = SharedPrefs(context)
            }
            return sharedPrefs
        }
    }


    var lastAdShownTime:Long
    get()=sharedPreferences.getLong("lastAdShownTime",0L)
    set(value) {
        editor.putLong("lastAdShownTime",value).apply()
    }



    var isFirstTime:Boolean
        get()=sharedPreferences.getBoolean("isFirstTime",true)
        set(value) {
            editor.putBoolean("isFirstTime",value).apply()
        }


}