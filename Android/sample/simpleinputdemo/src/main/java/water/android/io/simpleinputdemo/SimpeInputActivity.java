package water.android.io.simpleinputdemo;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import cn.jiguang.imui.chatinput.ChatInputView;
import cn.jiguang.imui.chatinput.listener.CustomMenuEventListener;
import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.menu.Menu;
import cn.jiguang.imui.chatinput.menu.MenuManager;
import cn.jiguang.imui.chatinput.menu.view.MenuFeature;
import cn.jiguang.imui.chatinput.menu.view.MenuItem;
import cn.jiguang.imui.chatinput.model.FileItem;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 最简单使用逻辑
 */
public class SimpeInputActivity extends AppCompatActivity implements View.OnTouchListener, OnMenuClickListener {

    ChatInputView chatInputView;

    private final int RC_PHOTO = 0x0003;

    private InputMethodManager mImm;
    private Window mWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simpe_input);
        this.mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        this.mWindow = getWindow();

        chatInputView = findViewById(R.id.chat_input);
        /**
         * Should set menu container height once the ChatInputView has been initialized.
         * For perfect display, the height should be equals with soft input height.
         */
        // TODO: 2018/12/24 关键点
        chatInputView.setMenuContainerHeight(819);

        // add Custom Menu View
        MenuManager menuManager = chatInputView.getMenuManager();
//        menuManager.addCustomMenu("MY_CUSTOM",R.layout.menu_text_item,R.layout.menu_text_feature);

        // Custom menu order
        menuManager.setMenu(Menu.newBuilder().
                customize(true).
//                setRight(Menu.TAG_SEND).
//                setBottom(Menu.TAG_VOICE, Menu.TAG_EMOJI, Menu.TAG_GALLERY, Menu.TAG_CAMERA, "MY_CUSTOM").
        setBottom(Menu.TAG_EMOJI, Menu.TAG_GALLERY).
                        build());
        menuManager.setCustomMenuClickListener(new CustomMenuEventListener() {
            @Override
            public boolean onMenuItemClick(String tag, MenuItem menuItem) {
                //Menu feature will not be show shown if return false；
                return true;
            }

            @Override
            public void onMenuFeatureVisibilityChanged(int visibility, String tag, MenuFeature menuFeature) {
                if (visibility == View.VISIBLE) {
                    // Menu feature is visible.
                } else {
                    // Menu feature is gone.
                }
            }
        });
//        chatInputView.setOnTouchListener(this);
        chatInputView.setMenuClickListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (chatInputView.getMenuState() == View.VISIBLE) {
                    chatInputView.dismissMenuLayout();
                }
                try {
                    View v = getCurrentFocus();
                    if (mImm != null && v != null) {
                        mImm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        view.clearFocus();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
            default:
        }
        return false;
    }


    @Override
    public boolean onSendTextMessage(CharSequence input) {
        return false;
    }

    @Override
    public void onSendFiles(List<FileItem> list) {

    }

    @Override
    public boolean switchToMicrophoneMode() {
        return false;
    }

    @Override
    public boolean switchToGalleryMode() {
        String[] perms = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "需要获取相册权限", RC_PHOTO, perms);
        }
        // If you call updateData, select photo view will try to update data(Last update over 30 seconds.)
        chatInputView.getSelectPhotoView().updateData();
        return true;
    }

    @Override
    public boolean switchToCameraMode() {
        return false;
    }

    @Override
    public boolean switchToEmojiMode() {
        return true;
    }
}
