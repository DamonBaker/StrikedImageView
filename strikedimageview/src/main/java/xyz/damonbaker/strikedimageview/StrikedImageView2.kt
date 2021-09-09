package xyz.damonbaker.strikedimageview2

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.LightingColorFilter
import android.graphics.drawable.*
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import xyz.damonbaker.strikedimageview.R


class StrikedImageView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var contentDrawable: Drawable? = null

    var isStriked: Boolean = true
        set(value) {
            field = value
            strikeWithAnimation(value)
        }

    fun getParentBackgroundColor(): Int? {
        return (parent as? View)?.background
            ?.let { bg ->
                (bg as ColorDrawable).color
            }
    }

    fun strikeWithoutAnimation(striked: Boolean) {
        if (isStriked) {

        }
        val (sourceDrawable, strikeDrawable) = (drawable as LayerDrawable).run {
            val strikeDrawable = ContextCompat.getDrawable(context, R.drawable.strike)
            val purple = ContextCompat.getColor(context, android.R.color.holo_purple)
            strikeDrawable?.colorFilter = LightingColorFilter((0xFFFFFFFF.toInt() - purple), purple)
            strikeDrawable?.invalidateSelf()
            setDrawable(1, strikeDrawable)
            getDrawable(0) to getDrawable(1)
        }
        if (striked) {
//            isStriked = true
            sourceDrawable.alpha = 85
        } else {
            sourceDrawable.alpha = 255
            strikeDrawable.alpha = 0
        }
    }

    private fun strikeWithAnimation(striked: Boolean) {
        setImageDrawable(contentDrawable)

        val purple = ContextCompat.getColor(context, android.R.color.holo_purple)
        drawable?.colorFilter = LightingColorFilter((0xFFFFFFFF.toInt() - purple), purple)
        drawable?.invalidateSelf()

        val (startAlpha, endAlpha) = if (striked) 255 to 85 else 85 to 255
        (drawable as LayerDrawable).getDrawable(0).let { contentDrawable ->
            ObjectAnimator.ofInt(contentDrawable, "alpha", startAlpha, endAlpha).apply {
                duration = 500
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }

        (drawable as LayerDrawable).apply {
            if (this is VectorDrawable) setDrawable(1, ContextCompat.getDrawable(context, R.drawable.strike))
        }.also { layerDrawable ->
            (layerDrawable.getDrawable(1) as AnimatedVectorDrawable).start()
        }
//        ((drawable as LayerDrawable).getDrawable(1) as? AnimatedVectorDrawable)?.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        strikeWithoutAnimation(isStriked)
//        isStriked = isStriked
    }

    override fun setImageDrawable(drawable: Drawable?) {
        contentDrawable = drawable

        val strikeDrawable = if (isStriked) {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_in) as AnimatedVectorDrawable
        } else {
            ContextCompat.getDrawable(context, R.drawable.strike_animate_out) as AnimatedVectorDrawable
        }

        super.setImageDrawable(LayerDrawable(arrayOf(drawable, strikeDrawable)))
    }

}


