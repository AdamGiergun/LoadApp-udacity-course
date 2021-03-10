package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

private const val SPACE = 50f
private const val DURATION = 2000L

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
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        isAntiAlias = true
    }
    private val barAnimator = valueAnimator

    private val circlePath = Path()
    private val circleRect = RectF()
    private val circlePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.MAGENTA
        isAntiAlias = true
    }
    private var circlePositionX = 0f
    private var radius = 0f
    private val circleAnimator = valueAnimator.apply { setFloatValues(0f, 360f) }

    private var buttonState by Delegates.observable<ButtonState>(ButtonState.Inactive) { _, _, new ->
        if (new != ButtonState.Inactive) {
            refreshButton()
            invalidate()
        }
    }

    private val valueAnimator
        get() = ValueAnimator().apply {
            duration = DURATION
            repeatCount = INFINITE
            addUpdateListener(this@LoadingButton)
        }

    init {
        addRippleEffectOnClick()
    }

    private fun addRippleEffectOnClick() {
        isClickable = true
        isFocusable = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless,
                outValue,
                true
            )
            foreground = ResourcesCompat.getDrawable(resources, outValue.resourceId, context.theme)
        }
    }

    fun setState(newButtonState: ButtonState) {
        buttonState = newButtonState
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            drawPath(rectPath, rectPaint)
            if (buttonState == ButtonState.Loading) {
                drawPath(animatedPath, animatedPaint)
                drawPath(circlePath, circlePaint)
                myDrawText(-radius - SPACE / 2)
            } else {
                myDrawText(0f)
            }
        }
    }

    private fun refreshButton() {
        rect.set(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        rectPath.apply {
            reset()
            addRect(rect, Path.Direction.CW)
        }
        when (buttonState) {
            ButtonState.Inactive -> refreshInactiveButton()
            ButtonState.Active -> refreshActiveButton()
            ButtonState.Loading -> refreshLoadingButton()
            else -> {
            }
        }
    }

    private fun refreshInactiveButton() {
        rectPaint.color = Color.LTGRAY
        textPaint.apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.choose_download)
    }

    private fun refreshActiveButton() {
        rectPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.download)
    }

    private fun refreshLoadingButton() {
        rectPaint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.loading)

        val buttonTextWidth = textPaint.measureText(buttonText)
        circlePositionX = (widthSize + buttonTextWidth + SPACE) / 2 - radius

        barAnimator.run {
            setFloatValues(0f, widthSize.toFloat())
            start()
        }
        circleAnimator.start()
    }

    private fun Canvas.myDrawText(textShift: Float) {
        val textOffset = (textPaint.descent() + textPaint.ascent()) / 2
        drawText(
            buttonText,
            (widthSize / 2).toFloat() + textShift,
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
        radius = (heightSize / 4).toFloat()
        setMeasuredDimension(w, h)
        refreshButton()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        animation?.let {
            if (animation == barAnimator) {
                val value = it.animatedValue as Float
                animatedPath.apply {
                    reset()
                    addRect(0f, 0f, value, heightSize.toFloat(), Path.Direction.CW)
                    invalidate()
                }
            } else {
                val value = it.animatedValue as Float
                circleRect.set(circlePositionX, radius, circlePositionX + 2 * radius, 3 * radius)
                circlePath.apply {
                    reset()
                    arcTo(circleRect, -180f, value)
                    lineTo(circlePositionX + radius, 2 * radius)
                    lineTo(circlePositionX, 2 * radius)
                    invalidate()
                }
            }
        }
    }
}