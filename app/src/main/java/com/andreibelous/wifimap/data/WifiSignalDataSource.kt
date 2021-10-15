package com.andreibelous.wifimap.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.andreibelous.wifimap.cast
import com.andreibelous.wifimap.plusAssign
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
class WifiSignalDataSource(
    private val activity: AppCompatActivity
) : Disposable {

    private val disposables = CompositeDisposable()

    private val wifiManager =
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE).cast<WifiManager>()
    private val connectivityManager =
        activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)
            .cast<ConnectivityManager>()

    private val position = BehaviorRelay.create<Point>()
    private val signalLevel = BehaviorRelay.create<Int>()

    val scanned = mutableListOf<Signal>()
    private var lastScanSuccess: Boolean = false

    init {
        val request =
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {}


            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {

                val wifiInfo =
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                        networkCapabilities.transportInfo?.cast<WifiInfo>()
                    } else {
                        wifiManager.connectionInfo
                    } ?: return

                val level =
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        wifiManager.calculateSignalLevel(wifiInfo.rssi)
                    } else {
                        WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
                    }
                signalLevel.accept(level)
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }


    fun setCurrentPosition(point: Point) {
        position.accept(point)
    }

    fun startScan() {
        disposables +=
            Observable.interval(SCAN_INTERVAL_MILLIS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val pos = position.value
                    val level = signalLevel.value

                    if (pos != null && level != null) {
                        scanned.add(
                            Signal(
                                point = pos,
                                level = level
                            )
                        )
                    }

                    lastScanSuccess = wifiManager.startScan()
                }
    }

    fun stopScan() {
        scanned.clear()
        disposables.dispose()
    }

    override fun dispose() {
        disposables.dispose()
    }

    override fun isDisposed(): Boolean = disposables.isDisposed

    private companion object {

        private const val SCAN_INTERVAL_MILLIS = 500L
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