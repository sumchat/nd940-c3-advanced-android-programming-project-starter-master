package com.udacity

import android.R.attr
import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates
import android.R.attr.radius
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE

import android.graphics.RectF




class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0f
    private var heightSize = 0f

    private var valueAnimator = ValueAnimator()
    private var bgColor: Int = Color.BLACK
    private var textColor: Int = Color.BLACK
    // tells the compiler that the value of a variable
    // must never be cached as its value may change outside
    @Volatile
    private var progress:Double = 0.0
    private var currentProgress = 0

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                animateProgress()
                //animateColorChange()
               // animateLoadingCircle()
                invalidate()

            }
            ButtonState.Completed -> {
                completeAnimations()
                invalidate()
            }
            else -> invalidate()
        }

    }


    init {
        isClickable = true
       /* valueAnimator = AnimatorInflater.loadAnimator(
            context,
            // properties for downloading progress is defined
            R.animator.loading_animation
        ) as ValueAnimator*/

        //valueAnimator.addUpdateListener(updateListener)

        // initialize custom attributes of the button
        val attr = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        )
        try {

            // button back-ground color
            bgColor = attr.getColor(
                R.styleable.LoadingButton_bgColor,
                ContextCompat.getColor(context, R.color.purple_200)
            )

            // button text color
            textColor = attr.getColor(
                R.styleable.LoadingButton_textColor,
                ContextCompat.getColor(context, R.color.white)
            )
        } finally {
            // clearing all the data associated with attribute
            attr.recycle()
        }

    }
    // set attributes of paint
    // Set up the paint with which to draw.
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER // button text alignment
        textSize = 55.0f //button text size
        typeface = Typeface.create("", Typeface.BOLD) // button text's font style
    }
    private val loadingRectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.colorPrimaryDark)
    }

    private val fillArcPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.BLUE
        strokeWidth = 40f
        // 1
        strokeCap = Paint.Cap.ROUND
    }

    fun setNewButtonState(newButtonState: ButtonState) {
        buttonState = newButtonState

    }
    private fun completeAnimations() {
       // loadingCircleAnimator.end()
        valueAnimator.cancel()
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.strokeWidth = 0f
        paint.color = bgColor
        // draw custom button
        if (canvas != null) {
            //background rectangle
            canvas.drawRect(0f, 0f, (width).toFloat(), height.toFloat(), paint)
        }

        // to show rectangular progress on custom button while file is downloading
        if (buttonState == ButtonState.Loading) {
            paint.color = Color.parseColor("#004349")
            if (canvas != null) {
                canvas.drawRect(
                    0f, 0f,
                    ((width) * (progress / 100)).toFloat(), height.toFloat(), loadingRectPaint
                )
                paint.color = Color.YELLOW
                paint.strokeWidth = 40f

                val oval = RectF(widthSize - 100f, heightSize/3  ,
                    (widthSize - 100f + 40f).toFloat(), heightSize/3 + 40f)

                canvas.drawArc(oval, (0).toFloat(), (360).toFloat(), true, paint)
                canvas.drawArc(oval, 270f, (360 * progress / 100).toFloat(), true, fillArcPaint)


            }
        }
        // check the button state
        val buttonText = if (buttonState == ButtonState.Loading)
            resources.getString(R.string.button_loading)  // We are loading as button text
        else resources.getString(R.string.button_download)// download as button text

        // write the text on custom button
        paint.color = textColor
        if (canvas != null) {
            canvas.drawText(buttonText, (width / 2).toFloat(), ((height + 30) / 2).toFloat(), paint)
        }


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w.toFloat()
        heightSize = h.toFloat()
        setMeasuredDimension(w, h)
    }

   /* override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Loading
         animateProgress()

        return true
    }*/
    fun animateProgress() {
        val valuesHolder = PropertyValuesHolder.ofFloat("progress", 0f, 100f)
        val animator = ValueAnimator().apply {
            setValues(valuesHolder)
            duration = 3000
            repeatMode = REVERSE
            repeatCount = INFINITE
            addUpdateListener {
                val percentage = it.getAnimatedValue(PERCENTAGE_VALUE_HOLDER) as Float
                progress = percentage.toDouble()
                invalidate()
            }
        }

        animator.start()
    }

    private fun drawText(canvas: Canvas) {
        val buttonText = when (buttonState) {
            ButtonState.Clicked -> context.getString(R.string.button_download)
            ButtonState.Loading -> context.getString(R.string.button_loading)
            ButtonState.Completed -> context.getString(R.string.button_download)
            else -> throw AssertionError()
        }

       // textPaint.color = buttonTextColor
        canvas.drawText(buttonText, widthSize / 2f, heightSize / 1.7f, paint)
    }

    companion object {
        const val ARC_FULL_ROTATION_DEGREE = 360
        const val PERCENTAGE_DIVIDER = 100.0
        const val PERCENTAGE_VALUE_HOLDER = "progress"
        private const val LOADING_CIRCLE_SIZE = 60
    }

}