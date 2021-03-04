package com.udacity

sealed class ButtonState {
    object NotActive : ButtonState()
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}