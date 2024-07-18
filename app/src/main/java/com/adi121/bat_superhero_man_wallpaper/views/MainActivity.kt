package com.adi121.bat_superhero_man_wallpaper.views

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import com.adi121.bat_superhero_man_wallpaper.R
import com.adi121.bat_superhero_man_wallpaper.adapters.ImageAdapter
import com.adi121.bat_superhero_man_wallpaper.databinding.ActivityMainBinding
import com.adi121.bat_superhero_man_wallpaper.paging.ImageRepository
import com.adi121.bat_superhero_man_wallpaper.utils.ConnectionLiveData
import com.adi121.bat_superhero_man_wallpaper.utils.Constants.UPDATE_REQUEST_CODE
import com.adi121.bat_superhero_man_wallpaper.utils.DownloadBroadcastReceiver
import com.adi121.bat_superhero_man_wallpaper.utils.NetworkUtils
import com.adi121.bat_superhero_man_wallpaper.utils.showToast
import com.adi121.bat_superhero_man_wallpaper.viewmodels.MainViewModel
import com.adi121.bat_superhero_man_wallpaper.viewmodels.MainViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ImageAdapter.OnHomeImageCLicked {

    lateinit var adapter: ImageAdapter
    lateinit var viewModel: MainViewModel
    lateinit var downloadBroadcastReceiver: DownloadBroadcastReceiver

    private val fireStore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val appUpdateManager by lazy {
        AppUpdateManagerFactory.create(this)
    }

    private val appUpdatedListener: InstallStateUpdatedListener by lazy {
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(installState: InstallState) {
                when {
                    installState.installStatus() == InstallStatus.DOWNLOADED -> popupSnackbarForCompleteUpdate()
                    installState.installStatus() == InstallStatus.INSTALLED -> appUpdateManager.unregisterListener(
                        this
                    )

                    else -> Log.d("Instalstate", installState.installStatus().toString())
                }
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ImageAdapter(this)
        binding.rvHome.adapter = adapter
        val repository = ImageRepository(fireStore)
        val viewModelFactory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        fullScreen()

        downloadBroadcastReceiver = DownloadBroadcastReceiver()
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE).also {
//            registerReceiver(downloadBroadcastReceiver, it)
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE).also {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU){
                    registerReceiver(downloadBroadcastReceiver,it, RECEIVER_EXPORTED)
                }else{
                    registerReceiver(downloadBroadcastReceiver, it)
                }
            }
        }

        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer {
            binding.layoutError.isVisible = !it
        })

        if (NetworkUtils.isConnected(this)) {
            getData()
        }


        binding.refreshLayout.setOnRefreshListener {
            if (connectionLiveData.value == true) {
                getData()
                binding.refreshLayout.isRefreshing = false
            } else {
                binding.refreshLayout.isRefreshing = false
            }
        }

        adapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading ||
                loadState.append is LoadState.Loading
            ) {
                binding.lottieAnimationView.isVisible = true
            } else {
                binding.lottieAnimationView.isVisible = false
                val errorState = when {
                    loadState.append is LoadState.Error -> loadState.append as LoadState.Error
                    loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
                    loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
                    else -> null
                }
                errorState?.let {
                    if (it.error is ArrayIndexOutOfBoundsException) {


                    } else {
                        //binding.btnRetry.isVisible = true
                        showToast(it.error.message.toString())

                    }

                }
            }
        }

        checkForAppUpdate()

    }


    private fun checkForAppUpdate() {
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                try {
                    val installType = when {
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
                        else -> null
                    }
                    if (installType == AppUpdateType.FLEXIBLE) appUpdateManager.registerListener(
                        appUpdatedListener
                    )

                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        installType!!,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getData() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getImages().observe(this@MainActivity, Observer {
                it?.let { data ->
                    adapter.submitData(lifecycle, data)
                }
            })
        }
    }

    override fun onImageCLicked(url: String) {
        val intent = Intent(this, DetailActivity::class.java)
            .apply {
                putExtra("img_url", url)
                startActivity(this)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadBroadcastReceiver)
    }


    override fun onResume() {
        super.onResume()
        fullScreen()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdate()
                }

                //Check if Immediate update is required
                try {
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_REQUEST_CODE
                        )
                    }
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
    }


    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(
            findViewById(R.id.homeFrame),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Restart") { appUpdateManager.completeUpdate() }
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.black))
        snackbar.show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(
                    this,
                    "App Update failed, please try again on the next app launch.",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun fullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


    }


}