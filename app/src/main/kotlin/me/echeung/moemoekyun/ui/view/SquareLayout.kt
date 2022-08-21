package me.echeung.moemoekyun.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.min

@SuppressLint("AppCompatCustomView")
class SquareLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val dimen = min(measuredWidth, measuredHeight)
        setMeasuredDimension(dimen, dimen)
    }
}
