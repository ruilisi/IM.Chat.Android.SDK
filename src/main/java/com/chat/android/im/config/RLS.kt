package com.chat.android.im.config

import android.content.Context
import android.content.Intent
import com.chat.android.im.activity.ChatActivity

/**
 * Created by Ryan on 2020/8/28.
 */
internal class RLS private constructor() {

    private lateinit var application: Context
    private var businessId: String? = empty
    private var uiConfig: UnifyUiConfig? = null
    private var dataConfig: UnifyDataConfig? = null

    companion object {
        @JvmStatic
        val empty = ""

        @JvmStatic
        fun getInstance(): RLS {
            return Helper.instance
        }
    }

    private object Helper {
        val instance = RLS()
    }

    fun init(application: Context, businessId: String) {
        this.application = application
        this.businessId = businessId
    }


    fun getUiConfig(): UnifyUiConfig {
        return uiConfig ?: UnifyUiConfig.Builder().build(application)
    }

    fun getDataConfig(): UnifyDataConfig {
        return dataConfig ?: UnifyDataConfig.Builder().build()
    }

    fun setUiConfig(uiConfig: UnifyUiConfig) {
        this.uiConfig = uiConfig
    }

    fun setDataConfig(dataConfig: UnifyDataConfig) {
        this.dataConfig = dataConfig
    }

    fun go() {
        val intent = Intent(application, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }

    fun getApp(): Context {
        return application
    }


}