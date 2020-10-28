package com.chat.android.im.helper

import com.chat.android.im.bean.NetType

/**
 * Created by Ryan on 2020/9/3.
 */
interface OnNetworkListener {

    fun onNetworkStateListener(type: NetType?)
}