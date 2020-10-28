package com.chat.android.im.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.util.MultiTypeDelegate;
import com.chat.android.im.R;
import com.chat.android.im.bean.ChatMessage;
import com.chat.android.im.bean.MsgBody;
import com.chat.android.im.bean.MsgSendStatus;
import com.chat.android.im.bean.MsgStatus;
import com.chat.android.im.bean.MsgType;
import com.chat.android.im.config.RLS;
import com.chat.android.im.config.UnifyUiConfig;
import com.chat.android.im.utils.DateUtils;
import com.chat.android.im.utils.GlideUtils;

import java.util.List;

import static com.chat.android.im.utils.IMUtilsKt.parseColor;

public class ChatAdapter extends BaseQuickAdapter<ChatMessage, BaseViewHolder> {


    private static final int TYPE_SEND_TEXT = 1;
    private static final int TYPE_RECEIVE_TEXT = 2;
    private static final int TYPE_SEND_IMAGE = 3;
    private static final int TYPE_RECEIVE_IMAGE = 4;
    private static final int TYPE_SEND_VIDEO = 5;
    private static final int TYPE_RECEIVE_VIDEO = 6;
    private static final int TYPE_SEND_FILE = 7;
    private static final int TYPE_RECEIVE_FILE = 8;
    private static final int TYPE_SEND_AUDIO = 9;
    private static final int TYPE_RECEIVE_AUDIO = 10;

    private static final int SEND_TEXT = R.layout.item_text_send;
    private static final int RECEIVE_TEXT = R.layout.item_text_receive;
    private static final int SEND_IMAGE = R.layout.item_image_send;
    private static final int RECEIVE_IMAGE = R.layout.item_image_receive;
    private static final int SEND_VIDEO = R.layout.item_video_send;
    private static final int RECEIVE_VIDEO = R.layout.item_video_receive;
    private static final int SEND_FILE = R.layout.item_file_send;
    private static final int RECEIVE_FILE = R.layout.item_file_receive;
    private static final int RECEIVE_AUDIO = R.layout.item_audio_receive;
    private static final int SEND_AUDIO = R.layout.item_audio_send;
    /*
    private static final int SEND_LOCATION = R.layout.item_location_send;
    private static final int RECEIVE_LOCATION = R.layout.item_location_receive;*/

    //    private long msgChangedTime = 0;
//    private boolean showTime = true;
//    private long itemCount = 0;
    private UnifyUiConfig mUiConfig = RLS.getInstance().getUiConfig();


    public ChatAdapter(Context context, List<ChatMessage> data) {
        super(data);
        setMultiTypeDelegate(new MultiTypeDelegate<ChatMessage>() {
            @Override
            protected int getItemType(ChatMessage entity) {
                boolean isSend = entity.getMsgStatus() == MsgStatus.SEND;
                if (MsgType.TEXT == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_TEXT : TYPE_RECEIVE_TEXT;
                } else if (MsgType.IMAGE == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_IMAGE : TYPE_RECEIVE_IMAGE;
                } else if (MsgType.VIDEO == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_VIDEO : TYPE_RECEIVE_VIDEO;
                } else if (MsgType.FILE == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_FILE : TYPE_RECEIVE_FILE;
                } else if (MsgType.AUDIO == entity.getMsgType()) {
                    return isSend ? TYPE_SEND_AUDIO : TYPE_RECEIVE_AUDIO;
                }
                return 0;
            }
        });
        getMultiTypeDelegate().registerItemType(TYPE_SEND_TEXT, SEND_TEXT)
                .registerItemType(TYPE_RECEIVE_TEXT, RECEIVE_TEXT)
                .registerItemType(TYPE_SEND_IMAGE, SEND_IMAGE)
                .registerItemType(TYPE_RECEIVE_IMAGE, RECEIVE_IMAGE)
                .registerItemType(TYPE_SEND_VIDEO, SEND_VIDEO)
                .registerItemType(TYPE_RECEIVE_VIDEO, RECEIVE_VIDEO)
                .registerItemType(TYPE_SEND_FILE, SEND_FILE)
                .registerItemType(TYPE_RECEIVE_FILE, RECEIVE_FILE)
                .registerItemType(TYPE_SEND_AUDIO, SEND_AUDIO)
                .registerItemType(TYPE_RECEIVE_AUDIO, RECEIVE_AUDIO);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatMessage item) {
        setContent(helper, item);
        setStatus(helper, item);
        setOnClick(helper, item);
    }


