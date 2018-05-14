package com.hananabid.commons.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.hananabid.commons.R
import com.hananabid.commons.extensions.baseConfig
import com.hananabid.commons.extensions.toast
import com.hananabid.commons.extensions.updateTextColors
import com.hananabid.commons.helpers.PROTECTION_PATTERN
import com.hananabid.commons.interfaces.HashListener
import com.hananabid.commons.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_pattern.view.*

class PatternTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {
    private var hash = ""
    private var requiredHash = ""
    lateinit var hashListener: HashListener

    override fun onFinishInflate() {
        super.onFinishInflate()
        val textColor = context.baseConfig.textColor
        context.updateTextColors(pattern_lock_holder)
        pattern_lock_view.correctStateColor = context.baseConfig.primaryColor
        pattern_lock_view.normalStateColor = textColor
        pattern_lock_view.addPatternLockListener(object : PatternLockViewListener {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                receivedHash(PatternLockUtils.patternToSha1(pattern_lock_view, pattern))
            }

            override fun onCleared() {}

            override fun onStarted() {}

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {
            }
        })
    }

    override fun initTab(requiredHash: String, listener: HashListener) {
        this.requiredHash = requiredHash
        hash = requiredHash
        hashListener = listener
    }

    private fun receivedHash(newHash: String) {
        when {
            hash.isEmpty() -> {
                hash = newHash
                pattern_lock_view.clearPattern()
                pattern_lock_title.setText(R.string.repeat_pattern)
            }
            hash == newHash -> {
                pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                Handler().postDelayed({
                    hashListener.receivedHash(hash, PROTECTION_PATTERN)
                }, 300)
            }
            else -> {
                pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.WRONG)
                context.toast(R.string.wrong_pattern)
                Handler().postDelayed({
                    pattern_lock_view.clearPattern()
                    if (requiredHash.isEmpty()) {
                        hash = ""
                        pattern_lock_title.setText(R.string.insert_pattern)
                    }
                }, 1000)
            }
        }
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
