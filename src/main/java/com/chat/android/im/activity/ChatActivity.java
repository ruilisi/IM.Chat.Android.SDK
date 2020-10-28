package com.chat.android.im.activity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chat.android.im.R;
import com.chat.android.im.adapter.ChatAdapter;
import com.chat.android.im.bean.ChatMessage;
import com.chat.android.im.bean.MsgStatus;
import com.chat.android.im.config.RLS;
import com.chat.android.im.config.UnifyUiConfig;
import com.chat.android.im.database.DBInstance;
import com.chat.android.im.databinding.ActivityChatBinding;
import com.chat.android.im.helper.IChatMessage;
import com.chat.android.im.utils.ChatUiHelper;
import com.chat.android.im.utils.NetworkListener;
import com.chat.android.im.utils.StatusBarUtil;
import com.chat.android.im.viewmodel.ChatViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.chat.android.im.config.RLS.getEmpty;
import static com.chat.android.im.utils.IMUtilsKt.isIMNull;
import static com.chat.android.im.utils.IMUtilsKt.parseColor;

/**
 * Created by Ryan on 2020/10/11.
 */
public class ChatActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String KEY_USERID = "KEY_USERID";
    public static long localMessageCount = 0L;

    private ActivityChatBinding mBinding;
    private ChatViewModel mViewModel;
    private ChatAdapter mAdapter;

    private View.OnLayoutChangeListener onLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
        if (bottom < oldBottom && mBinding != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.rvChatList.getLayoutManager();
            int count = mAdapter.getItemCount();
            mBinding.rvChatList.post(() -> {
                layoutManager.scrollToPositionWithOffset(count - 1, 0);
                mBinding.rvChatList.post(() -> {
                    View target = layoutManager.findViewByPosition(count - 1);
                    if (target != null) {
                        int minus = mBinding.rvChatList.getMeasuredHeight() - target.getMeasuredHeight();
                        layoutManager.scrollToPositionWithOffset(count - 1, minus);
                    }
//                    mBinding?.rvChatList?.post{
//                        if (count > 0) {
//                            mBinding?.rvChatList?.smoothScrollToPosition(count)
//                        }
//                    }
                });
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isIMNull()) {
            showToast(R.string.data_is_null);
            finish();
            return;
        } else {
            initContent();
            observerData();
            mViewModel.startConnect();
            registerNetwork();
        }
    }

    private void registerNetwork() {
        NetworkListener.Companion.getInstance().registerNetWorkListener(this, type -> {
            runOnUiThread(() -> mViewModel.startConnect());
        });
    }

    private void setConfigUi() {
        UnifyUiConfig mUiConfig = RLS.getInstance().getUiConfig();
        StatusBarUtil.INSTANCE.setStatusColor(this, mUiConfig.getStatusBarColor());

        if (mUiConfig.getBackgroundRes() == 0) {
            mBinding.getRoot().setBackgroundColor(parseColor(this, mUiConfig.getBackgroundColor()));
        } else {
            mBinding.getRoot().setBackgroundResource(mUiConfig.getBackgroundRes());
        }

        mBinding.etContent.setBackgroundResource(mUiConfig.getEditTextBackgroundRes());
        mBinding.etContent.setTextColor(parseColor(this, mUiConfig.getEditTextColor()));
        mBinding.sendArea.setBackgroundColor(parseColor(this, mUiConfig.getBottomBackgroundColor()));
        mBinding.btnSend.setTextColor(parseColor(this, mUiConfig.getSendBtnTextColor()));
        mBinding.btnSend.setBackgroundResource(mUiConfig.getSendBtnBackgroundRes());

        if (mUiConfig.isHideNavigation()) {
            mBinding.titleView.commonToolbar.setVisibility(View.GONE);
        } else {
            mBinding.titleView.imvBack.setImageDrawable(getResources().getDrawable(mUiConfig.getNavigationIcon(), null));
            if (mUiConfig.getBackIconWidth() != null) {
                mBinding.titleView.imvBack.getLayoutParams().width = mUiConfig.getBackIconWidth();
            }

            if (mUiConfig.getBackIconHeight() != null) {
                mBinding.titleView.imvBack.getLayoutParams().height = mUiConfig.getBackIconHeight();
            }

            mBinding.titleView.commonToolbar.
                    setBackgroundColor(parseColor(this, mUiConfig.getNavigationBackgroundColor()));

            mBinding.titleView.commonToolbar.getLayoutParams().height = mUiConfig.getNavHeight();
            mViewModel.setTitle(mUiConfig.getTitle());
            mBinding.titleView.commonToolbarTitle.
                    setTextColor(parseColor(this, mUiConfig.getTitleColor()));

            if (mUiConfig.isNavTitleBold()) {
                mBinding.titleView.commonToolbarTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            } else {
                mBinding.titleView.commonToolbarTitle.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            mBinding.titleView.commonToolbarTitle.
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, mUiConfig.getNavTitleSize());
        }
    }

    private void observerData() {

        mViewModel.getChatMessageData().observe(this, chatMessage -> {
            initChatMsgView(chatMessage);
        });

        mViewModel.getChatListMessageData().observe(this, chatMessages -> {
            initChatMsgListView(chatMessages, true);
        });

        mViewModel.getChatMissedListMessageData().observe(this, chatMessages -> {
            initMissedChatMsgListView(chatMessages);
        });

        mViewModel.getConnectState().observe(this, integer -> {

            if (integer == ChatViewModel.Companion.getERROR()) {
                finish();
            } else if (integer == ChatViewModel.Companion.getCONNECTING()) {
                if (mAdapter.getData().isEmpty()) {
                    mBinding.swipeChat.setRefreshing(true);
                }
            } else if (integer == ChatViewModel.Companion.getCONNECTED()) {

            }

        });
    }

    private void initChatMsgView(ChatMessage chatMessage) {
        mAdapter.addData(chatMessage);
        if (chatMessage.getMsgStatus() == MsgStatus.SEND || mViewModel.isScrollToBottom()) {
            mBinding.rvChatList.scrollToPosition(mAdapter.getItemCount() - 1);
            mViewModel.setScrollToBottom(true);
        }
    }

    private void initChatMsgListView(List<ChatMessage> chatMessageList, Boolean dismissRefreshLoading) {
        if (chatMessageList != null && !chatMessageList.isEmpty()) {
            mAdapter.addData(0, chatMessageList);
            if (mViewModel.getFirstRefresh()) {
                mViewModel.setFirstRefresh(false);
                mBinding.rvChatList.scrollToPosition(mAdapter.getItemCount() - 1);
                mViewModel.setScrollToBottom(true);
            }
            mViewModel.setNeeShowWelecome(false);
        }
        if (dismissRefreshLoading) {
            mBinding.swipeChat.setRefreshing(false);
        }
    }

    private void initMissedChatMsgListView(List<ChatMessage> chatMessageList) {
        if (chatMessageList != null && !chatMessageList.isEmpty()) {
            mAdapter.addData(chatMessageList);
            mBinding.rvChatList.scrollToPosition(mAdapter.getItemCount() - 1);

            mViewModel.setNeeShowWelecome(false);
            mBinding.swipeChat.setRefreshing(false);
        }
    }

    private void initContent() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        mBinding.setViewModel(mViewModel);
        mBinding.setLifecycleOwner(this);

        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preference.getString(KEY_USERID, getEmpty()).equals(RLS.getInstance().getDataConfig().getId())) {
            preference.edit().putString(KEY_USERID, RLS.getInstance().getDataConfig().getId()).apply();
            DBInstance.Companion.getInstance().getChatMessageDao().delete(DBInstance.Companion.getInstance().getChatMessageDao().loadAll());
        }

        setConfigUi();
        mAdapter = new ChatAdapter(this, new ArrayList());
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);
        mBinding.rvChatList.setLayoutManager(mLinearLayout);
        mBinding.rvChatList.setAdapter(mAdapter);
        mBinding.swipeChat.setOnRefreshListener(this);
        initChatUi();

        localMessageCount = DBInstance.Companion.getInstance().getChatMessageDao().loadMessageCount();
        mBinding.swipeChat.setRefreshing(true);
        mViewModel.loadLocalHistoryMsg(null);

    }

    private void initChatUi() {

        mBinding.rvChatList.setItemAnimator(null);

        ChatUiHelper mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mBinding.llContent)
                .bindttToSendButton(mBinding.btnSend)
                .bindEditText(mBinding.etContent)
                .bindBottomLayout(mBinding.bottomLayout);
