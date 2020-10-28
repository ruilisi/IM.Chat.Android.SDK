package com.chat.android.im.helper;

/**
 * Created by Ryan on 2020/10/11.
 */
public interface IMCallback {
    void onFailure(String error);

    void onSuccess();//保留方法
}
