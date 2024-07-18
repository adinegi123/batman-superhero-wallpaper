

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.adi121.bat_superhero_man_wallpaper.R


object CShowProgress {
    var dialog: Dialog?=null
    fun showProgressBar(context: Context){
        dialog= Dialog(context)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.progressbar)
        dialog?.setCancelable(true)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()

    }

    fun dismiss(){
        dialog?.dismiss()
    }

}