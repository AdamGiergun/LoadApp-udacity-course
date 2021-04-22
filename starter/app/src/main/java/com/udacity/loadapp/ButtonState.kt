package com.udacity.loadapp

sealed class ButtonState {
    object Inactive : ButtonState()
    object Active : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}