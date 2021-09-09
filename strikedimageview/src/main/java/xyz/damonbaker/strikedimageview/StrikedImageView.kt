package xyz.damonbaker.strikedimageview

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
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
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.parcelize.Parcelize

@Parcelize
private class ViewState(
    val superSavedState: Parcelable?,
    val isStriked: Boolean,
    val strikeColor: Int?,
    val strikeBackgroundColor: Int?
) : View.BaseSavedState(superSavedState), Parcelable

class StrikedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var _isStriked: Boolean // Backing property

    /**
     * Animate and set the striked state of the view
     */
    var isStriked: Boolean
        get() = _isStriked
        set(value) {
            _isStriked = value
            animateStrike()
        }

    /**
     * Color of the lower half of the strike, recommended to be the same color as the icon tint
     * Optional as the icon tint can be inferred automatically
     */
    var strikeColor: Int? = null
        set(value) {
            refreshTint()
            field = value
        }

    /**
     * Color of the top half of the strike, recommended to be the same color as the parent background
     * Optional as the parent background color will attempt to be inferred automatically
     */
    var strikeBackgroundColor: Int? = null
        set(value) {
            refreshTint()
            isEnabled
            field = value
        }

    /**
     * Returns the user-defined drawable that the strike draws over
     */
    val contentDrawable: Drawable?
        get() = drawable.asLayerDrawable().getDrawable(0)

    private val strikeDrawableMask: AnimatedVectorDrawable
        get() = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable().getDrawable(0).asAnimatedVectorDrawable()

    private val strikeDrawable: AnimatedVectorDrawable
        get() = drawable.asLayerDrawable().getDrawable(1).asLayerDrawable().getDrawable(1).asAnimatedVectorDrawable()

    private val strikeAnimateIn: LayerDrawable
        get() = AppCompatResources.getDrawable(context, R.drawable.layer_strike_animate_in)?.mutate() as LayerDrawable

    private val strikeAnimateOut: LayerDrawable
        get() = AppCompatResources.getDrawable(context, R.drawable.layer_strike_animate_out)?.mutate() as LayerDrawable

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StrikedImageView, 0, 0).apply {
            _isStriked = getBoolean(R.styleable.StrikedImageView_striked, false)
            if (hasValue(R.styleable.StrikedImageView_strikeBackgroundColor)) {
                strikeBackgroundColor = getInt(R.styleable.StrikedImageView_strikeBackgroundColor, 0)
            }
            if (hasValue(R.styleable.StrikedImageView_strikeColor)) {
                strikeColor = getInt(R.styleable.StrikedImageView_strikeColor, 0)
            }
            recycle()
        }
        // A tint is required on the ImageView so we can match the strike color, otherwise assume the icon is black
//        if (imageTintList == null) {
//            imageTintList = ColorStateList.valueOf(Color.BLACK)
//        }
        strikeWithoutAnimation(isStriked)
    }

    /**
     * Set the striked state without the transition animation
     */
    fun strikeWithoutAnimation(showStrike: Boolean) {
        val strikeLayerDrawable = drawable.asLayerDrawable()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            strikeLayerDrawable.setDrawable(1, if (showStrike) strikeAnimateOut else strikeAnimateIn)
        } else {
            // Fallback for SDK < 23 which does not allow `setDrawable` to be called on a LayerDrawable
            _isStriked = !showStrike
            setImageDrawable(contentDrawable)
        }

        _isStriked = showStrike

        if (showStrike) {
            strikeDrawable.alpha = STRIKE_ALPHA
            contentDrawable?.alpha = STRIKE_ALPHA
            refreshTint()
        } else {
            strikeDrawable.alpha = 0
            strikeDrawableMask.alpha = 0
            contentDrawable?.alpha = 255
        }
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

    private fun refreshTint() {
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()
        // If parent view background color cannot be found and no background color is specified, default to white
        val parentBackground = strikeBackgroundColor ?: getBackgroundColor() ?: white
        val tintColor = strikeColor ?: ImageViewCompat.getImageTintList(this)?.defaultColor ?: black

        strikeDrawableMask.setTint(parentBackground)
        strikeDrawable.colorFilter = LightingColorFilter(white - tintColor, tintColor)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setStrikeAnimation() {
        val strikeLayerDrawable = drawable.asLayerDrawable()

        if (isStriked) {
            strikeLayerDrawable.setDrawable(1, strikeAnimateIn)
        } else {
            strikeLayerDrawable.setDrawable(1, strikeAnimateOut)
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val strikeAnimation = if (isStriked) strikeAnimateIn else strikeAnimateOut

        super.setImageDrawable(LayerDrawable(arrayOf(drawable, strikeAnimation)))

        refreshTint()
    }

    override fun setImageTintList(tint: ColorStateList?) {
        super.setImageTintList(tint)
        refreshTint()
    }

    override fun onSaveInstanceState(): Parcelable {
        return ViewState(super.onSaveInstanceState(), isStriked, strikeColor, strikeBackgroundColor)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is ViewState -> {
                super.onRestoreInstanceState(state.superSavedState)
                isStriked = state.isStriked
                strikeColor = state.strikeColor
                strikeBackgroundColor = state.strikeBackgroundColor
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
