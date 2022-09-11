package com.alexthedev.solutions.intercommer.ui

import android.annotation.SuppressLint
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class StandardBsCallback(
    private val arrowOfBs: View
) : BottomSheetBehavior.BottomSheetCallback() {

    @SuppressLint("SwitchIntDef")
    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_COLLAPSED ->
                setupRotateAnimatorOut(arrowOfBs).start()
            BottomSheetBehavior.STATE_EXPANDED ->
                setupRotateAnimatorIn(arrowOfBs).start()
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
}