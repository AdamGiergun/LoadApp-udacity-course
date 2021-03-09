package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    ValueAnimator.AnimatorUpdateListener {

    private var widthSize = 0
    private var heightSize = 0
    private val defaultTextSize = resources.getDimension(R.dimen.default_text_size)
    private lateinit var buttonText: String

    private val rect = RectF()
    private val rectPaint = Paint().apply {
        isAntiAlias = true
    }
    private val rectPath = Path()

    private val textPaint = Paint().apply {
        textSize = defaultTextSize
    }

    private val animatedPath = Path()
    private val animatedPaint = Paint().apply {
        color = context.getColor(R.color.colorPrimaryDark)
        isAntiAlias = true
    }
    private val valueAnimator = ValueAnimator()

    private var buttonState by Delegates.observable<ButtonState>(ButtonState.Inactive) { _, _, new ->
        if (new != ButtonState.Inactive) {
            refresh()
            invalidate()
        }
    }

    fun setState(newButtonState: ButtonState) {
        buttonState = newButtonState
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            drawPath(rectPath, rectPaint)
            if (buttonState == ButtonState.Loading) drawPath(animatedPath, animatedPaint)
            myDrawText()
        }
    }

    private fun refreshInactiveButton() {
        rect.set(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        rectPath.apply {
            reset()
            addRect(rect, Path.Direction.CW)
        }
        rectPaint.color = Color.LTGRAY

        textPaint.apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.choose_download)
    }

    private fun refresh() {
        when (buttonState) {
            ButtonState.Inactive -> refreshInactiveButton()
            ButtonState.Active -> refreshActiveButton()
            ButtonState.Loading -> refreshLoadingButton()
            else -> {
            }
        }
    }

    private fun refreshActiveButton() {
        rect.set(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        rectPath.apply {
            reset()
            addRect(rect, Path.Direction.CW)
        }
        rectPaint.color = context.getColor(R.color.colorPrimary)

        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.download)
    }

    private fun refreshLoadingButton() {
        valueAnimator.apply {
            setFloatValues(0f, 1000f)
            duration = 1500
            addUpdateListener(this@LoadingButton)
            start()
            repeatCount = INFINITE
        }
        buttonText = context.getString(R.string.loading)
    }

    private fun Canvas.myDrawText() {
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2
        drawText(
            buttonText,
            (widthSize / 2).toFloat(),
            ((heightSize / 2) - textOffset),
            textPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
        refresh()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        animation?.let {
            val value = (it.animatedValue as Float)
            animatedPath.apply {
                reset()
                addRect(0f, 0f, value, heightSize.toFloat(), Path.Direction.CW)
                invalidate()
            }
        }
    }
}