package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private val defaultTextSize = resources.getDimension(R.dimen.default_text_size)


    private val rect = Rect()
    private val rectPaint = Paint()
    private val textPaint = Paint()

    private val valueAnimator = ValueAnimator()

    private var buttonState by Delegates.observable<ButtonState>(ButtonState.NotActive) { p, old, new ->
        invalidate()
    }

    fun changeState(newButtonState: ButtonState) {
        buttonState = newButtonState
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            when (buttonState) {
                ButtonState.NotActive -> drawNotActiveButton(it)
                else -> {}
            }
        }
    }

    private fun drawNotActiveButton(canvas: Canvas) {
        rect.set(0, 0, widthSize, heightSize)
        rectPaint.apply {
            color = Color.LTGRAY
            isAntiAlias = true

        }
        canvas.drawRect(rect, rectPaint)

        textPaint.apply {
            color = Color.BLACK
            textSize = defaultTextSize
            textAlign = Paint.Align.CENTER
        }
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(
            context.getString(R.string.choose_download),
            (widthSize / 2).toFloat(),
            ((heightSize / 2) - textOffset),
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}