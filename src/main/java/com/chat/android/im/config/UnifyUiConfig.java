package com.chat.android.im.config;

import android.content.Context;
import android.graphics.Color;

import com.chat.android.im.R;
import com.chat.android.im.utils.IMUtilsKt;

/**
 * Created by Ryan on 2020/9/30.
 */
public class UnifyUiConfig {

    private int statusBarColor;
    private int navigationIcon;
    private Integer backIconWidth;
    private Integer backIconHeight;
    private Integer navigationBackgroundColor;
    private int navHeight;
    private String title = "客服聊天";
    private Integer titleColor = Color.BLACK;
    private float navTitleSize;
    private boolean navTitleBold;
    private boolean isHideNavigation;
    private int backgroundColor;
    private int backgroundRes;
    private int timeColor;
    private int timeSize;
    private int receiveTextColor;
    private int receiveTextSize;
    private int receiveBackgroundRes;
    private int sendTextColor;
    private int sendTextSize;
    private int sendBackgroundRes;
    private int bottomBackgroundColor;
    private int editTextBackgroundRes;
    private int editTextColor;
    private int sendBtnTextColor;
    private int sendBtnBackgroundRes;

    private UnifyUiConfig(Builder builder, Context context) {
        this.statusBarColor = builder.statusBarColor != null ? builder.statusBarColor : R.color.im_status_bar;
        this.navigationIcon = IMUtilsKt.getDrawableByName(context, builder.navigationIcon != null ? builder.navigationIcon : "icon_title_back");
        this.backIconWidth = builder.backIconWidth;
        this.backIconHeight = builder.backIconHeight;
        this.navigationBackgroundColor = builder.navigationBackgroundColor != null ? builder.navigationBackgroundColor : R.color.im_white;
        this.title = builder.title != null ? builder.title : this.title;
        this.titleColor = builder.titleColor != null ? builder.titleColor : this.titleColor;
        this.navTitleBold = builder.navTitleBold == true;
        this.isHideNavigation = builder.isHideNavigation == true;
        this.backgroundColor = builder.backgroundColor != null ? builder.backgroundColor : R.color.im_background;
        this.timeColor = builder.timeColor != null ? builder.timeColor : R.color.im_time_text;
        this.timeSize = builder.timeSize != null ? builder.timeSize : IMUtilsKt.px2sp(context, context.getResources().getDimension(R.dimen.sp_12));
        this.receiveTextColor = builder.receiveTextColor != null ? builder.receiveTextColor : R.color.im_receive_text;
        this.receiveTextSize = builder.receiveTextSize != null ? builder.receiveTextSize : IMUtilsKt.px2sp(context, context.getResources().getDimension(R.dimen.sp_14));
        this.receiveBackgroundRes = IMUtilsKt.getDrawableByName(context, builder.receiveBackgroundRes != null ? builder.receiveBackgroundRes : "im_receive_bg");
        this.sendTextColor = builder.sendTextColor != null ? builder.sendTextColor : R.color.im_white;
        this.sendTextSize = builder.sendTextSize != null ? builder.sendTextSize : IMUtilsKt.px2sp(context, context.getResources().getDimension(R.dimen.sp_14));
        this.sendBackgroundRes = IMUtilsKt.getDrawableByName(context, builder.sendBackgroundRes != null ? builder.sendBackgroundRes : "im_send_bg");
        this.bottomBackgroundColor = builder.bottomBackgroundColor != null ? builder.bottomBackgroundColor : R.color.im_white;
        this.editTextBackgroundRes = IMUtilsKt.getDrawableByName(context, builder.editTextBackgroundRes != null ? builder.editTextBackgroundRes : "im_message_edittext_bg");
        this.editTextColor = builder.editTextColor != null ? builder.editTextColor : R.color.im_receive_text;
        this.sendBtnTextColor = builder.sendBtnTextColor != null ? builder.sendBtnTextColor : R.color.im_white;
        this.sendBtnBackgroundRes = IMUtilsKt.getDrawableByName(context, builder.sendBtnBackgroundRes != null ? builder.sendBtnBackgroundRes : "im_message_shap_send_bg");

        if (builder.backgroundRes != null) {
            this.backgroundRes = IMUtilsKt.getDrawableByName(context, builder.backgroundRes);
        } else {
            this.backgroundRes = 0;
        }


        if (builder.navTitleSize != null) {
            this.navTitleSize = builder.navTitleSize;
        } else {
            this.navTitleSize = 21f;
        }

        if (builder.navHeight != null) {
            this.navHeight = IMUtilsKt.dip2px(context, builder.navHeight);
        } else {
            this.navHeight = (int) context.getResources().getDimension(R.dimen.dp_50);
        }
    }

    public int getStatusBarColor() {
        return statusBarColor;
    }

    public int getNavigationIcon() {
        return navigationIcon;
    }

    public Integer getBackIconWidth() {
        return backIconWidth;
    }

    public Integer getBackIconHeight() {
        return backIconHeight;
    }

    public Integer getNavigationBackgroundColor() {
        return navigationBackgroundColor;
    }

