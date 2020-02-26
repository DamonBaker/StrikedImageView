package xyz.damonbaker.strikedimageview

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.LightingColorFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator


class StrikedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StrikedImageView, 0, 0).apply {
            strikeColor = getColor(R.styleable.StrikedImageView_strikeColor, 0xFFF)
            strikeAlpha = getFloat(R.styleable.StrikedImageView_strikeAlpha, 0.33f)

            recycle()
        }
    }

    var strikeColor: Int
    var strikeAlpha: Float

    var isStriked: Boolean = true
        set(value) {
            field = value
            strikeWithAnimation(value)
        }

    val contentDrawable: Drawable
        get() = (drawable as LayerDrawable).getDrawable(0)

    private val strikeDrawable: AnimatedVectorDrawable
        get() = ((drawable as LayerDrawable).getDrawable(1) as AnimatedVectorDrawable)

    private val hexAlpha: Int
        get() = (strikeAlpha * 255).toInt()

    private fun strikeWithAnimation(striked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applyStrikeAnimation()
        } else {
            setImageDrawable(drawable)
        }

        val tintColor = ImageViewCompat.getImageTintList(this)?.defaultColor ?: 0xFFFFFFFF.toInt()
        drawable?.colorFilter = LightingColorFilter(0xFFFFFFFF.toInt() - tintColor, tintColor)
        drawable?.invalidateSelf()

        val (startAlpha, endAlpha) = if (striked) 255 to hexAlpha else hexAlpha to 255
        contentDrawable.let { contentDrawable ->
            ObjectAnimator.ofInt(contentDrawable, "alpha", startAlpha, endAlpha).apply {
                duration = 500
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }

        strikeDrawable.start()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun applyStrikeAnimation() {
        val strikeDrawable = if (isStriked) {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_in) as AnimatedVectorDrawable
        } else {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_out) as AnimatedVectorDrawable
        }

        (drawable as LayerDrawable).setDrawable(1, strikeDrawable)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val strikeDrawable = if (isStriked) {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_in) as AnimatedVectorDrawable
        } else {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_out) as AnimatedVectorDrawable
        }

        super.setImageDrawable(LayerDrawable(arrayOf(drawable, strikeDrawable)))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isStriked = isStriked
    }
}