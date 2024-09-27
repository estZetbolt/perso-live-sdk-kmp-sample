package com.example.androidapp

import android.app.Activity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class KeyboardHeightProviderImpl(
    activity: Activity
) {

    private val _keyboardHeight = MutableStateFlow(0)
    val keyboardHeight: StateFlow<Int> = _keyboardHeight

    init {
        ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom -
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            _keyboardHeight.value = imeInsets.coerceAtLeast(0)
            insets
        }
    }
}