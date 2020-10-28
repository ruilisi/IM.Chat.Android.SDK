package com.chat.android.im.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 * Created by feng on 2017/8/14.
 */

public class KeyBoardPatch {
    private Activity activity;
    private View decorView;
    private View contentView;


    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            //获取当前界面可视部分
            decorView.getWindowVisibleDisplayFrame(r);
            //获取屏幕的高度
            int height = decorView.getRootView().getHeight();
//            int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
            //此处就是用来获取键盘的高度的， 在键盘没有弹出的时候 此高度为0 键盘弹出的时候为一个正数
            int diff = height - r.bottom;

//            MyUtils.log("---height---->" + height + "<--r.bottom-->" + r.bottom + "<--diff-->" + diff + "<--contentView.getPaddingBottom()-->" + contentView.getPaddingBottom());

            if (contentView.getPaddingBottom() != diff) {
                contentView.setPadding(0, 0, 0, diff);
            }
        }
    };

    /**
     * 构造函数
     *
     * @param act         需要解决bug的activity
     * @param contentView 界面容器，activity中一般是R.id.content，也可能是Fragment的容器，根据个人需要传递
     */
    public KeyBoardPatch(Activity act, View contentView) {
        this.activity = act;
        this.decorView = act.getWindow().getDecorView();
        this.contentView = contentView;
//        this.editBottomView = editBottomView;
    }

    /**
     * 监听layout变化
     */
    public void enable() {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    /**
     * 取消监听
     */
    public void disable() {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (Build.VERSION.SDK_INT >= 19) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }


}
