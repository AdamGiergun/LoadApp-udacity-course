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

    private val baseContent = ViewContent()

    private val textPaint = Paint().apply {
        textSize = defaultTextSize
    }

    private val progressBar =
        ViewContent(true, this).apply {
            setColor(context, R.color.colorPrimaryDark)
        }

    private val progressCircle =
        ViewContent(true, this).apply {
            paint.apply {
                style = Paint.Style.FILL
                color = Color.MAGENTA
                isAntiAlias = true
            }
            animator.apply { setFloatValues(0f, 360f) }
        }

    private var circlePositionX = 0f
    private var radius = 0f

    private var buttonState by Delegates.observable<ButtonState>(ButtonState.Inactive) { _, _, new ->
        if (new != ButtonState.Inactive) {
            refreshButton()
            invalidate()
        }
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
            drawPath(baseContent.path, baseContent.paint)
            if (buttonState == ButtonState.Loading) {
                drawPath(progressBar.path, progressBar.paint)
                drawPath(progressCircle.path, progressCircle.paint)
                myDrawText(-radius - SPACE / 2)
            } else {
                myDrawText(0f)
            }
        }
    }

    private fun refreshButton() {
        rect.set(0f, 0f, widthSize.toFloat(), heightSize.toFloat())
        baseContent.path.apply {
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
        baseContent.paint.color = Color.LTGRAY
        textPaint.apply {
            color = Color.BLACK
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.choose_download)
    }

    private fun refreshActiveButton() {
        baseContent.setColor(context, R.color.colorPrimary)
        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.download)
    }

    private fun refreshLoadingButton() {
        baseContent.setColor(context, R.color.colorPrimary)
        textPaint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }
        buttonText = context.getString(R.string.loading)

        val buttonTextWidth = textPaint.measureText(buttonText)
        circlePositionX = (widthSize + buttonTextWidth + SPACE) / 2 - radius

        progressBar.animator.run {
            setFloatValues(0f, widthSize.toFloat())
            start()
        }
        progressCircle.animator.start()
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
            val value = it.animatedValue as Float
            if (animation == progressBar.animator) {
                progressBar.path.apply {
                    reset()
                    rect.set(0f, 0f, value, heightSize.toFloat())
                    addRect(rect, Path.Direction.CW)
                }
            } else {
                rect.set(circlePositionX, radius, circlePositionX + 2 * radius, 3 * radius)
                progressCircle.path.apply {
                    reset()
                    arcTo(rect, -180f, value)
                    lineTo(circlePositionX + radius, 2 * radius)
                    lineTo(circlePositionX, 2 * radius)
                }
            }
            invalidate()
        }
    }

    private class ViewContent(
        isAnimated: Boolean = false,
        listener: ValueAnimator.AnimatorUpdateListener? = null
    ) {
        val path = Path()
        val paint = Paint().apply {
            isAntiAlias = true
        }

        lateinit var animator: ValueAnimator

        init {
            if (isAnimated) animator = ValueAnimator().apply {
                duration = DURATION
                repeatCount = INFINITE
                addUpdateListener(listener)
            }
        }

        fun setColor(context: Context, colorId: Int) {
            paint.color = ContextCompat.getColor(context, colorId)
        }
    }
}