package com.chat.android.im.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.chat.android.im.bean.NetType
import com.chat.android.im.helper.OnNetworkListener

/**
 * Created by Ryan on 2020/9/3.
 */
class NetworkListener private constructor() {

    private var onNetworkListener: OnNetworkListener? = null

    private val defaultNetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()


    private val defaultNetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            LogUtils.e("NetworkListener", "onAvailable: 网络已连接")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities?) {
            if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    LogUtils.e("NetworkListener", "onCapabilitiesChanged: 网络类型为wifi")
                    onNetworkListener?.onNetworkStateListener(NetType.WIFI)
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    LogUtils.e("NetworkListener", "onCapabilitiesChanged: 蜂窝网络")
                    onNetworkListener?.onNetworkStateListener(NetType.CMWAP)
                } else {
                    LogUtils.e("NetworkListener", "onCapabilitiesChanged: 其他网络")
                    onNetworkListener?.onNetworkStateListener(NetType.AUTO)
                }
            }
        }

        override fun onLost(network: Network) {
            LogUtils.e("NetworkListener", "onLost: 网络已断开")
            onNetworkListener?.onNetworkStateListener(null)
        }
    }

    companion object {
        fun getInstance(): NetworkListener {
            return Helper.instance
        }
    }

    private object Helper {
        val instance = NetworkListener()
    }

    fun registerNetWorkListener(context: Context, onNetworkListener: OnNetworkListener) {
        this.onNetworkListener = onNetworkListener
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).registerNetworkCallback(defaultNetworkRequest, defaultNetworkCallback)
    }

    fun unRegisterNetWorkListener(context: Context) {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).unregisterNetworkCallback(defaultNetworkCallback)
    }
}