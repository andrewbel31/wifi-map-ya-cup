package com.andreibelous.wifimap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.andreibelous.wifimap.data.WifiSignalDataSource
import com.andreibelous.wifimap.feature.Stage
import com.andreibelous.wifimap.feature.WifiMapFeature
import com.andreibelous.wifimap.mapper.NewsToViewAction
import com.andreibelous.wifimap.mapper.StateToViewModel
import com.andreibelous.wifimap.mapper.UiEventToWish
import com.andreibelous.wifimap.view.MapView
import com.badoo.binder.Binder
import com.badoo.binder.using
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private var disposables: CompositeDisposable? = null
    private var dialog: AlertDialog? = null
    private val fragment by lazy {
        supportFragmentManager.findFragmentById(R.id.ar_fragment)!!.cast<WIfiMapArFragment>()
    }
    private var feature: WifiMapFeature? = null

    private val dataSource by lazy { WifiSignalDataSource(this) }

    private val permissions =
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions(permissions)) {
            startRequesting()
        } else {
            permissionsGranted()
        }
    }

    private fun permissionsGranted() {
        val feature = WifiMapFeature(dataSource).also { this.feature = it }
        val view = MapView(this, { dataSource.setCurrentPosition(it) })
        Binder(CreateDestroyBinderLifecycle(lifecycle)).apply {
            bind(view to feature using UiEventToWish)
            bind(feature to view using StateToViewModel)
            bind(feature.news to view::execute.asConsumer() using NewsToViewAction)
        }
    }

    private fun startRequesting() {
        val shouldShowRationale =
            permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(this, it) }

        if (shouldShowRationale) {
            showRationale()
            return
        }

        requestPermissions()
    }

    private fun showRationale() {
        dialog?.dismiss()
        AlertDialog.Builder(this)
            .setTitle("Нет необходимых разрешений")
            .setMessage("Без этих разрешений приложение не сможет работать :(")
            .setPositiveButton("дать разрешения") { _, _ ->
                dismissDialog()
                requestPermissions()
            }
            .setNegativeButton("отмена") { _, _ ->
                dismissDialog()
                finish()
            }
            .setCancelable(true)
            .create()
            .also { dialog = it }
            .show()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = mutableSetOf<String>()
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                    granted += permission
                }
            }
        }

        if (granted.containsAll(permissions.toList())) {
            permissionsGranted()
        } else {
            showRationale()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat
                .checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onBackPressed() {
        if (feature?.state?.stage is Stage.Finished) {
            feature?.accept(WifiMapFeature.Wish.Init)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        dispose()
        super.onDestroy()
    }

    private fun dismissDialog() {
        dialog?.dismiss()
        dialog = null
    }

    private fun dispose() {
        dismissDialog()
        disposables?.dispose()
        disposables = null
    }

    private companion object {

        private const val PERMISSIONS_REQUEST_CODE = 101
    }
}