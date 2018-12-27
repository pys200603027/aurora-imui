package water.android.io.simpleinputdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.ImageEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.listener.OnResultListener;
import java.util.List;

import cn.jiguang.imui.chatinput.listener.CustomMenuEventListener;
import cn.jiguang.imui.chatinput.menu.Menu;
import cn.jiguang.imui.chatinput.menu.view.MenuFeature;
import cn.jiguang.imui.chatinput.menu.view.MenuItem;
import water.android.io.chatinputextra.input.CustomInputView;
import water.android.io.chatinputextra.input.CustomMenuManager;
import water.android.io.chatinputextra.input.GifSizeFilter;
import water.android.io.chatinputextra.input.Glide4Engine;
import water.android.io.chatinputextra.input.OnMenuClickListenerWrapper;

/**
 * 最简单使用逻辑
 */
public class SimpeInputActivity extends AppCompatActivity {

    CustomInputView chatInputView;
    ImageView imageView;
    private static final int REQUEST_CODE_CHOOSE = 23;

    private final int RC_PHOTO = 0x0003;
    ImageEngine imageEngine = new Glide4Engine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simpe_input);
        chatInputView = findViewById(R.id.chat_input);
        imageView = findViewById(R.id.img);
        /**
         * Should set menu container height once the ChatInputView has been initialized.
         * For perfect display, the height should be equals with soft input height.
         */
        chatInputView.setMenuContainerHeight(879);

        // add Custom Menu View
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .countable(false)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .maxSelectable(9)
                .originalEnable(true)
                .maxOriginalSize(10)
                .imageEngine(new Glide4Engine())
                .forCallback(new OnResultListener() {
                    @Override
                    public void onResult(int i, Intent intent) {
                        Log.d("123", "i:" + i);
                        List<Uri> uris = Matisse.obtainResult(intent);
                        imageEngine.loadImage(imageView.getContext(), imageView.getWidth(), imageView.getHeight(), imageView, uris.get(0));
                    }
                });

        CustomMenuManager menuManager = chatInputView.getMenuManager();
        menuManager.addCustomMenu("recorder", R.layout.im_menu_voice_item, R.layout.im_menu_voice_feature);
        menuManager.addCustomMenu("photo", R.layout.im_menu_photo_item, R.layout.im_menu_photo_feature);

        // Custom menu order
        menuManager.setMenu(Menu.newBuilder().
                customize(true).
                setBottom("recorder", "photo", Menu.TAG_EMOJI).build());
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
        chatInputView.setMenuClickListener(new OnMenuClickListenerWrapper());
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
