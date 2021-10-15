package com.andreibelous.wifimap.mapper

import com.andreibelous.wifimap.view.MapView
import com.andreibelous.wifimap.feature.WifiMapFeature

object NewsToViewAction : (WifiMapFeature.News) -> MapView.Action? {

    override fun invoke(state: WifiMapFeature.News): MapView.Action? = null
}