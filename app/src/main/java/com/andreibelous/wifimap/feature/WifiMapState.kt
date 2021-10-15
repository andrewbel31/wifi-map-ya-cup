package com.andreibelous.wifimap.feature

import com.andreibelous.wifimap.data.Signal

data class WifiMapState(
    val stage: Stage = Stage.Init
)

sealed interface Stage {

    object Init : Stage
    object MeasuringPerimeter : Stage
    object MeasuringArea : Stage
    data class Finished(val data: List<Signal>) : Stage
}