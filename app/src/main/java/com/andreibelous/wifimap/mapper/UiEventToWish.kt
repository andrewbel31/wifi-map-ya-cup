package com.andreibelous.wifimap.mapper

import com.andreibelous.wifimap.feature.WifiMapFeature.Wish
import com.andreibelous.wifimap.view.MapView.Event

object UiEventToWish : (Event) -> Wish? {

    override fun invoke(event: Event): Wish? =
        when (event) {
            is Event.CloseResultsClicked -> Wish.Init
            is Event.AnchorConfirmed -> Wish.StartMeasuringPerimeter
            is Event.PerimeterConfirmed -> Wish.StartMeasuringArea
            is Event.AreaFinishConfirmed -> Wish.FinishMeasuringArea
        }
}