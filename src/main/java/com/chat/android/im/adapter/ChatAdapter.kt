package com.chat.android.im.adapter

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.android.im.R
import com.chat.android.im.activity.PlayerActivity.Companion.play
import com.chat.android.im.bean.ChatMessage
import com.chat.android.im.bean.MsgSendStatus
import com.chat.android.im.bean.MsgStatus
import com.chat.android.im.bean.MsgType
import com.chat.android.im.config.RLS.Companion.getInstance
import com.chat.android.im.helper.ImageHelper.openImage
import com.chat.android.im.utils.DateUtils
import com.chat.android.im.utils.attachmentTitle
import com.chat.android.im.utils.attachmentUrl
import com.chat.android.im.utils.parseColor
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView

class ChatAdapter(data: MutableList<ChatMessage?>?) : BaseDelegateMultiAdapter<ChatMessage?, BaseViewHolder>(data) {
    /*
    private static final int SEND_LOCATION = R.layout.item_location_send;
    private static final int RECEIVE_LOCATION = R.layout.item_location_receive;*/
    private val mUiConfig = getInstance().getUiConfig()

    override fun convert(helper: BaseViewHolder, item: ChatMessage?) {
        setContent(helper, item)
        setStatus(helper, item)
    }

    private fun setStatus(helper: BaseViewHolder, item: ChatMessage?) {
        val msgType = item?.msgType
        if (msgType === MsgType.TEXT || msgType === MsgType.AUDIO || msgType === MsgType.VIDEO || msgType === MsgType.FILE) {
            //只需要设置自己发送的状态
            val sentStatus = item.sentStatus
            val isSend = item.msgStatus === MsgStatus.SEND
            if (isSend) {
                if (sentStatus === MsgSendStatus.SENDING) {
                    helper.setVisible(R.id.chat_item_progress, true).setVisible(R.id.chat_item_fail, false)
                } else if (sentStatus === MsgSendStatus.FAILED) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true)
                } else if (sentStatus === MsgSendStatus.SENT) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false)
                }
            }
        } else if (msgType === MsgType.IMAGE) {
            val isSend = item.msgStatus === MsgStatus.SEND
            if (isSend) {
                val sentStatus = item.sentStatus
                if (sentStatus === MsgSendStatus.SENDING) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false)
                } else if (sentStatus === MsgSendStatus.FAILED) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, true)
                } else if (sentStatus === MsgSendStatus.SENT) {
                    helper.setVisible(R.id.chat_item_progress, false).setVisible(R.id.chat_item_fail, false)
                }
            }
        }
    }

    private fun setContent(helper: BaseViewHolder, item: ChatMessage?) {
        if (item?.msgType == MsgType.TEXT) {
            setTextMsgShow(helper, item)
        } else if (item?.msgType == MsgType.IMAGE) {
            setImageMsgShow(helper, item)
        } else if (item?.msgType == MsgType.VIDEO) {
            setVideoShow(helper, item)
        } else if (item?.msgType == MsgType.FILE) {
            setFileShow(helper, item)
        } else if (item?.msgType == MsgType.AUDIO) {
//            AudioMsgBody msgBody = (AudioMsgBody) item.getBody();
//            helper.setText(R.id.tvDuration, msgBody.getDuration() + "\"");
        }
    }

    private fun setFileShow(helper: BaseViewHolder, item: ChatMessage) {
        val msgBody = item.msgBody
        val timeView = helper.getView<TextView>(R.id.item_tv_time)
        if (item.timeShow.date == 0L) {
            timeView.visibility = View.GONE
        } else {
            timeView.visibility = View.VISIBLE
            timeView.setTextColor(parseColor(timeView.context, mUiConfig.timeColor))
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.timeSize.toFloat())
            timeView.text = parseTime(item.timeShow.date)
        }
        helper.setText(R.id.msg_tv_file_name, msgBody.attachments!![0].title)
        helper.setText(R.id.msg_tv_file_description, msgBody.attachments!![0].description)
    }

    private fun setVideoShow(helper: BaseViewHolder, item: ChatMessage) {
        val msgBody = item.msgBody
        val timeView = helper.getView<TextView>(R.id.item_tv_time)
        if (item.timeShow.date == 0L) {
            timeView.visibility = View.GONE
        } else {
            timeView.visibility = View.VISIBLE
            timeView.setTextColor(parseColor(timeView.context, mUiConfig.timeColor))
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.timeSize.toFloat())
            timeView.text = parseTime(item.timeShow.date)
        }
        val description = helper.getView<TextView>(R.id.file_description)
        val descriptionText = msgBody.attachments!![0].description
        if (descriptionText == null || descriptionText.isEmpty()) {
            description.visibility = View.GONE
        } else {
            description.text = descriptionText
            description.visibility = View.VISIBLE
        }
        val videoUrl = attachmentUrl(msgBody.attachments!![0].video_url)
        val videoThumbnails = helper.getView<ImageView>(R.id.image_attachment)
        val options = RequestOptions().frame(1)
        Glide.with(videoThumbnails).load(videoUrl).apply(options).into(videoThumbnails)
        val viewAttachment = helper.getView<FrameLayout>(R.id.video_attachment)
        viewAttachment.setOnClickListener { v: View ->
            if (videoUrl != null && !videoUrl.isEmpty()) {
                play(v.context, videoUrl)
            }
        }
    }

    private fun setImageMsgShow(helper: BaseViewHolder, item: ChatMessage) {
        val msgBody = item.msgBody
        val timeView = helper.getView<TextView>(R.id.item_tv_time)
        if (item.timeShow.date == 0L) {
            timeView.visibility = View.GONE
        } else {
            timeView.visibility = View.VISIBLE
            timeView.setTextColor(parseColor(timeView.context, mUiConfig.timeColor))
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.timeSize.toFloat())
            timeView.text = parseTime(item.timeShow.date)
        }
        val description = helper.getView<TextView>(R.id.file_description)
        var descriptionText: String? = ""
        descriptionText = if (item.msgStatus === MsgStatus.RECEIVE) {
            msgBody.attachments!![0].description
        } else {
            msgBody.message
        }
        if (descriptionText == null || descriptionText.isEmpty()) {
            description.visibility = View.GONE
        } else {
            description.text = descriptionText
            description.visibility = View.VISIBLE
        }
        val simpleDraweeView = helper.getView<SimpleDraweeView>(R.id.image_attachment)
        val url = attachmentUrl(msgBody.attachments!![0].image_url)
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(url)
                .setAutoPlayAnimations(true)
                .setOldController(simpleDraweeView.controller)
                .build()
        simpleDraweeView.controller = controller
        simpleDraweeView.setOnClickListener { v: View ->
            openImage(
                    v.context,
                    url!!, attachmentTitle(msgBody.attachments!![0].title, url).toString())
        }
    }

    private fun setTextMsgShow(helper: BaseViewHolder, item: ChatMessage) {
        val msgBody = item.msgBody
        val timeView = helper.getView<TextView>(R.id.item_tv_time)
        if (item.timeShow.date == 0L) {
            timeView.visibility = View.GONE
        } else {
            timeView.visibility = View.VISIBLE
            timeView.setTextColor(parseColor(timeView.context, mUiConfig.timeColor))
            timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.timeSize.toFloat())
            timeView.text = parseTime(item.timeShow.date)
        }
        val contentText = helper.getView<TextView>(R.id.chat_item_content_text)
        contentText.text = msgBody.message
        if (item.msgStatus === MsgStatus.SEND) {
            contentText.setTextColor(parseColor(contentText.context, mUiConfig.sendTextColor))
            contentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.sendTextSize.toFloat())
            contentText.setBackgroundResource(mUiConfig.sendBackgroundRes)
        } else {
            contentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.receiveTextSize.toFloat())
            contentText.setTextColor(parseColor(contentText.context, mUiConfig.receiveTextColor))
            contentText.setBackgroundResource(mUiConfig.receiveBackgroundRes)
        }
    }

    private fun parseTime(time: Long): String {
        if (time == null || time == 0L) {
            return ""
        }
        return if (DateUtils.isToday(time)) {
            DateUtils.getHourMinuteOfDate(time)
        } else if (DateUtils.isYear(time)) {
            DateUtils.getDateTimeStr1(time)
        } else {
            DateUtils.getDateTimeStr2(time)
        }
    }

    companion object {
        private const val TYPE_SEND_TEXT = 1
        private const val TYPE_RECEIVE_TEXT = 2
        private const val TYPE_SEND_IMAGE = 3
        private const val TYPE_RECEIVE_IMAGE = 4
        private const val TYPE_SEND_VIDEO = 5
        private const val TYPE_RECEIVE_VIDEO = 6
        private const val TYPE_SEND_FILE = 7
        private const val TYPE_RECEIVE_FILE = 8
        private const val TYPE_SEND_AUDIO = 9
        private const val TYPE_RECEIVE_AUDIO = 10
        private val SEND_TEXT = R.layout.item_text_send
        private val RECEIVE_TEXT = R.layout.item_text_receive
        private val SEND_IMAGE = R.layout.item_image_send
        private val RECEIVE_IMAGE = R.layout.item_image_receive
        private val SEND_VIDEO = R.layout.item_video_send
        private val RECEIVE_VIDEO = R.layout.item_video_receive
        private val SEND_FILE = R.layout.item_file_send
        private val RECEIVE_FILE = R.layout.item_file_receive
        private val RECEIVE_AUDIO = R.layout.item_audio_receive
        private val SEND_AUDIO = R.layout.item_audio_send
    }

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<ChatMessage?>() {
            override fun getItemType(list: List<ChatMessage?>, i: Int): Int {
                val entity = list[i]
                val isSend = entity?.msgStatus === MsgStatus.SEND
                if (MsgType.TEXT === entity?.msgType) {
                    return if (isSend) TYPE_SEND_TEXT else TYPE_RECEIVE_TEXT
                } else if (MsgType.IMAGE === entity?.msgType) {
                    return if (isSend) TYPE_SEND_IMAGE else TYPE_RECEIVE_IMAGE
                } else if (MsgType.VIDEO === entity?.msgType) {
                    return if (isSend) TYPE_SEND_VIDEO else TYPE_RECEIVE_VIDEO
                } else if (MsgType.FILE === entity?.msgType) {
                    return if (isSend) TYPE_SEND_FILE else TYPE_RECEIVE_FILE
                } else if (MsgType.AUDIO === entity?.msgType) {
                    return if (isSend) TYPE_SEND_AUDIO else TYPE_RECEIVE_AUDIO
                }
                return 0
            }

        })

        getMultiTypeDelegate()!!.addItemType(TYPE_SEND_TEXT, SEND_TEXT)
                .addItemType(TYPE_RECEIVE_TEXT, RECEIVE_TEXT)
                .addItemType(TYPE_SEND_IMAGE, SEND_IMAGE)
                .addItemType(TYPE_RECEIVE_IMAGE, RECEIVE_IMAGE)
                .addItemType(TYPE_SEND_VIDEO, SEND_VIDEO)
                .addItemType(TYPE_RECEIVE_VIDEO, RECEIVE_VIDEO)
                .addItemType(TYPE_SEND_FILE, SEND_FILE)
                .addItemType(TYPE_RECEIVE_FILE, RECEIVE_FILE)
                .addItemType(TYPE_SEND_AUDIO, SEND_AUDIO)
                .addItemType(TYPE_RECEIVE_AUDIO, RECEIVE_AUDIO)
    }
}