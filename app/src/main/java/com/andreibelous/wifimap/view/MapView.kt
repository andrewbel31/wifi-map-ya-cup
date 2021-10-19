package com.andreibelous.wifimap.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andreibelous.wifimap.*
import com.andreibelous.wifimap.data.Point
import com.andreibelous.wifimap.data.Signal
import com.andreibelous.wifimap.feature.Stage
import com.badoo.mvicore.modelWatcher
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.ObservableSource
import io.reactivex.functions.Consumer

private typealias SceneformColor = com.google.ar.sceneform.rendering.Color

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
    private var anchorNode: AnchorNode? = null
    private var renderable: Renderable? = null
    private var child: Node? = null

    private val labelAction = root.findViewById<TextView>(R.id.label_next_action).apply {
        setTextColor(Color.YELLOW)
    }

    private val buttonDone = root.findViewById<TextView>(R.id.button_done).apply {
        val radii = context.dp(24f)
        val stroke = context.dp(2f)
        val radiiArr = floatArrayOf(radii, radii, radii, radii, radii, radii, radii, radii)
        setTextColor(Color.YELLOW)
        background =
            RippleDrawable(
                ColorStateList.valueOf(Color.YELLOW),
                GradientDrawable().apply {
                    setStroke(stroke.toInt(), Color.YELLOW)
                    cornerRadii = radiiArr
                },
                ShapeDrawable(RoundRectShape(radiiArr, null, null))
            )
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
        buttonDone.gone()
        resultsView.gone()
        labelAction.visible()
        labelAction.text = "Тапом выберите центр комнаты"
        fragment.locationListener = positionChangedListener

        if (renderable != null) {
            onRenderableReady(renderable!!)
        } else {
            MaterialFactory.makeOpaqueWithColor(root, SceneformColor(Color.GREEN))
                .thenApply { ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.3f, 0.0f), it) }
                .thenAccept { onRenderableReady(it) }
        }
    }

    private fun onRenderableReady(renderable: Renderable) {
        this.renderable = renderable
        child?.let { anchorNode?.removeChild(it) }

        fragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            val anchor = hitResult.createAnchor()

            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(fragment.arSceneView.scene)
            anchorNode?.worldPosition = Vector3(0f, 0f, 0f)

            val node = Node().also { child = it }
            node.setParent(anchorNode)
            node.renderable = renderable

            fragment.setOnTapArPlaneListener(null)
            events.accept(Event.AnchorConfirmed)
        }
    }

    private fun measuringPerimeter() {
        resultsView.gone()
        labelAction.visible()
        labelAction.text = "Обычным шагом обойдите комнату по периметру стараясь видеть зеленый шар"
        with(buttonDone) {
            visible()
            text = "Сделано"
            setOnClickListener { events.accept(Event.PerimeterConfirmed) }
        }
    }

    private fun measuringArea() {
        resultsView.gone()
        labelAction.visible()
        labelAction.text = "Обычным шагом походите по площади комнаты стараясь видеть зеленый шар"

        with(buttonDone) {
            visible()
            text = "Сделано"
            setOnClickListener { events.accept(Event.AreaFinishConfirmed) }
        }
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