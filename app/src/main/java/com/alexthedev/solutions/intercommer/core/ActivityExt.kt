package com.alexthedev.solutions.intercommer.core

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setCustomToolbar(toolbar: Toolbar, @LayoutRes layout: Int) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        customView = LayoutInflater.from(this@setCustomToolbar)
            .inflate(layout, toolbar, false)
    }
}

fun Activity.hideKeyboard() {
    val imm: InputMethodManager =
        this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view: View? = this.currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}