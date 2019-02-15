package com.example.administrator.recycler.widget

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class NoScrollViewPager: ViewPager{
    private var isIntercept = true

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?) = isIntercept && super.onInterceptTouchEvent(ev)

    fun seItIntercept(intercept: Boolean){
        isIntercept = intercept
    }

    override fun onTouchEvent(ev: MotionEvent?) = isIntercept && super.onTouchEvent(ev)



}