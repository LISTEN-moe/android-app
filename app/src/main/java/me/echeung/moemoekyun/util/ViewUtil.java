package me.echeung.moemoekyun.util;

import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ViewUtil {

    protected static final int TRANSITION_DURATION = 250;

    public static void transitionBackgroundColor(View view, @ColorInt int toColor) {
        if (view.getBackground() == null) {
            view.setBackgroundColor(toColor);
            return;
        }

        int fromColor = ((ColorDrawable) view.getBackground()).getColor();

        ValueAnimator valueAnimator = ValueAnimator.ofArgb(fromColor, toColor);
        valueAnimator.setDuration(TRANSITION_DURATION);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animator -> view.setBackgroundColor((Integer) animator.getAnimatedValue()));
        valueAnimator.start();
    }

}
