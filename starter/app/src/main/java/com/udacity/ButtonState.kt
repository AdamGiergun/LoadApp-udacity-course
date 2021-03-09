package com.udacity

sealed class ButtonState {
    object Inactive : ButtonState()
    object Active : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}