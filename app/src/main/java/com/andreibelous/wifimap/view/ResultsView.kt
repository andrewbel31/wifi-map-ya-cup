package com.andreibelous.wifimap.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.andreibelous.wifimap.R
import com.andreibelous.wifimap.data.Signal

class ResultsView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.layout_results_view, this)
        setBackgroundColor(Color.WHITE)
    }

    var closeClickListener: (() -> Unit)? = null

    private val closeButton = findViewById<View>(R.id.button_close).apply {
        setOnClickListener { closeClickListener?.invoke() }
        background = RippleDrawable(
            ColorStateList.valueOf(Color.GRAY),
            null,
            ShapeDrawable(OvalShape())
        )
    }

    private val heatMap = findViewById<HeatMap>(R.id.heat_map)

    fun bind(data: List<Signal>) {
        heatMap.bind(data)
    }
}