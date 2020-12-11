package com.chat.android.im.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.android.im.R;
import com.chat.android.im.activity.PlayerActivity;
import com.chat.android.im.bean.ChatMessage;
import com.chat.android.im.bean.MsgBody;
import com.chat.android.im.bean.MsgSendStatus;
import com.chat.android.im.bean.MsgStatus;
import com.chat.android.im.bean.MsgType;
import com.chat.android.im.config.RLS;
import com.chat.android.im.config.UnifyUiConfig;
import com.chat.android.im.helper.ImageHelper;
import com.chat.android.im.utils.DateUtils;
import com.chat.android.im.utils.IMUtilsKt;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.chat.android.im.utils.IMUtilsKt.attachmentTitle;
import static com.chat.android.im.utils.IMUtilsKt.parseColor;

public class ChatAdapter extends BaseDelegateMultiAdapter<ChatMessage, BaseViewHolder> {


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

    private UnifyUiConfig mUiConfig = RLS.getInstance().getUiConfig();


    public ChatAdapter(Context context, List<ChatMessage> data) {
        super(data);
        setMultiTypeDelegate(new BaseMultiTypeDelegate<ChatMessage>() {
            @Override
            public int getItemType(@NotNull List<? extends ChatMessage> list, int i) {
                ChatMessage entity = list.get(i);
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
        getMultiTypeDelegate().addItemType(TYPE_SEND_TEXT, SEND_TEXT)
                .addItemType(TYPE_RECEIVE_TEXT, RECEIVE_TEXT)
                .addItemType(TYPE_SEND_IMAGE, SEND_IMAGE)
                .addItemType(TYPE_RECEIVE_IMAGE, RECEIVE_IMAGE)
                .addItemType(TYPE_SEND_VIDEO, SEND_VIDEO)
                .addItemType(TYPE_RECEIVE_VIDEO, RECEIVE_VIDEO)
                .addItemType(TYPE_SEND_FILE, SEND_FILE)
                .addItemType(TYPE_RECEIVE_FILE, RECEIVE_FILE)
                .addItemType(TYPE_SEND_AUDIO, SEND_AUDIO)
                .addItemType(TYPE_RECEIVE_AUDIO, RECEIVE_AUDIO);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatMessage item) {
        setContent(helper, item);
        setStatus(helper, item);
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
        } else if (item.getMsgType().equals(MsgType.VIDEO)) {
            setVideoShow(helper, item);
        } else if (item.getMsgType().equals(MsgType.FILE)) {
            setFileShow(helper, item);
        } else if (item.getMsgType().equals(MsgType.AUDIO)) {
//            AudioMsgBody msgBody = (AudioMsgBody) item.getBody();
//            helper.setText(R.id.tvDuration, msgBody.getDuration() + "\"");
        }
    }

    private void setFileShow(BaseViewHolder helper, ChatMessage item) {
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

        helper.setText(R.id.msg_tv_file_name, msgBody.getAttachments()[0].getTitle());
        helper.setText(R.id.msg_tv_file_description, msgBody.getAttachments()[0].getDescription());

    }

    private void setVideoShow(BaseViewHolder helper, ChatMessage item) {
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

        TextView description = helper.getView(R.id.file_description);
        String descriptionText = msgBody.getAttachments()[0].getDescription();
        if (descriptionText == null || descriptionText.isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(descriptionText);
            description.setVisibility(View.VISIBLE);
        }

        String videoUrl = IMUtilsKt.attachmentUrl(msgBody.getAttachments()[0].getVideo_url());
        ImageView videoThumbnails = helper.getView(R.id.image_attachment);
        RequestOptions options = new RequestOptions().frame(1);
        Glide.with(videoThumbnails).load(videoUrl).apply(options).into(videoThumbnails);

        FrameLayout viewAttachment = helper.getView(R.id.video_attachment);
        viewAttachment.setOnClickListener(v -> {
            if (videoUrl != null && !videoUrl.isEmpty()) {
                PlayerActivity.Companion.play(v.getContext(), videoUrl);
            }
        });


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

        TextView description = helper.getView(R.id.file_description);
        String descriptionText = "";
        if (item.getMsgStatus() == MsgStatus.RECEIVE) {
            descriptionText = msgBody.getAttachments()[0].getDescription();
        } else {
            descriptionText = msgBody.getMessage();
        }


        if (descriptionText == null || descriptionText.isEmpty()) {
            description.setVisibility(View.GONE);
        } else {
            description.setText(descriptionText);
            description.setVisibility(View.VISIBLE);
        }


        SimpleDraweeView simpleDraweeView = helper.getView(R.id.image_attachment);
        String url = IMUtilsKt.attachmentUrl(msgBody.getAttachments()[0].getImage_url());
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .setOldController(simpleDraweeView.getController())
                .build();
        simpleDraweeView.setController(controller);
        simpleDraweeView.setOnClickListener(v -> ImageHelper.INSTANCE.openImage(
                v.getContext(),
                url,
                String.valueOf(attachmentTitle(msgBody.getAttachments()[0].getTitle(), url))));
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

}
