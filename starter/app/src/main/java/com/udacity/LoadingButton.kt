package com.udacity

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
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
private val changingLookStates =
    listOf(ButtonState.Active, ButtonState.Loading, ButtonState.Completed)

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
                lightButtonColor =
                    getColor(R.styleable.LoadingButton_lightButtonColor, Color.LTGRAY)
                darkButtonColor =
                    getColor(R.styleable.LoadingButton_darkButtonColor, Color.rgb(7, 194, 170))
                progressBarColor =
                    getColor(R.styleable.LoadingButton_progressBarColor, Color.rgb(0, 67, 73))
                progressCircleColor =
                    getColor(R.styleable.LoadingButton_progressCircleColor, Color.MAGENTA)
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

    private val buttonText = ButtonText(resources.getDimension(R.dimen.default_text_size))

    private val baseContent = ViewContent()

    private val progressBar = ProgressAnimation(this).apply {
        setColor(progressBarColor)
    }

    private val progressCircle = CircleAnimation(this).apply {
        setColor(progressCircleColor)
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
            baseContent.draw(this)
            if (buttonState == ButtonState.Loading) {
                progressBar.draw(this)
                progressCircle.draw(this)
                buttonText.draw(
                    this,
                    (widthSize / 2) - progressCircle.radius - SPACE / 2,
                    (heightSize / 2).toFloat()
                )
            } else {
                buttonText.draw(
                    this,
                    (widthSize / 2).toFloat(),
                    (heightSize / 2).toFloat()
                )
            }
        }
    }

    private fun refreshButton() {
        baseContent.reset(widthSize.toFloat(), heightSize.toFloat())
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
        baseContent.setColor(lightButtonColor)
        buttonText.setColor(darkTextColor)
        buttonText.text = context.getString(R.string.choose_download)
    }

    private fun refreshButtonAsActive() {
        baseContent.setColor(darkButtonColor)
        buttonText.setColor(darkTextColor)
        buttonText.text = context.getString(R.string.download)
    }

    private fun refreshButtonAsLoading() {
        baseContent.setColor(darkButtonColor)
        buttonText.setColor(lightTextColor)
        buttonText.text = context.getString(R.string.loading)

        progressBar.startAnimator(widthSize.toFloat())
        progressCircle.startAnimator(widthSize.toFloat(), buttonText.width)
    }

    private fun refreshButtonAsCompleted() {
        baseContent.setColor(lightButtonColor)
        buttonText.setColor(darkTextColor)
        buttonText.text = context.getString(R.string.download_completed)
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
            if (progressBar.isAnimator(animation)) {
                progressBar.reset(value, heightSize.toFloat())
            } else {
                progressCircle.reset(value)
            }
            invalidate()
        }
    }

    private abstract class Paintable {
        protected abstract val paint: Paint

        fun setColor(colorId: Int) {
            paint.color = colorId
        }
    }

    private open class ViewContent : Paintable() {
        protected val path = Path()
        final override val paint = Paint().apply {
            isAntiAlias = true
        }
        protected val rectF = RectF()

        fun draw(canvas: Canvas) = canvas.drawPath(path, paint)

        open fun reset(width: Float, height: Float) {
            path.apply {
                reset()
                rectF.set(0f, 0f, width, height)
                addRect(rectF, Path.Direction.CW)
            }
        }
    }

    private open class ProgressAnimation(listener: ValueAnimator.AnimatorUpdateListener) :
        ViewContent() {
        protected val animator: ValueAnimator = ValueAnimator().apply {
            duration = DURATION
            repeatCount = INFINITE
            addUpdateListener(listener)
        }

        fun isAnimator(valueAnimator: ValueAnimator) = valueAnimator == animator

        open fun startAnimator(buttonWidth: Float, textWidth: Float = 0f) {
            animator.apply {
                setFloatValues(0f, buttonWidth)
                start()
            }
        }
    }

    private class CircleAnimation(listener: ValueAnimator.AnimatorUpdateListener) :
        ProgressAnimation(listener) {
        var radius = 0f
        var positionX = 0f
            private set

        init {
            paint.style = Paint.Style.FILL
            animator.setFloatValues(0f, 360f)
        }

        fun reset(angle: Float) {
            rectF.set(positionX, radius, positionX + 2 * radius, 3 * radius)
            path.apply {
                reset()
                arcTo(rectF, -180f, angle)
                lineTo(positionX + radius, 2 * radius)
                lineTo(positionX, 2 * radius)
            }
        }

        override fun reset(width: Float, height: Float) {
            throw(UnsupportedOperationException())
        }

        override fun startAnimator(buttonWidth: Float, textWidth: Float) {
            positionX = (buttonWidth + textWidth + SPACE) / 2 - radius
            animator.start()
        }
    }

    private class ButtonText(textSize: Float) : Paintable() {
        lateinit var text: String

        override val paint = Paint().apply {
            this.textSize = textSize
            textAlign = Paint.Align.CENTER
        }

        val width
            get() = paint.measureText(text)

        private val textOffset = (paint.descent() + paint.ascent()) / 2

        fun draw(canvas: Canvas, x: Float, y: Float) =
            canvas.drawText(text, x, y - textOffset, paint)
    }
}