//                .bindEmojiLayout(mBinding?.rlEmotion?.llEmoji)
//                .bindAddLayout(mBinding?.llAdd?.llAdd)
//                .bindToAddButton(mBinding?.ivAdd)
//                .bindToEmojiButton(mBinding?.ivEmo)
//                .bindAudioBtn(mBinding?.btnAudio)
//                .bindAudioIv(mBinding?.ivAudio)
//                .bindEmojiData()

        //底部布局弹出,聊天列表上滑
        mBinding.rvChatList.addOnLayoutChangeListener(onLayoutChangeListener);
        //点击空白区域关闭键盘
        mBinding.rvChatList.setOnTouchListener((v, event) -> {
            mUiHelper.hideBottomLayout(false);
            mUiHelper.hideSoftInput();
            mBinding.etContent.clearFocus();
//            mBinding?.ivEmo?.setImageResource(R.drawable.ic_emoji)
            return false;
        });

        //发送
        mBinding.btnSend.setOnClickListener(v -> {
            String content = mBinding.etContent.getText().toString().trim();
            mViewModel.clickSendMsg(content);
            mBinding.etContent.setText(getEmpty());
        });
        mBinding.titleView.commonToolbarBack.setOnClickListener(v -> finish());


        mViewModel.getRecycleViewScrollHelper().setCheckIfItemViewFullRecycleViewForBottom(true);
        mViewModel.getRecycleViewScrollHelper().setCheckIfItemViewFullRecycleViewForTop(true);
        mViewModel.getRecycleViewScrollHelper().setCheckScrollToTopBottomTogether(true);

        mBinding.rvChatList.addOnScrollListener(mViewModel.getRecycleViewScrollHelper());

        mViewModel.setIChatmessage(new IChatMessage() {
            @org.jetbrains.annotations.Nullable
            @Override
            public List<ChatMessage> getItemList() {
                return mAdapter.getData();
            }

            @Override
            public void notifyItemChanged(int position) {
                mAdapter.notifyItemChanged(position);
            }

            @Override
            public void initChatMsgListView(@NotNull List<ChatMessage> chatMessageList, boolean dismissRefreshLoading) {
                ChatActivity.this.initChatMsgListView(chatMessageList, dismissRefreshLoading);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.closeConnect();
            mBinding.rvChatList.removeOnScrollListener(mViewModel.getRecycleViewScrollHelper());
            mBinding.rvChatList.removeOnLayoutChangeListener(onLayoutChangeListener);
            mViewModel.setIChatmessage(null);
            NetworkListener.Companion.getInstance().unRegisterNetWorkListener(this);
            mBinding = null;
        }
    }

    private void showToast(int toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        mBinding.swipeChat.setRefreshing(true);
        List<ChatMessage> dataList = mAdapter.getData();
        if (dataList == null || dataList.isEmpty()) {
            mViewModel.loadHistoryMessage(String.valueOf(System.currentTimeMillis()), RLS.getInstance().getDataConfig().getPreLoadHistoryCount());
        } else {
            if (mViewModel.getLoadFromLocalFinished()) {
                mViewModel.loadHistoryMessage(String.valueOf(dataList.get(0).getTs().getDate()), RLS.getInstance().getDataConfig().getPreLoadHistoryCount());
            } else {
                mViewModel.loadLocalHistoryMsg(dataList.get(0));
            }
        }
    }
}
