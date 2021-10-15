package com.andreibelous.wifimap.feature

import com.andreibelous.wifimap.data.Signal
import com.andreibelous.wifimap.data.WifiSignalDataSource
import com.andreibelous.wifimap.feature.WifiMapFeature.News
import com.andreibelous.wifimap.feature.WifiMapFeature.Wish
import com.andreibelous.wifimap.toObservable
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.badoo.mvicore.feature.Feature
import io.reactivex.Observable

class WifiMapFeature(
    private val dataSource: WifiSignalDataSource
) : Feature<Wish, WifiMapState, News> by BaseFeature(
    initialState = WifiMapState(),
    wishToAction = Action::ExecuteWish,
    actor = ActorImpl(dataSource),
    reducer = ReducerImpl
) {


    sealed interface Wish {

        object StartMeasuringPerimeter : Wish
        object StartMeasuringArea : Wish
        object FinishMeasuringArea : Wish
        object Init : Wish
    }

    sealed interface News {

        data class ErrorHappened(val throwable: Throwable) : News
    }

    private sealed interface Effect {

        data class StageChanged(val newStage: Stage) : Effect
    }

    private sealed interface Action {

        data class ExecuteWish(val wish: Wish) : Action
    }

    private class ActorImpl(
        private val dataSource: WifiSignalDataSource
    ) : Actor<WifiMapState, Action, Effect> {

        override fun invoke(state: WifiMapState, action: Action): Observable<out Effect> =
            when (action) {
                is Action.ExecuteWish -> executeWish(state, action.wish)
            }

        private fun executeWish(state: WifiMapState, wish: Wish): Observable<out Effect> =
            when (wish) {
                is Wish.StartMeasuringPerimeter -> {
                    dataSource.stopScan()
                    dataSource.startScan()
                    Effect.StageChanged(Stage.MeasuringPerimeter).toObservable()
                }

                is Wish.StartMeasuringArea ->
                    Effect.StageChanged(Stage.MeasuringArea).toObservable()

                is Wish.FinishMeasuringArea -> {
                    val copy = mutableListOf<Signal>()
                    copy.addAll(dataSource.scanned)
                    dataSource.stopScan()
                    Effect.StageChanged(Stage.Finished(copy)).toObservable()
                }

                is Wish.Init -> Effect.StageChanged(Stage.Init).toObservable()
            }
    }

    private object ReducerImpl : Reducer<WifiMapState, Effect> {

        override fun invoke(state: WifiMapState, effect: Effect): WifiMapState =
            when (effect) {
                is Effect.StageChanged -> state.copy(stage = effect.newStage)
            }
    }
}