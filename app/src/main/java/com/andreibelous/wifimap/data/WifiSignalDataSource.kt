package com.andreibelous.wifimap.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import com.andreibelous.wifimap.cast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class WifiSignalDataSource(
    activity: AppCompatActivity
) : Disposable {

    private var timer: Disposable? = null

    private val wifiManager =
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE).cast<WifiManager>()

    private var position: Point? = null
    private var signalLevel: Int? = null

    val scanned = mutableListOf<Signal>()

    fun setCurrentPosition(point: Point) {
        position = point
    }

    fun startScan() {
        timer =
            Observable.interval(SCAN_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    signalLevel = wifiManager.connectionInfo.rssi
                    val pos = position
                    val level = signalLevel
                    if (pos != null && level != null) {
                        scanned.add(
                            Signal(
                                point = pos,
                                level = level
                            )
                        )
                    }
                }
    }

    fun stopScan() {
        scanned.clear()
        timer?.dispose()
        timer = null
    }

    override fun dispose() {
        timer?.dispose()
    }

    override fun isDisposed(): Boolean = timer?.isDisposed == true

    private companion object {

        private const val SCAN_INTERVAL_MILLIS = 100L
    }
}

data class Signal(
    val point: Point,
    val level: Int
)

data class Point(
    val x: Float,
    val y: Float
)