package com.hananabid.commons.extensions

import android.support.v4.view.ViewPager

fun ViewPager.onPageChangeListener(pageChangedAction: (newPosition: Int) -> Unit) =
        addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                pageChangedAction(position)
            }
        })
