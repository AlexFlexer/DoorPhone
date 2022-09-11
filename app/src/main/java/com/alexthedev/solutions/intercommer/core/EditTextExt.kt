package com.alexthedev.solutions.intercommer.core

import android.text.InputFilter
import android.widget.EditText

fun EditText.getMaxLength(): Int {
    filters.forEach {
        if (it is InputFilter.LengthFilter) {
            return it.max
        }
    }
    return 0
}

fun EditText.getContent(): String {
    return this.editableText.toString()
}