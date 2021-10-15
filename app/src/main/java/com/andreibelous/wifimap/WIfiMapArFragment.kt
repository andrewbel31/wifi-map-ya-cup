package com.andreibelous.wifimap

import com.andreibelous.wifimap.data.Point
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment

class WIfiMapArFragment : ArFragment() {

    var locationListener: ((Point) -> Unit)? = null

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        if (locationListener != null) {
            val pos = arSceneView.scene.camera.worldPosition
            val point = Point(pos.x, pos.y)
            locationListener?.invoke(point)
        }
    }
}