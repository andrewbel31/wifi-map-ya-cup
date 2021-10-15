package com.andreibelous.wifimap

import android.content.Context
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlin.math.ceil

inline fun <reified T> Any.cast() = this as T

fun Lifecycle.subscribe(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null
) {
    addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            onCreate?.invoke()
        }

        override fun onStart(owner: LifecycleOwner) {
            onStart?.invoke()
        }

        override fun onResume(owner: LifecycleOwner) {
            onResume?.invoke()
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause?.invoke()
        }

        override fun onStop(owner: LifecycleOwner) {
            onStop?.invoke()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            onDestroy?.invoke()
        }
    })
}

inline fun <reified T> T.toObservable() =
    if (this == null) Observable.empty<T>() else Observable.just(this)

inline fun <reified T> ((T) -> Unit).asConsumer(): Consumer<T> =
    Consumer { t -> this@asConsumer.invoke(t) }

fun Context.dp(value: Float): Float {
    return if (value == 0f) {
        0f
    } else ceil(resources.displayMetrics.density * value)
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}