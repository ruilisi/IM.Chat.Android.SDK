package com.chat.android.im.constant

/**
 * Created by Ryan on 2020/9/8.
 */

/**
 * The interval of continuous message sending without displaying time
 */
const val SEND_INTERVAL_NO_SHOW_TIME: Int = 5 * 60 * 1000
//const val SEND_INTERVAL_NO_SHOW_TIME: Int = 10 * 1000

/**
 * Send a heartbeat detection every 10 seconds
 */
const val HEART_BEAT_RATE = 10 * 1000.toLong()

/**
 * Send cached messages every 10 seconds
 */
const val DEFAULT_SEND_INTERVAL = 100L
