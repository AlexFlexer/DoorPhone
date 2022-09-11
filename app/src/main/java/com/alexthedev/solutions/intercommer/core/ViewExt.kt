package com.alexthedev.solutions.intercommer.core

import android.view.View

fun View.onClick(callback: () -> Unit) {
    setOnClickListener { callback() }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}