package com.alexthedev.solutions.intercommer.ui

import android.animation.ValueAnimator
import android.view.View

fun setupRotateAnimatorIn(viewToRotate: View): ValueAnimator {
    return setupAnimatorInternal(viewToRotate, 0f, 180f)
}

fun setupRotateAnimatorOut(viewToRotate: View): ValueAnimator {
    return setupAnimatorInternal(viewToRotate, 180f, 360f)
}

private fun setupAnimatorInternal(
    viewToRotate: View,
    startValue: Float,
    endValue: Float,
    duration: Long = 200
): ValueAnimator {
    return ValueAnimator.ofFloat(startValue, endValue).apply {
        this.duration = duration
        addUpdateListener {
            viewToRotate.rotation = it.animatedValue as Float
        }
    }
}