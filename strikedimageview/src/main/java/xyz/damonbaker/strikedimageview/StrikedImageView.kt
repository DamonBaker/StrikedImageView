package xyz.damonbaker.strikedimageview

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.parcel.Parcelize

@Parcelize
private class ViewState(
    val superSavedState: Parcelable?,
    val isStriked: Boolean
) : View.BaseSavedState(superSavedState), Parcelable

class StrikedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var _isStriked: Boolean // Backing property
    var isStriked: Boolean
        get() = _isStriked
        set(value) {
            _isStriked = value
            animateStrike()
        }

    val contentDrawable: Drawable
        get() = drawable.asLayerDrawable().getDrawable(0)

    val strikeDrawableMask: AnimatedVectorDrawable
        get() = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable().getDrawable(0).asAnimatedVectorDrawable()

    val strikeDrawable: AnimatedVectorDrawable
        get() = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable().getDrawable(1).asAnimatedVectorDrawable()

    val strikeAnimateIn = context.getDrawable(R.drawable.strike_animate_in)
    val strikeAnimateOut = context.getDrawable(R.drawable.strike_animate_out)
    val maskAnimateIn = context.getDrawable(R.drawable.mask_animate_in)
    val maskAnimateOut = context.getDrawable(R.drawable.mask_animate_out)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StrikedImageView, 0, 0).apply {
            _isStriked = getBoolean(R.styleable.StrikedImageView_striked, false)
            strikeWithoutAnimation(isStriked)
            recycle()
        }
        if (imageTintList == null) {
            imageTintList = ColorStateList.valueOf(Color.BLACK)
        }
    }

    override fun setImageTintList(tint: ColorStateList?) {
        super.setImageTintList(tint)
        refreshTint()
    }

    private fun animateStrike() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStrikeAnimation()
        } else {
            setImageDrawable(drawable)
        }

        // Reset alpha in case strikeWithoutAnimation was called
        if (strikeDrawable.alpha != 255) strikeDrawable.alpha = 255

        // Re-apply tint and color filter in case the background color of the parent has changed
        refreshTint()

        val (startAlpha, endAlpha) = if (isStriked) 255 to STRIKE_ALPHA else STRIKE_ALPHA to 255
        ObjectAnimator.ofInt(contentDrawable, "alpha", startAlpha, endAlpha).apply {
            duration = STRIKE_ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()
            start()
        }

        strikeDrawable.start()
        strikeDrawableMask.start()
    }

    fun strikeWithoutAnimation(showStrike: Boolean) {
        _isStriked = showStrike

        val strikeLayerDrawable = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable()

        if (showStrike) {
            strikeLayerDrawable.setDrawable(0, context.getDrawable(R.drawable.mask_animate_out))
            strikeLayerDrawable.setDrawable(1, context.getDrawable(R.drawable.strike_animate_out))
            strikeDrawable.alpha = STRIKE_ALPHA
            contentDrawable.alpha = STRIKE_ALPHA
            refreshTint()
        } else {
            strikeLayerDrawable.setDrawable(0, context.getDrawable(R.drawable.mask_animate_in))
            strikeLayerDrawable.setDrawable(1, context.getDrawable(R.drawable.strike_animate_in))
            contentDrawable.alpha = 255
        }
    }

    private fun refreshTint() {
        val white = 0xFFFFFFFF.toInt()
        val parentBackground = getBackgroundColor() ?: white
        val tintColor = ImageViewCompat.getImageTintList(this)?.defaultColor ?: white

        strikeDrawableMask.setTint(parentBackground)
        strikeDrawable.colorFilter = LightingColorFilter(white - tintColor, tintColor)

        drawable?.invalidateSelf()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setStrikeAnimation() {
        val strikeLayerDrawable = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable()

        if (isStriked) {
            strikeLayerDrawable.setDrawable(0, context.getDrawable(R.drawable.mask_animate_in))
            strikeLayerDrawable.setDrawable(1, context.getDrawable(R.drawable.strike_animate_in))
        } else {
            strikeLayerDrawable.setDrawable(0, context.getDrawable(R.drawable.mask_animate_out))
            strikeLayerDrawable.setDrawable(1, context.getDrawable(R.drawable.strike_animate_out))
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val strikeDrawableOverlay = arrayOf(
            context.getDrawable(if (isStriked) R.drawable.mask_animate_in else R.drawable.mask_animate_out)?.mutate(),
            context.getDrawable(if (isStriked) R.drawable.strike_animate_in else R.drawable.strike_animate_out)?.mutate()
        )
        val strikeLayerDrawable = LayerDrawable(strikeDrawableOverlay)

        super.setImageDrawable(LayerDrawable(arrayOf(drawable, strikeLayerDrawable)))
    }

    override fun onSaveInstanceState(): Parcelable = ViewState(super.onSaveInstanceState(), isStriked)

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is ViewState -> {
                super.onRestoreInstanceState(state.superSavedState)
                isStriked = state.isStriked
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    // Traverse view hierarchy to find the first parent with a background color
    private tailrec fun View.getBackgroundColor(): Int? {
        return (background as? ColorDrawable)?.color
            ?: (parent as? View)?.getBackgroundColor()
    }

    private fun Drawable.asLayerDrawable() = this as LayerDrawable
    private fun Drawable.asAnimatedVectorDrawable() = this as AnimatedVectorDrawable

    companion object {
        private const val STRIKE_ANIMATION_DURATION = 500L
        // Hex value for 38% opacity - Material Guidelines recommended value for disabled states
        private const val STRIKE_ALPHA = 97
    }
}
