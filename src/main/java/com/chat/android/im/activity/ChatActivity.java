package com.chat.android.im.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chat.android.im.R;
import com.chat.android.im.adapter.ChatAdapter;
import com.chat.android.im.bean.ChatMessage;
import com.chat.android.im.bean.MsgStatus;
import com.chat.android.im.config.RLS;
import com.chat.android.im.config.UnifyUiConfig;
import com.chat.android.im.database.DBInstance;
import com.chat.android.im.databinding.ActivityChatBinding;
import com.chat.android.im.helper.AndroidPermissionsHelper;
import com.chat.android.im.helper.IChatMessage;
import com.chat.android.im.helper.ImageHelper;
import com.chat.android.im.utils.CancelStrategy;
import com.chat.android.im.utils.ChatUiHelper;
import com.chat.android.im.utils.DialogKt;
import com.chat.android.im.utils.GlideEngine;
import com.chat.android.im.utils.NetworkListener;
import com.chat.android.im.utils.PictureSelectorEngineImp;
import com.chat.android.im.utils.StatusBarUtil;
import com.chat.android.im.utils.UiKt;
import com.chat.android.im.utils.UriInteractor;
import com.chat.android.im.viewmodel.ChatViewModel;
import com.facebook.drawee.backends.pipeline.DraweeConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.app.IApp;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.PictureSelectorEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.squareup.moshi.Moshi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.chat.android.im.config.RLS.getEmpty;
import static com.chat.android.im.utils.IMUtilsKt.isIMNull;
import static com.chat.android.im.utils.IMUtilsKt.parseColor;
import static com.chat.android.im.viewmodel.ChatViewModel.CONNECTED;
import static com.chat.android.im.viewmodel.ChatViewModel.CONNECTING;
import static com.chat.android.im.viewmodel.ChatViewModel.ERROR;
import static com.chat.android.im.viewmodel.ChatViewModel.REQUEST_CODE_FOR_PERFORM_CAMERA;
import static com.chat.android.im.viewmodel.ChatViewModel.REQUEST_CODE_FOR_PERFORM_SAF;

/**
 * Created by Ryan on 2020/10/11.
 */
public class ChatActivity extends AppCompatActivity implements IApp, SwipeRefreshLayout.OnRefreshListener {

    public static final String KEY_USERID = "KEY_USERID";
    public static long localMessageCount = 0L;

    private ActivityChatBinding mBinding;
    private ChatViewModel mViewModel;
    private ChatAdapter mAdapter;
    public CancelStrategy strategy;
    public String citation;
    public UriInteractor uriInteractor;
    public OkHttpClient okHttpClient;
    public Moshi moshi;
    private ChatUiHelper mUiHelper;

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

        Glide.with(this).load(R.drawable.ic_add_default).into(mBinding.ivAdd);
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

            if (integer == ERROR) {
                finish();
            } else if (integer == CONNECTING) {
                if (mAdapter.getData().isEmpty()) {
                    mBinding.swipeChat.setRefreshing(true);
                }
            } else if (integer == CONNECTED) {

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
        strategy = new CancelStrategy(this, mViewModel.getJob());
        okHttpClient = provideOkHttpClient();
        uriInteractor = new UriInteractor(getApplicationContext());
        moshi = new Moshi.Builder().build();
        setupFresco();
        AndroidThreeTen.init(this);
//        PictureAppMaster.getInstance().setApp(this);
//        PictureSelectorCrashUtils.init((t, e) -> {
//            // Crash之后的一些操作可再此处理，没有就忽略...
//        });

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

        mBinding.llAdd.rlCamera.setOnClickListener(v ->
                {
                    mUiHelper.hideBottomLayout(false);
                    mUiHelper.hideSoftInput();
                    mBinding.etContent.clearFocus();
                    mViewModel.openCamera(ChatActivity.this);
                }
        );

        mBinding.llAdd.rlPhoto.setOnClickListener(v ->
                {
                    mUiHelper.hideBottomLayout(false);
                    mUiHelper.hideSoftInput();
                    mBinding.etContent.clearFocus();
                    openPhoto();
                }
        );

    }