    private void setStatus(BaseViewHolder helper, ChatMessage item) {
        MsgType msgType = item.getMsgType();
        if (msgType == MsgType.TEXT
                || msgType == MsgType.AUDIO || msgType == MsgType.VIDEO || msgType == MsgType.FILE) {
            //只需要设置自己发送的状态
            MsgSendStatus sentStatus = item.getSentStatus();
            boolean isSend = item.getMsgStatus() == MsgStatus.SEND;
            if (isSend) {
                if (sentStatus == MsgSendStatus.SENDING) {
                    helper.setVisible(R.id.chat_item_progress, true).setVisible(R.id.chat_item_fail, false);
                } else if (sentStatus == MsgSendStatus.FAILED) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true);
                } else if (sentStatus == MsgSendStatus.SENT) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                }
            }
        } else if (msgType == MsgType.IMAGE) {
            boolean isSend = item.getMsgStatus() == MsgStatus.SEND;
            if (isSend) {
                MsgSendStatus sentStatus = item.getSentStatus();
                if (sentStatus == MsgSendStatus.SENDING) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                } else if (sentStatus == MsgSendStatus.FAILED) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true);
                } else if (sentStatus == MsgSendStatus.SENT) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false);
                }
            }
        }


    }

    private void setContent(BaseViewHolder helper, ChatMessage item) {
        if (item.getMsgType().equals(MsgType.TEXT)) {
            setTextMsgShow(helper, item);
        } else if (item.getMsgType().equals(MsgType.IMAGE)) {
            setImageMsgShow(helper, item);
            //            ImageMsgBody msgBody = (ImageMsgBody) item.getBody();
//            if (TextUtils.isEmpty(msgBody.getThumbPath())) {
//                GlideUtils.loadChatImage(mContext, msgBody.getThumbUrl(), (ImageView) helper.getView(R.id.bivPic));
//            } else {
//                File file = new File(msgBody.getThumbPath());
//                if (file.exists()) {
//                    GlideUtils.loadChatImage(mContext, msgBody.getThumbPath(), (ImageView) helper.getView(R.id.bivPic));
//                } else {
//                    GlideUtils.loadChatImage(mContext, msgBody.getThumbUrl(), (ImageView) helper.getView(R.id.bivPic));
//                }
//            }
        } else if (item.getMsgType().equals(MsgType.VIDEO)) {
//            VideoMsgBody msgBody = (VideoMsgBody) item.getBody();
//            File file = new File(msgBody.getExtra());
//            if (file.exists()) {
//                GlideUtils.loadChatImage(mContext, msgBody.getExtra(), (ImageView) helper.getView(R.id.bivPic));
//            } else {
//                GlideUtils.loadChatImage(mContext, msgBody.getExtra(), (ImageView) helper.getView(R.id.bivPic));
//            }
        } else if (item.getMsgType().equals(MsgType.FILE)) {
//            FileMsgBody msgBody = (FileMsgBody) item.getBody();
//            helper.setText(R.id.msg_tv_file_name, msgBody.getDisplayName());
//            helper.setText(R.id.msg_tv_file_size, msgBody.getSize() + "B");
        } else if (item.getMsgType().equals(MsgType.AUDIO)) {
//            AudioMsgBody msgBody = (AudioMsgBody) item.getBody();
//            helper.setText(R.id.tvDuration, msgBody.getDuration() + "\"");
        }
    }

    private void setImageMsgShow(BaseViewHolder helper, ChatMessage item) {

        MsgBody msgBody = item.getMsgBody();
        TextView timeView = helper.getView(R.id.item_tv_time);
        if (item.getTimeShow().getDate() == 0) {
            timeView.setVisibility(View.GONE);
        } else {
            timeView.setVisibility(View.VISIBLE);
            timeView.setTextColor(parseColor(timeView.getContext(), mUiConfig.getTimeColor()));
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.getTimeSize());
            timeView.setText(parseTime(item.getTimeShow().getDate()));
        }

        GlideUtils.loadChatImage(mContext, msgBody.getImageUrl(), helper.getView(R.id.bivPic));
    }

    private void setTextMsgShow(BaseViewHolder helper, ChatMessage item) {

        MsgBody msgBody = item.getMsgBody();

        TextView timeView = helper.getView(R.id.item_tv_time);
        if (item.getTimeShow().getDate() == 0) {
            timeView.setVisibility(View.GONE);
        } else {
            timeView.setVisibility(View.VISIBLE);
            timeView.setTextColor(parseColor(timeView.getContext(), mUiConfig.getTimeColor()));
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.getTimeSize());
            timeView.setText(parseTime(item.getTimeShow().getDate()));
        }