    public int getNavHeight() {
        return navHeight;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTitleColor() {
        return titleColor;
    }

    public float getNavTitleSize() {
        return navTitleSize;
    }

    public boolean isNavTitleBold() {
        return navTitleBold;
    }

    public boolean isHideNavigation() {
        return isHideNavigation;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getBackgroundRes() {
        return backgroundRes;
    }

    public int getTimeColor() {
        return timeColor;
    }

    public int getTimeSize() {
        return timeSize;
    }

    public int getReceiveTextColor() {
        return receiveTextColor;
    }

    public int getReceiveTextSize() {
        return receiveTextSize;
    }

    public int getReceiveBackgroundRes() {
        return receiveBackgroundRes;
    }

    public int getSendTextColor() {
        return sendTextColor;
    }

    public int getSendTextSize() {
        return sendTextSize;
    }

    public int getSendBackgroundRes() {
        return sendBackgroundRes;
    }

    public int getBottomBackgroundColor() {
        return bottomBackgroundColor;
    }

    public int getEditTextBackgroundRes() {
        return editTextBackgroundRes;
    }

    public int getEditTextColor() {
        return editTextColor;
    }

    public int getSendBtnTextColor() {
        return sendBtnTextColor;
    }

    public int getSendBtnBackgroundRes() {
        return sendBtnBackgroundRes;
    }

    public static class Builder {

        private Integer statusBarColor = null;
        private String navigationIcon = null;
        private Integer backIconWidth = null;
        private Integer backIconHeight = null;
        private Integer navigationBackgroundColor = null;
        private Integer navHeight = null;
        private String title = null;
        private Integer titleColor = null;
        private Integer navTitleSize = null;
        private boolean navTitleBold = false;
        private boolean isHideNavigation = false;
        private Integer backgroundColor = null;
        private String backgroundRes = null;
        private Integer timeColor = null;
        private Integer timeSize = null;
        private Integer receiveTextColor = null;
        private Integer receiveTextSize = null;
        private String receiveBackgroundRes = null;
        private Integer sendTextColor = null;
        private Integer sendTextSize = null;
        private String sendBackgroundRes = null;
        private Integer bottomBackgroundColor = null;
        private String editTextBackgroundRes = null;
        private Integer editTextColor = null;
        private Integer sendBtnTextColor = null;
        private String sendBtnBackgroundRes = null;

        public Builder setStatusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
            return this;
        }

        public Builder setNavigationIcon(String navigationIcon) {
            this.navigationIcon = navigationIcon;
            return this;
        }

        public Builder setNavigationBackIconWidth(int backIconWidth) {
            this.backIconWidth = backIconWidth;
            return this;
        }

        public Builder setNavigationBackgroundColor(int navigationBackgroundColor) {
            this.navigationBackgroundColor = navigationBackgroundColor;
            return this;
        }

        public Builder setNavigationTitleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setHideNavigation(boolean isHideNavigation) {
            this.isHideNavigation = isHideNavigation;
            return this;
        }

        public Builder setNavTitleBold(boolean navTitleBold) {
            this.navTitleBold = navTitleBold;
            return this;
        }

        public Builder setNavTitleSize(int navTitleSize) {
            this.navTitleSize = navTitleSize;
            return this;
        }

        public Builder setNavigationTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setNavigationHeight(int navHeight) {
            this.navHeight = navHeight;
            return this;
        }

        public Builder setNavigationBackIconHeight(int backIconHeight) {
            this.backIconHeight = backIconHeight;
            return this;
        }

        public Builder setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setBackgroundRes(String backgroundRes) {
            this.backgroundRes = backgroundRes;
            return this;
        }

        public Builder setItemShowTimeColor(int timeColor) {
            this.timeColor = timeColor;
            return this;
        }

        public Builder setItemShowTimeSize(int timeSize) {
            this.timeSize = timeSize;
            return this;
        }

        public Builder setItemReceiveTextColor(int receiveTextColor) {
            this.receiveTextColor = receiveTextColor;
            return this;
        }

        public Builder setItemSendTextColor(int sendTextColor) {
            this.sendTextColor = sendTextColor;
            return this;
        }

        public Builder setItemReceiveTextSize(int receiveTextSize) {
            this.receiveTextSize = receiveTextSize;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTitleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setItemReceiveBackgroundRes(String receiveBackgroundRes) {
            this.receiveBackgroundRes = receiveBackgroundRes;
            return this;
        }

        public Builder setItemSendBackgroundRes(String sendBackgroundRes) {
            this.sendBackgroundRes = sendBackgroundRes;
            return this;
        }

        public Builder setItemSendTextSize(int sendTextSize) {
            this.sendTextSize = sendTextSize;
            return this;
        }

        public Builder setBottomBackgroundColor(int bottomBackgroundColor) {
            this.bottomBackgroundColor = bottomBackgroundColor;
            return this;
        }

        public Builder setEditTextBackgroundRes(String editTextBackgroundRes) {
            this.editTextBackgroundRes = editTextBackgroundRes;
            return this;
        }

        public Builder setEditTextColor(int editTextColor) {
            this.editTextColor = editTextColor;
            return this;
        }

        public Builder setSendBtnTextColor(int sendBtnTextColor) {
            this.sendBtnTextColor = sendBtnTextColor;
            return this;
        }

        public Builder setSendBtnBackgroundRes(String sendBtnBackgroundRes) {
            this.sendBtnBackgroundRes = sendBtnBackgroundRes;
            return this;
        }

        public UnifyUiConfig build(Context context) {
            return new UnifyUiConfig(this, context);
        }
    }

}
