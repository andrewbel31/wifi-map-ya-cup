package com.andreibelous.wifimap.mapper

import com.andreibelous.wifimap.view.MapViewModel
import com.andreibelous.wifimap.feature.WifiMapState

object StateToViewModel : (WifiMapState) -> MapViewModel {

    override fun invoke(state: WifiMapState): MapViewModel =
        MapViewModel(stage = state.stage)
}