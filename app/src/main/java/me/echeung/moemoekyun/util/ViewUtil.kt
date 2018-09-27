package me.echeung.moemoekyun.util

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.animation.LinearInterpolator

import androidx.annotation.ColorInt

object ViewUtil {

    const val TRANSITION_DURATION = 250

    fun transitionBackgroundColor(view: View, @ColorInt toColor: Int) {
        if (view.background == null) {
            view.setBackgroundColor(toColor)
            return
        }

        val fromColor = (view.background as ColorDrawable).color

        val valueAnimator = ValueAnimator.ofArgb(fromColor, toColor)
        valueAnimator.duration = TRANSITION_DURATION.toLong()
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener { animator -> view.setBackgroundColor(animator.animatedValue as Int) }
        valueAnimator.start()
    }

}
