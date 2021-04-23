package com.udacity.loadapp.button

sealed class ButtonState {
    object Inactive : ButtonState()
    object Active : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}