    private void initChatUi() {

        mBinding.rvChatList.setItemAnimator(null);

        mUiHelper = ChatUiHelper.with(this);
        mUiHelper.bindContentLayout(mBinding.llContent)
                .bindttToSendButton(mBinding.llSend)
                .bindEditText(mBinding.etContent)
                .bindBottomLayout(mBinding.bottomLayout)
                .bindToAddButton(mBinding.ivAdd)
                .bindAddLayout(mBinding.llAdd.llAdd);
//                .bindEmojiLayout(mBinding?.rlEmotion?.llEmoji)
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
        mBinding.llSend.setOnClickListener(v -> {
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

    private void setupFresco() {
        Fresco.initialize(this, provideImagePipelineConfig(getApplicationContext(), okHttpClient), DraweeConfig.newBuilder().build());
    }

    private ImagePipelineConfig provideImagePipelineConfig(Context context, OkHttpClient okHttpClient) {
        Set listeners = new HashSet();
        listeners.add(new RequestLoggingListener());
        return OkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient)
                .setRequestListeners(listeners)
                .setDownsampleEnabled(true)
                .experiment().setPartialImageCachingEnabled(true).build();
    }

    private OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AndroidPermissionsHelper.CAMERA_CODE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    mViewModel.dispatchTakePictureIntent(this);
                } else {
                    // permission denied
                    Snackbar.make(
                            mBinding.getRoot(),
                            R.string.msg_camera_permission_denied,
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
                break;
            }
            case AndroidPermissionsHelper.WRITE_EXTERNAL_STORAGE_CODE_IMAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    ImageHelper.INSTANCE.saveImage(this);
                } else {
                    // permission denied
                    Snackbar.make(
                            mBinding.getRoot(),
                            R.string.msg_storage_permission_denied,
                            Snackbar.LENGTH_SHORT
                    ).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FOR_PERFORM_CAMERA: {
                    if (mViewModel.getTakenPhotoUri() != null) {
                        DialogKt.showFileAttachmentDialog(ChatActivity.this, mViewModel.getTakenPhotoUri());
                    }
                }
                case REQUEST_CODE_FOR_PERFORM_SAF: {
                }
            }
        }
    }

    public void openPhoto() {

        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .selectionMode(PictureConfig.SINGLE)
                .isPageStrategy(false)
                .isWeChatStyle(true)
                .isCamera(false)
                .setLanguage(LanguageConfig.ENGLISH)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        if (result != null && result.size() == 1) {
                            LocalMedia localMedia = result.get(0);
                            String path = "";
                            if (TextUtils.isEmpty(path)) {
                                path = localMedia.getAndroidQToPath();
                            }
                            if (TextUtils.isEmpty(path)) {
                                path = localMedia.getPath();
                            }
                            if (!TextUtils.isEmpty(path)) {
                                DialogKt.showFileAttachmentDialog(ChatActivity.this, Uri.parse(localMedia.getRealPath()));
                            } else {
                                showMessage(R.string.app_name);
                            }
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    public void showLoading() {
        UiKt.ui(this, activity -> {
            mBinding.viewLoading.setVisibility(View.VISIBLE);
            return null;
        });
    }

    public void hideLoading() {
        UiKt.ui(this, activity -> {
            mBinding.viewLoading.setVisibility(View.GONE);
            return null;
        });
    }

    public void showMessage(String message) {
        UiKt.ui(this, activity -> {
            showToast(message);
            return null;
        });
    }

    public void showMessage(int resId) {
        UiKt.ui(this, activity -> {
            showToast(resId);
            return null;
        });
    }

    public void showMessage(Exception ex) {
        if (ex.getMessage() == null || ex.getMessage().isEmpty()) {
            showGenericErrorMessage();
        } else {
            showMessage(ex.getMessage());
        }
    }

    public void showGenericErrorMessage() {
        UiKt.ui(this, activity -> {
            showMessage(getString(R.string.msg_generic_error));
            return null;
        });
    }

    public void showInvalidFileMessage() {
        UiKt.ui(this, activity -> {
            showMessage(getString(R.string.msg_invalid_file));
            return null;
        });
    }

    public void showInvalidFileSize(int fileSize, int maxFileSize) {
        UiKt.ui(this, activity -> {
            showMessage(getString(R.string.max_file_size_exceeded, fileSize, maxFileSize));
            return null;
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public PictureSelectorEngine getPictureSelectorEngine() {
        return new PictureSelectorEngineImp();
    }
}
