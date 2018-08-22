package me.echeung.moemoekyun.util;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ViewUtil {

    private static final long BACKGROUND_COLOR_TRANSITION_DURATION = 500L;

    public static void transitionBackgroundColor(View view, @ColorInt int toColor) {
        if (view.getBackground() == null) {
            view.setBackgroundColor(toColor);
            return;
        }

        int fromColor = ((ColorDrawable) view.getBackground()).getColor();

        ValueAnimator valueAnimator = ValueAnimator.ofArgb(fromColor, toColor);
        valueAnimator.setDuration(BACKGROUND_COLOR_TRANSITION_DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animator -> view.setBackgroundColor((Integer) animator.getAnimatedValue()));
        valueAnimator.start();
    }

}
