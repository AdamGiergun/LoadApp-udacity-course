package com.udacity

import androidx.databinding.BindingAdapter

@BindingAdapter("buttonState")
fun LoadingButton.setButtonState(buttonState: ButtonState) {
    setState(buttonState)
}
