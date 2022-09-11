@file:Suppress("Recycle", "Unused")
package com.alexthedev.solutions.intercommer.core

import android.animation.ValueAnimator
import android.view.View
import androidx.core.view.isVisible

/**
 * Animates rotation of the given [View] from [startDegrees] to [endDegrees]
 * during the [duration] and optionally executes [actionOnEnd], if it is not null.
 */
fun View.animateRotation(startDegrees: Float, endDegrees: Float, duration: Long = 300, actionOnEnd: (() -> Unit)? = null) {
    animateWithValueAnimator(startDegrees, endDegrees, duration, {
        this.rotation = it
    }, actionOnEnd)
}

/**
 * Animates alpha state changes of the given view with help of [ValueAnimator].
 *
 * @param startAlpha is the alpha we start animate from.
 * @param endAlpha is the alpha we end up with.
 * @param duration answers the question "How fast should the alpha state change?".
 * @param shouldBeVisibleAtTheEnd determines whether the given view should be VISIBLE
 *      or GONE at the end of the animation.
 */
fun View.animateAlphaChange(
    startAlpha: Float,
    endAlpha: Float,
    duration: Long,
    shouldBeVisibleAtTheEnd: Boolean
) {
    ValueAnimator.ofFloat(startAlpha, endAlpha)
        .completeAndStart(endAlpha, duration, { this@animateAlphaChange.alpha = it }) {
            this@animateAlphaChange.isVisible = shouldBeVisibleAtTheEnd
        }
}

/**
 * Animates the alpha state changes of the given view and then invokes
 * the given [action].
 */
fun View.animateAlphaWithAction(
    startAlpha: Float,
    endAlpha: Float,
    duration: Long,
    action: (v: View) -> Unit
) {
    animateAlphaChange(startAlpha, endAlpha, duration, true)
    action(this)
}

/**
 * Creates a [ValueAnimator] for [startValue] and [endValue], sets [duration] to it
 * and invokes [actionOnUpdate] every time the value is measured.
 *
 * This function animates [Int] values.
 */
fun animateWithValueAnimator(
    startValue: Int,
    endValue: Int,
    duration: Long,
    actionOnUpdate: (measuredValue: Int) -> Unit,
    actionOnEnd: (() -> Unit)? = null
) {
    ValueAnimator.ofInt(startValue, endValue)
        .completeAndStart(endValue, duration, actionOnUpdate, actionOnEnd)
}

/**
 * Creates a [ValueAnimator] for [startValue] and [endValue], sets [duration] to it
 * and invokes [actionOnUpdate] every time the value is measured.
 *
 * This function animates [Float] values.
 */
fun animateWithValueAnimator(
    startValue: Float,
    endValue: Float,
    duration: Long,
    actionOnUpdate: (measuredValue: Float) -> Unit,
    actionOnEnd: (() -> Unit)? = null
) {
    ValueAnimator.ofFloat(startValue, endValue)
        .completeAndStart(endValue, duration, actionOnUpdate, actionOnEnd)
}

/**
 * Sets duration and action on update for this [ValueAnimator] and starts it.
 */
@Suppress("UNCHECKED_CAST")
private fun <T> ValueAnimator.completeAndStart(
    endValue: T,
    duration: Long,
    actionOnUpdate: (measuredValue: T) -> Unit,
    actionOnEnd: (() -> Unit)? = null
) {
    this.apply {
        this.duration = duration
        addUpdateListener {
            val value = animatedValue as T
            actionOnUpdate(value)
            if (value == endValue) actionOnEnd?.invoke()
        }
        start()
    }
}