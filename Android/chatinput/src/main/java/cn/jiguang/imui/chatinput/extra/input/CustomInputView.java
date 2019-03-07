package cn.jiguang.imui.chatinput.extra.input;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import cn.jiguang.imui.chatinput.R;
import cn.jiguang.imui.chatinput.emoji.Constants;
import cn.jiguang.imui.chatinput.emoji.EmojiBean;
import cn.jiguang.imui.chatinput.emoji.EmojiView;
import cn.jiguang.imui.chatinput.emoji.EmoticonsKeyboardUtils;
import cn.jiguang.imui.chatinput.emoji.data.EmoticonEntity;
import cn.jiguang.imui.chatinput.emoji.listener.EmoticonClickListener;
import cn.jiguang.imui.chatinput.emoji.widget.EmoticonsEditText;
import cn.jiguang.imui.chatinput.listener.CustomMenuEventListener;
import cn.jiguang.imui.chatinput.listener.OnClickEditTextListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.menu.Menu;
import cn.jiguang.imui.chatinput.menu.view.MenuFeature;
import cn.jiguang.imui.chatinput.menu.view.MenuItem;
import cn.jiguang.imui.chatinput.utils.SimpleCommonUtils;

public class CustomInputView extends LinearLayout
        implements View.OnClickListener, TextWatcher, ViewTreeObserver.OnPreDrawListener {

    public static final int MULTI_MODE = 0x1;
    public static final int SEND_MODE = 0x2;

    private static final String TAG = "ChatInputView";
    private EmoticonsEditText mChatInput;
    private CharSequence mInput;
    /**
     * 发送相关
     */
    private TextView sendBtn;
    private OnSendClickListener sendClickListener;
    private OnSendTouchListener onSendTouchListener;
    private OnInputModeChangeListener onInputModeChangeListener;

    private LinearLayout mChatInputContainer;
    private LinearLayout mMenuItemContainer;
    private FrameLayout mMenuContainer;


    private OnMenuClickListener mListener;


    private OnClickEditTextListener mEditTextListener;

    private EmojiView mEmojiRl;

    private InputMethodManager mImm;
    private Window mWindow;

    private int mHeight;
    private int mSoftKeyboardHeight;
    public static int sMenuHeight = 831;
    private boolean mPendingShowMenu;


    private boolean mIsEarPhoneOn;

    private Context mContext;
    private Rect mRect = new Rect();


    private View mEmojiBtnContainer;
    private View quickRecoderContainer;
    private TextView voiceQuickTimeLeft;

    private CustomMenuManager mMenuManager;
    /**
     * 录音面板是否已经打开
     */
    private boolean isRecorderMenuFeatureVisiable = false;
    private boolean isPhtotMenuFeatureVisiable = false;
    /**
     * 是否是快速录制状态
     */
    boolean isShowQuickRecorderMode = false;

    private TextView qcTipView;

    public CustomInputView(Context context) {
        super(context);
        init(context);
    }

    public CustomInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context) {
        mContext = context;
        inflate(context, R.layout.im_view_custom_chatinput, this);

        sendBtn = findViewById(R.id.tv_send);
        sendBtn.setVisibility(GONE);
        sendBtn.setOnClickListener(this);

        mChatInputContainer = (LinearLayout) findViewById(R.id.aurora_ll_input_container);
        mMenuItemContainer = (LinearLayout) findViewById(R.id.aurora_ll_menuitem_container);
        mMenuContainer = (FrameLayout) findViewById(R.id.aurora_fl_menu_container);

        quickRecoderContainer = findViewById(R.id.quick_recorder);
        qcTipView = findViewById(R.id.tv_qc_tip);


        mMenuManager = new CustomMenuManager(this);

        // menu buttons
        mChatInput = (EmoticonsEditText) findViewById(R.id.aurora_et_chat_input);


        //btn
        mEmojiBtnContainer = findViewById(R.id.aurora_ll_menuitem_emoji_container);
        mEmojiBtnContainer.setOnClickListener(onMenuItemClickListener);

        mEmojiRl = (EmojiView) findViewById(R.id.aurora_rl_emoji_container);

        mMenuContainer.setVisibility(GONE);

        mChatInput.addTextChangedListener(this);
        mChatInput.setOnBackKeyClickListener(() -> {
            if (mMenuContainer.getVisibility() == VISIBLE) {
                dismissMenuLayout();
            } else if (isKeyboardVisible()) {
                EmoticonsKeyboardUtils.closeSoftKeyboard(mChatInput);
            }
        });

        mChatInput.setOnTouchListener((v, event) -> {
            Log.d("123", "onTouch event:" + event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (onSendTouchListener != null) {
                    onSendTouchListener.onEditTouch();
                }
                //自己控制键盘弹出
                showKeyBoard();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                performClick();
            }
            return false;
        });

        voiceQuickTimeLeft = findViewById(R.id.tv_quick_time);
        /**
         * about InputMethodManager
         */
        mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        mWindow = ((Activity) context).getWindow();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mHeight = dm.heightPixels;
        getViewTreeObserver().addOnPreDrawListener(this);
    }


    public void initCostomMenu() {
        CustomMenuManager menuManager = getMenuManager();
        menuManager.addCustomMenu("recorder", R.layout.im_menu_voice_item, R.layout.im_menu_voice_feature);
        menuManager.addCustomMenu("photo", R.layout.im_menu_photo_item, R.layout.im_menu_photo_feature);

        // Custom menu order
        menuManager.setMenu(Menu.newBuilder().
                customize(true).
                setBottom("recorder", "photo").build());
        menuManager.setCustomMenuClickListener(new CustomMenuEventListener() {
            @Override
            public boolean onMenuItemClick(String tag, MenuItem menuItem) {
                //Menu feature will not be show shown if return false；
                return true;
            }

            @Override
            public void onMenuFeatureVisibilityChanged(int visibility, String tag, MenuFeature menuFeature) {
                setRecorderMenuState(visibility, tag, 300);
                setPhotoMenuState(visibility, tag, 600);
            }
        });
    }


    public void setPhotoMenuState(int visibility, String tag, int customHeight) {
        if (visibility == View.VISIBLE) {
            // Menu feature is visible.
            if (tag.equals("photo")) {
                isPhtotMenuFeatureVisiable = true;
                View rootView = getMenuManager().getMenuItemCollection().get("photo");
                View menuRecorder = rootView.findViewById(R.id.tv_photo);
                menuRecorder.setBackgroundResource(R.drawable.im_input_menu_photo_press);

                if (customHeight > 0) {
                    setMenuContainerHeight(customHeight);
                }
            }
        } else {
            // Menu feature is gone.
            if (tag.equals("photo")) {
                isPhtotMenuFeatureVisiable = false;
                View rootView = getMenuManager().getMenuItemCollection().get("photo");
                View menuRecorder = rootView.findViewById(R.id.tv_photo);
                menuRecorder.setBackgroundResource(R.drawable.im_input_menu_img);
            }
        }
    }


    public void setRecorderMenuState(int visibility, String tag, int customHeight) {
        if (visibility == View.VISIBLE) {
            // Menu feature is visible.
            if (tag.equals("recorder")) {
                isRecorderMenuFeatureVisiable = true;
                View rootView = getMenuManager().getMenuItemCollection().get("recorder");
                View menuRecorder = rootView.findViewById(R.id.tv_voice_menu);
                menuRecorder.setBackgroundResource(R.drawable.im_input_menu_voice_press);
                if (customHeight > 0) {
                    setMenuContainerHeight(customHeight);
                }
            }
        } else {
            // Menu feature is gone.
            if (tag.equals("recorder")) {
                isRecorderMenuFeatureVisiable = false;
                View rootView = getMenuManager().getMenuItemCollection().get("recorder");
                View menuRecorder = rootView.findViewById(R.id.tv_voice_menu);
                menuRecorder.setBackgroundResource(R.drawable.im_input_menu_voice);
            }
        }
    }


    public void setRecorderQuickTouch(OnQuickRecorderListener onQuickRecorderListener) {
        View recorderMenu = getMenuManager().getMenuItemCollection().get("recorder");
        if (recorderMenu == null) {
            return;
        }
        recorderMenu.setOnTouchListener(new OnTouchListener() {
            int startX;
            int startY;
            long startTime;
            private Handler handler = new Handler();
            final int PRESSREANGE = 500;
            final int TOUCHRANGE = 20;
            /**
             * 是否移出到控件外
             */
            boolean isOutofTouchRange = false;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /**
                 * 如果录制页面已经打开，快捷录制将屏蔽
                 */
                if (isRecorderMenuFeatureVisiable) {
                    return false;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isOutofTouchRange = false;
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        startTime = event.getDownTime();

                        /* 长按操作 */
                        handler.postDelayed(() -> {
                            showQuickRecorderMode();
                            if (onQuickRecorderListener != null) {
                                onQuickRecorderListener.onStartRecorder();
                            }
                        }, PRESSREANGE);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        int newY = (int) event.getY();
                        Rect rect = new Rect();
                        v.getLocalVisibleRect(rect);
                        int bottom = rect.bottom + TOUCHRANGE;
                        /**
                         * 判断按下区域
                         * 1. 没有进入快速录制页，移除控件外外
                         * 2. 正在快速录制中，移出控件外
                         */
                        if (newY <= -TOUCHRANGE || newY >= bottom) {
                            handler.removeCallbacksAndMessages(null);
                            qcTipView.setText("松开    取消");
                            isOutofTouchRange = true;
                            if (onQuickRecorderListener != null) {
                                onQuickRecorderListener.onCancelRecorder();
                            }
                        } else {
                            //从控件外又恢复到控件内
                            if (isOutofTouchRange) {
                                if (onQuickRecorderListener != null) {
                                    onQuickRecorderListener.onResumeRecorder();
                                }
                                //在控件范围下移动
                                isOutofTouchRange = false;
                                qcTipView.setText("松开    发送");
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        handler.removeCallbacksAndMessages(null);
                        if (isShowQuickRecorderMode) {
                            showNormalMode();
                            if (onQuickRecorderListener != null) {
                                if (isOutofTouchRange) {
                                    onQuickRecorderListener.onCancelStopRecorder();
                                } else {
                                    onQuickRecorderListener.onStopRecorder();
                                }
                            }
                            return true;
                        }
                        performClick();
                        break;
                    default:
                }

                Log.d("123", "action:" + event.getAction());
                Log.d("123", "v.x" + event.getX() + ",v.y:" + event.getY());
//                Rect rect = new Rect();
//                v.getLocalVisibleRect(rect);
//                Log.d("123", "l:" + rect.left + ",top:" + rect.top + ",bottom:" + rect.bottom);

                return false;
            }
        });
    }


    private void showQuickRecorderMode() {
        qcTipView.setText("松开    发送");
        quickRecoderContainer.setVisibility(VISIBLE);
        mChatInputContainer.setVisibility(GONE);
        isShowQuickRecorderMode = true;
    }

    private void showNormalMode() {
        mChatInputContainer.setVisibility(VISIBLE);
        quickRecoderContainer.setVisibility(GONE);
        isShowQuickRecorderMode = false;
    }

    EmoticonClickListener emoticonClickListener = new EmoticonClickListener() {
        @Override
        public void onEmoticonClick(Object o, int actionType, boolean isDelBtn) {

            if (isDelBtn) {
                SimpleCommonUtils.delClick(mChatInput);
            } else {
                if (o == null) {
                    return;
                }
                if (actionType == Constants.EMOTICON_CLICK_BIGIMAGE) {
                    // if(o instanceof EmoticonEntity){
                    // OnSendImage(((EmoticonEntity)o).getIconUri());
                    // }
                } else {
                    String content = null;
                    if (o instanceof EmojiBean) {
                        content = ((EmojiBean) o).emoji;
                    } else if (o instanceof EmoticonEntity) {
                        content = ((EmoticonEntity) o).getContent();
                    }

                    if (TextUtils.isEmpty(content)) {
                        return;
                    }
                    int index = mChatInput.getSelectionStart();
                    Editable editable = mChatInput.getText();
                    editable.insert(index, content);
                }
            }
        }
    };

    private void init(Context context, AttributeSet attrs) {
        init(context);

        SimpleCommonUtils.initEmoticonsEditText(mChatInput);
        mEmojiRl.setAdapter(SimpleCommonUtils.getCommonAdapter(mContext, emoticonClickListener));
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mInput = s;

        // Starting input
        if (s.length() >= 1 && start == 0 && before == 0) {
            switchSendMode();
            // Clear content
        } else if (s.length() == 0 && before >= 1) {
            switchMultiMode();
        }
    }

    /**
     * 切换至发送模式
     */
    private void switchSendMode() {
        sendBtn.setVisibility(VISIBLE);
        mMenuItemContainer.setVisibility(GONE);
        if (onInputModeChangeListener != null) {
            onInputModeChangeListener.onSwitchInputMode(SEND_MODE);
        }

    }

    /**
     * 切换至图片发送& 语音发送模式
     */
    private void switchMultiMode() {
        sendBtn.setVisibility(GONE);
        mMenuItemContainer.setVisibility(VISIBLE);
        if (onInputModeChangeListener != null) {
            onInputModeChangeListener.onSwitchInputMode(MULTI_MODE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    private OnClickListener onMenuItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.aurora_menuitem_ib_send) {
                // Allow send text and photos at the same time.
                if (onSubmit()) {
                    mChatInput.setText("");
                }
            } else {
                mMenuManager.hideCustomMenu();
                mChatInput.clearFocus();
                if (view.getId() == R.id.aurora_ll_menuitem_emoji_container) {
                    if (mListener != null && mListener.switchToEmojiMode()) {
                        if (mEmojiRl.getVisibility() == VISIBLE && mMenuContainer.getVisibility() == VISIBLE) {
                            dismissMenuLayout();
                        } else if (isKeyboardVisible()) {
                            mPendingShowMenu = true;
                            EmoticonsKeyboardUtils.closeSoftKeyboard(mChatInput);
                            showEmojiLayout();
                        } else {
                            showMenuLayout();
                            showEmojiLayout();
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        // TODO: 2019/1/2 发送按钮
        if (view.getId() == R.id.tv_send) {
            String text = mChatInput.getText().toString().trim();

            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }

            mChatInput.setText("");
            if (sendClickListener != null) {
                sendClickListener.onSendText(text);
            }
        }
    }

    public void setAudioPlayByEarPhone(int state) {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (state == 0) {
            mIsEarPhoneOn = false;
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume, AudioManager.STREAM_VOICE_CALL);
        }
    }

    public int dp2px(float value) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    public void dismissMenuLayout() {
        mMenuManager.hideCustomMenu();
        mMenuContainer.setVisibility(GONE);
    }

    public void invisibleMenuLayout() {
        mMenuContainer.setVisibility(INVISIBLE);
    }

    public void showMenuLayout() {
        EmoticonsKeyboardUtils.closeSoftKeyboard(mChatInput);
        mMenuContainer.setVisibility(VISIBLE);
    }


    public void showEmojiLayout() {
        mEmojiRl.setVisibility(VISIBLE);
        if (onSendTouchListener != null) {
            onSendTouchListener.onEditTouch();
        }
    }

    public void hideDefaultMenuLayout() {
        mEmojiRl.setVisibility(GONE);
    }


    public void dismissEmojiLayout() {
        mEmojiRl.setVisibility(GONE);
    }

    /**
     * Set menu container's height, invoke this method once the menu was
     * initialized.
     *
     * @param height Height of menu, set same height as soft keyboard so that
     *               display to perfection.
     */
    public void setMenuContainerHeight(int height) {
        if (height > 0) {
            sMenuHeight = height;
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            mMenuContainer.setLayoutParams(params);
        }
    }

    private boolean onSubmit() {
        return mListener != null && mListener.onSendTextMessage(mInput);
    }

    public int getMenuState() {
        return mMenuContainer.getVisibility();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Set camera capture file path and file name. If user didn't invoke this
     * method, will save in default path.
     *
     * @param path     Photo to be saved in.
     * @param fileName File name.
     */
    @Deprecated
    public void setCameraCaptureFile(String path, String fileName) {
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    private long convertStrTimeToLong(String strTime) {
        String[] timeArray = strTime.split(":");
        long longTime = 0;
        if (timeArray.length == 2) {
            // If time format is MM:SS
            longTime = Integer.parseInt(timeArray[0]) * 60 * 1000 + Integer.parseInt(timeArray[1]) * 1000;
        }
        return SystemClock.elapsedRealtime() - longTime;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this);
    }


    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus && mHeight <= 0) {
            this.getRootView().getGlobalVisibleRect(mRect);
            mHeight = mRect.bottom;
            Log.d(TAG, "Window focus changed, height: " + mHeight);
        }
    }

    public boolean isKeyboardVisible() {
        return (getDistanceFromInputToBottom() > 300 && mMenuContainer.getVisibility() == GONE)
                || (getDistanceFromInputToBottom() > (mMenuContainer.getHeight() + 300)
                && mMenuContainer.getVisibility() == VISIBLE);
    }


    public void setPendingShowMenu(boolean flag) {
        this.mPendingShowMenu = flag;
    }

    @Override
    public boolean onPreDraw() {
        if (mPendingShowMenu) {
            if (isKeyboardVisible()) {
                Log.w(TAG, "isKeyboardVisible=true");
//                ViewGroup.LayoutParams params = mMenuContainer.getLayoutParams();
//                int distance = getDistanceFromInputToBottom();
//                Log.d(TAG, "Distance from bottom: " + distance);
//
//                if (distance < mHeight / 2 && distance > 300 && distance != params.height) {
//                    params.height = distance;
//                    mSoftKeyboardHeight = distance;
//                    mMenuContainer.setLayoutParams(params);
//                }
                return false;
            } else {
                Log.w(TAG, "isKeyboardVisible=false");
                showMenuLayout();
                mPendingShowMenu = false;
                return false;
            }
        } else {
            if (mMenuContainer.getVisibility() == VISIBLE && isKeyboardVisible()) {
                Log.d(TAG, "VISIBLE && isKeyboardVisible");
                dismissMenuLayout();
                return false;
            }
        }
        return true;
    }


    private boolean showBottomMenu = true;

    public void setShowBottomMenu(Boolean showBottomMenu) {
        this.showBottomMenu = showBottomMenu;
        mMenuItemContainer.setVisibility(showBottomMenu ? View.VISIBLE : View.GONE);
    }

    public boolean isShowBottomMenu() {
        return showBottomMenu;
    }

    public int getDistanceFromInputToBottom() {
        if (isShowBottomMenu()) {
            mMenuItemContainer.getGlobalVisibleRect(mRect);
        } else {
            mChatInputContainer.getGlobalVisibleRect(mRect);
        }
        return mHeight - mRect.bottom;
    }

    private final Runnable measureAndLayout = new Runnable() {
        @Override
        public void run() {
            measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
            layout(getLeft(), getTop(), getRight(), getBottom());
        }
    };

    @Override
    public void requestLayout() {
        super.requestLayout();
        // React Native Override requestLayout, since we refresh our layout in native,
        // RN catch the
        // requestLayout event, so that the view won't refresh at once, we simulate
        // layout here.
        post(measureAndLayout);
    }

    public int getSoftKeyboardHeight() {
        return mSoftKeyboardHeight > 0 ? mSoftKeyboardHeight : sMenuHeight;
    }

    public CustomMenuManager getMenuManager() {
        return this.mMenuManager;
    }

    public LinearLayout getChatInputContainer() {
        return this.mChatInputContainer;
    }

    public LinearLayout getMenuItemContainer() {
        return this.mMenuItemContainer;
    }

    public FrameLayout getMenuContainer() {
        return this.mMenuContainer;
    }

    private void setCursor(Drawable drawable) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(mChatInput, drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMenuClickListener(OnMenuClickListener listener) {
        mListener = listener;
    }

    public void setCustomMenuClickListener(CustomMenuEventListener listener) {
        mMenuManager.setCustomMenuClickListener(listener);
    }


    public void setOnClickEditTextListener(OnClickEditTextListener listener) {
        mEditTextListener = listener;
    }

    public TextView getVoiceQuickTimeLeft() {
        return voiceQuickTimeLeft;
    }

    public EditText getInputView() {
        return mChatInput;
    }


    public void showKeyBoard() {
        if (mChatInput != null) {
            mChatInput.setFocusable(true);
            mChatInput.setFocusableInTouchMode(true);
            mChatInput.requestFocus();
            try {
                InputMethodManager inputManager = (InputMethodManager) mChatInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mChatInput, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setOnSendClickListener(OnSendClickListener sendClickListener) {
        this.sendClickListener = sendClickListener;
    }

    public void setOnSendTouchListener(OnSendTouchListener onSendTouchListener) {
        this.onSendTouchListener = onSendTouchListener;
    }

    public void setOnInputModeChangeListener(OnInputModeChangeListener onInputModeChangeListener) {
        this.onInputModeChangeListener = onInputModeChangeListener;
    }

    public interface OnInputModeChangeListener {
        void onSwitchInputMode(int mode);
    }

    public interface OnSendClickListener {
        void onSendText(String o);
    }

    public interface OnSendTouchListener {
        void onEditTouch();
    }

    public interface OnQuickRecorderListener {
        void onStartRecorder();

        void onStopRecorder();

        boolean onCancelRecorder();

        void onCancelStopRecorder();

        void onResumeRecorder();
    }

}
