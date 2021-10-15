package com.andreibelous.wifimap.view

import androidx.appcompat.app.AppCompatActivity
import com.andreibelous.wifimap.*
import com.andreibelous.wifimap.data.Point
import com.andreibelous.wifimap.data.Signal
import com.andreibelous.wifimap.feature.Stage
import com.badoo.mvicore.modelWatcher
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

class MapView(
    private val root: AppCompatActivity,
    private val positionChangedListener: (Point) -> Unit,
    private val events: PublishRelay<Event> = PublishRelay.create()
) : Consumer<MapViewModel>, ObservableSource<MapView.Event> by events {

    private val fragment =
        root.supportFragmentManager.findFragmentById(R.id.ar_fragment)!!.cast<WIfiMapArFragment>()
    private val resultsView = root.findViewById<ResultsView>(R.id.results_view).apply {
        closeClickListener = { events.accept(Event.CloseResultsClicked) }
    }

    sealed interface Event {

        object CloseResultsClicked : Event
        object AnchorConfirmed : Event
        object PerimeterConfirmed : Event
        object AreaFinishConfirmed : Event
    }

    override fun accept(vm: MapViewModel) {
        modelWatcher(vm)
    }

    private val modelWatcher = modelWatcher<MapViewModel> {
        watch(MapViewModel::stage) { stage ->
            when (stage) {
                is Stage.Init -> init()
                is Stage.MeasuringPerimeter -> measuringPerimeter()
                is Stage.MeasuringArea -> measuringArea()
                is Stage.Finished -> results(stage.data)
            }
        }
    }

    private fun init() {
        fragment.locationListener = positionChangedListener
        fragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            fragment.setOnTapArPlaneListener(null)
        }
    }

    private fun measuringPerimeter() {
        resultsView.gone()
    }

    private fun measuringArea() {
        resultsView.gone()
    }

    private fun results(data: List<Signal>) {
        resultsView.visible()
        resultsView.bind(data)
    }

    fun execute(action: Action) {

    }

    sealed interface Action {

    }
}

data class MapViewModel(
    val stage: Stage
)