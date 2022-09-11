package com.alexthedev.solutions.intercommer.core

import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.widget.TextView

/**
 * Underlines the content of the given TextView.
 */
fun TextView.underline() {
    val content = SpannableString(this.text)
    content.setSpan(UnderlineSpan(), 0, content.length, 0)
    this.text = content
}