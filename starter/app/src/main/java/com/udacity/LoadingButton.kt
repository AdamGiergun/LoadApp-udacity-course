package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import kotlin.properties.Delegates

private const val SPACE = 50f
private const val DURATION = 2000L
private val changingLookStates = listOf(ButtonState.Active, ButtonState.Loading, ButtonState.Completed)

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
    ValueAnimator.AnimatorUpdateListener {

    private val lightTextColor: Int
    private val darkTextColor: Int
    private val lightButtonColor: Int
    private val darkButtonColor: Int
    private val progressBarColor: Int
    private val progressCircleColor: Int

    init {
        isClickable = true
        isFocusable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addRippleEffectOnClick()
        }

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        ).apply {
            try {
                lightTextColor = getColor(R.styleable.LoadingButton_lightTextColor, Color.WHITE)
                darkTextColor = getColor(R.styleable.LoadingButton_darkTextColor, Color.BLACK)
                lightButtonColor = getColor(R.styleable.LoadingButton_lightButtonColor, Color.LTGRAY)
                darkButtonColor = getColor(R.styleable.LoadingButton_darkButtonColor, Color.rgb(7, 194, 170))
                progressBarColor = getColor(R.styleable.LoadingButton_progressBarColor, Color.rgb(0,67,73))
                progressCircleColor = getColor(R.styleable.LoadingButton_progressCircleColor, Color.MAGENTA)
            } finally {
                recycle()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addRippleEffectOnClick() {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            outValue,
            true
        )
        foreground = ResourcesCompat.getDrawable(resources, outValue.resourceId, context.theme)
    }
    
    private var widthSize = 0
    private var heightSize = 0
    private val buttonText = ButtonText(resources)

    private val rect = RectF()

    private val baseContent = ViewContent()

    private val progressBar =
        ProgressAnimation(this).apply {
            setColor(progressBarColor)
        }

    private val progressCircle =
        CircleAnimation(this).apply {
            paint.apply {
                style = Paint.Style.FILL
                color = progressCircleColor
                isAntiAlias = true
            }
            animator.setFloatValues(0f, 360f)
        }

    private var buttonState by Delegates.observable<ButtonState>(ButtonState.Inactive) { _, _, new ->
        if (new in changingLookStates) {
            refreshButton()
            invalidate()
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
                myDrawText(-progressCircle.radius - SPACE / 2)
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
            ButtonState.Inactive -> refreshButtonAsInactive()
            ButtonState.Active -> refreshButtonAsActive()
            ButtonState.Loading -> refreshButtonAsLoading()
            ButtonState.Completed -> refreshButtonAsCompleted()
            else -> {
            }
        }
    }

    private fun refreshButtonAsInactive() {
        baseContent.paint.color = lightButtonColor
        buttonText.paint.apply {
            color = darkTextColor
            textAlign = Paint.Align.CENTER
        }
        buttonText.value = context.getString(R.string.choose_download)
    }

    private fun refreshButtonAsActive() {
        baseContent.setColor(darkButtonColor)
        buttonText.paint.apply {
            color = darkTextColor
            textAlign = Paint.Align.CENTER
        }
        buttonText.value = context.getString(R.string.download)
    }

    private fun refreshButtonAsLoading() {
        baseContent.setColor(darkButtonColor)
        buttonText.paint.apply {
            color = lightTextColor
            textAlign = Paint.Align.CENTER
        }
        buttonText.value = context.getString(R.string.loading)

        progressCircle.setPositionX(widthSize.toFloat(), buttonText.width)

        progressBar.animator.run {
            setFloatValues(0f, widthSize.toFloat())
            start()
        }
        progressCircle.animator.start()
    }

    private fun refreshButtonAsCompleted() {
        baseContent.paint.color = lightButtonColor
        buttonText.paint.apply {
            color = darkTextColor
            textAlign = Paint.Align.CENTER
        }
        buttonText.value = context.getString(R.string.download_completed)
    }

    private fun Canvas.myDrawText(textShift: Float) {
        val textOffset = (buttonText.paint.descent() + buttonText.paint.ascent()) / 2
        drawText(
            buttonText.value,
            (widthSize / 2).toFloat() + textShift,
            ((heightSize / 2) - textOffset),
            buttonText.paint
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
        progressCircle.radius = (heightSize / 4).toFloat()
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
                rect.set(
                    progressCircle.positionX,
                    progressCircle.radius,
                    progressCircle.positionX + 2 * progressCircle.radius,
                    3 * progressCircle.radius
                )
                progressCircle.path.apply {
                    reset()
                    arcTo(rect, -180f, value)
                    lineTo(
                        progressCircle.positionX + progressCircle.radius,
                        2 * progressCircle.radius
                    )
                    lineTo(progressCircle.positionX, 2 * progressCircle.radius)
                }
            }
            invalidate()
        }
    }

    private open class ViewContent {
        val path = Path()
        val paint = Paint().apply {
            isAntiAlias = true
        }

        fun setColor(colorId: Int) {
            paint.color = colorId
        }
    }

    private open class ProgressAnimation(listener: ValueAnimator.AnimatorUpdateListener) :
        ViewContent() {
        val animator: ValueAnimator = ValueAnimator().apply {
            duration = DURATION
            repeatCount = INFINITE
            addUpdateListener(listener)
        }
    }

    private class CircleAnimation(listener: ValueAnimator.AnimatorUpdateListener) :
        ProgressAnimation(listener) {
        var radius = 0f
        var positionX = 0f
            private set

        fun setPositionX(widthSize: Float, textWidth: Float) {
            positionX = (widthSize + textWidth + SPACE) / 2 - radius
        }
    }

    private class ButtonText(resources: Resources) {
        lateinit var value: String

        val paint = Paint().apply {
            textSize = resources.getDimension(R.dimen.default_text_size)
        }

        val width
            get() = paint.measureText(value)
    }
}