//        if (helper.getLayoutPosition() == itemCount) {
//            timeView.setVisibility(showTime ? View.VISIBLE : View.GONE);
//        }

        TextView contentText = helper.getView(R.id.chat_item_content_text);
        contentText.setText(msgBody.getMessage());
        if (item.getMsgStatus() == MsgStatus.SEND) {
            contentText.setTextColor(parseColor(contentText.getContext(), mUiConfig.getSendTextColor()));
            contentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.getSendTextSize());
            contentText.setBackgroundResource(mUiConfig.getSendBackgroundRes());
        } else {
            contentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.getReceiveTextSize());
            contentText.setTextColor(parseColor(contentText.getContext(), mUiConfig.getReceiveTextColor()));
            contentText.setBackgroundResource(mUiConfig.getReceiveBackgroundRes());
        }
    }

//    private void setTime(BaseViewHolder helper, TextMsgBody msgBody) {
//        int currentPosition = helper.getLayoutPosition();
//        helper.setText(R.id.item_tv_time, parseTime(msgBody.getTime()));
//        if (currentPosition - 1 < 0) {
//            helper.setVisible(R.id.item_tv_time, true);
//        } else {
//            TextMsgBody lastMsgBody = (TextMsgBody) getData().get(currentPosition - 1).getBody();
//            if (DateUtils.isMinute(msgBody.getTime(), lastMsgBody.getTime())) {
//                helper.setVisible(R.id.item_tv_time, false);
//            } else {
//                helper.setVisible(R.id.item_tv_time, true);
//            }
//        }
//    }

    private String parseTime(Long time) {
        if (time == null || time == 0) {
            return "";
        }
        long t = time;
        if (DateUtils.isToday(t)) {
            return DateUtils.getHourMinuteOfDate(t);
        } else if (DateUtils.isYear(t)) {
            return DateUtils.getDateTimeStr1(t);
        } else {
            return DateUtils.getDateTimeStr2(t);
        }
    }


    private void setOnClick(BaseViewHolder helper, ChatMessage item) {
//        MsgBody msgContent = item.getBody();
//        if (msgContent instanceof AudioMsgBody) {
//            helper.addOnClickListener(R.id.rlAudio);
//        }
    }

//    @Override
//    public void addData(@NonNull ChatMessage data) {
//        long currentTime = System.currentTimeMillis();
//        long subTime = currentTime - msgChangedTime;
//        msgChangedTime = currentTime;
//        showTime = subTime > SEND_INTERVAL_NO_SHOW_TIME;
//        itemCount = getItemCount();
//        super.addData(data);
//    }
}
