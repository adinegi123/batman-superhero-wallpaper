package com.adi121.bat_superhero_man_wallpaper.views

import android.Manifest
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.adi121.bat_superhero_man_wallpaper.R
import com.adi121.bat_superhero_man_wallpaper.databinding.ActivityDetailBinding
import com.adi121.bat_superhero_man_wallpaper.utils.ConnectionLiveData
import com.adi121.bat_superhero_man_wallpaper.utils.Constants.REQUEST_CODE_STORAGE_PERMISSION
import com.adi121.bat_superhero_man_wallpaper.utils.Constants.REQUEST_CODE_WALLPAPER_SET
import com.adi121.bat_superhero_man_wallpaper.utils.NetworkUtils
import com.adi121.bat_superhero_man_wallpaper.utils.SharedPrefs
import com.adi121.bat_superhero_man_wallpaper.utils.getBitmap
import com.adi121.bat_superhero_man_wallpaper.utils.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.io.ByteArrayOutputStream

class DetailActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityDetailBinding

    val sharedPrefs by lazy {
        SharedPrefs.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fullScreen()

        if (sharedPrefs?.isFirstTime!!) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                sharedPrefs?.isFirstTime = false
                MaterialTapTargetPrompt.Builder(this@DetailActivity)
                    .setTarget(binding.ivSetWallpaper)
                    .setPrimaryText("Set wallpaper")
                    .setSecondaryText("Set wallpaper from this button")
                    .setPromptStateChangeListener(MaterialTapTargetPrompt.PromptStateChangeListener { prompt, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            // User has pressed the prompt target

                        }
                    })
                    .show()
            }
        }


        if (NetworkUtils.isConnected(this)) {
            intent.getStringExtra("img_url")?.let { imageUrl ->

                Glide.with(binding.ivDetail)
                    .load(imageUrl)
                    .timeout(60000)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.lottieAnimationView.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.lottieAnimationView.isVisible = false
                            return false
                        }
                    })
                    .into(binding.ivDetail)


            }
        }

        binding.ivSetWallpaper.setOnClickListener {
            if (NetworkUtils.isConnected(this)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setAsWallpaper()
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (EasyPermissions.hasPermissions(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        setAsWallpaper()

                    } else {
                        requestPermissionsFromUser()
                    }
                } else {
                    setAsWallpaper()
                }

            } else {
                showToast("Internet Unavailable")
            }
        }

        binding.ivSaveImage.setOnClickListener {

            if (NetworkUtils.isConnected(this)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(this, "Downloading", Toast.LENGTH_SHORT).show()
                    CoroutineScope(Dispatchers.IO).launch {
                        downloadImage()
                    }

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (EasyPermissions.hasPermissions(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        Toast.makeText(this, "Downloading", Toast.LENGTH_SHORT).show()
                        CoroutineScope(Dispatchers.IO).launch {
                            downloadImage()
                        }

                    } else {
                        requestPermissionsFromUser()
                    }
                } else {

                    Toast.makeText(this, "Downloading", Toast.LENGTH_SHORT).show()
                    CoroutineScope(Dispatchers.IO).launch {
                        downloadImage()

                    }
                }

            } else {
                Toast.makeText(this, "Internet Unavailable", Toast.LENGTH_SHORT).show()
            }
        }


        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, {
            binding.layoutError.isVisible = !it
            binding.relAction.isVisible = it
        })

    }

    private fun requestPermissionsFromUser() {
        EasyPermissions.requestPermissions(
            this,
            "You need to allow these permissions to access this file",
            REQUEST_CODE_STORAGE_PERMISSION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private fun getImageUri(url: String): Uri {
        val bitmap = getBitmap(url)
        val bytes = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "IMG_" + System.currentTimeMillis(), null
        )
        return Uri.parse(path)
    }


    //Download Image

    private fun downloadImage() {
        val request = DownloadManager.Request(Uri.parse(intent.getStringExtra("img_url")))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Download")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "" + System.currentTimeMillis()
            )

        val downloadManager =
            this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)


    }


    // Set Wallpaper

    private fun setAsWallpaper() {
        val manager = WallpaperManager.getInstance(this)

        CoroutineScope(Dispatchers.IO).launch {

            intent.getStringExtra("img_url")?.let {
                try {
                    startActivityForResult(
                        Intent(manager.getCropAndSetWallpaperIntent(getImageUri(it))),
                        REQUEST_CODE_WALLPAPER_SET
                    )
                } catch (exception: Exception) {
                    try {
                        withContext(Dispatchers.Main) {
                            CShowProgress.showProgressBar(this@DetailActivity)
                        }
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            getImageUri(it)
                        )
                        manager.setBitmap(bitmap)
                        withContext(Dispatchers.Main) {
                            showToast("Wallpaper Changed Successfully")
                            CShowProgress.dismiss()
                            handleBackPressed()

                        }
                    } catch (exception: Exception) {
                        Log.d("TAG", "setAsWallpaper:${exception.message} ")
                        withContext(Dispatchers.Main) {
                            showToast("Can't set wallpaper")
                        }
                        CShowProgress.dismiss()
                    }

                    //Log.d("TAG", "setAsWallpaper:${exception.localizedMessage} ")
                }

            }

        }
    }


    //Permissions Callbacks

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        showToast("Permissions granted , try performing same action again")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissionsFromUser()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WALLPAPER_SET) {
            showToast("Wallpaper Changed Successfully")
            handleBackPressed()
        }
    }

    private fun fullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
//        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

    }

    override fun onResume() {
        super.onResume()
        fullScreen()
    }



    private fun handleBackPressed() {
        finish()
    }

}