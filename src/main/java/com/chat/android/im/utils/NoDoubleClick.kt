package com.chat.android.im.utils

import android.view.View
import com.chat.android.im.R

/**
 * Created by Ryan on 2020/7/21.
 */
private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(R.id.triggerLastTimeKey) != null) getTag(R.id.triggerLastTimeKey) as Long else 0
    set(value) = setTag(R.id.triggerLastTimeKey, value)

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(R.id.triggerDelayKey) != null) getTag(R.id.triggerDelayKey) as Long else 0
    set(value) = setTag(R.id.triggerDelayKey, value)

private fun <T : View> T.clickEnable(): Boolean {
    var clickable = false
    var currentTime = System.currentTimeMillis()
    if (currentTime - triggerLastTime >= triggerDelay) {
        clickable = true
    }
    triggerLastTime = currentTime
    return clickable
}

//点击调用
fun <T : View> T.clickWithTrigger(delay: Long = 600, block: (T) -> Unit) {
    triggerDelay = delay
    setOnClickListener {
        if (clickEnable()) {
            block(this)
        }
    }
}

