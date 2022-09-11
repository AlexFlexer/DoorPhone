package com.alexthedev.solutions.intercommer.core

fun String.isBinary(): Boolean {
    if (this.isBlank()) return false
    for (c in this) {
        if (c != '0' && c != '1')
            return false
    }
    return true
}

fun String.toBooleanArray(): Array<Boolean> {
    return Array(this.length) {
        this[it] != '0'
    }
}