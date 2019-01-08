package water.android.io.simpleinputdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.ImageEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.listener.OnResultListener;
import com.zhihu.matisse.ui.ActivityResultHelper;

import java.util.List;

import cn.jiguang.imui.chatinput.extra.input.CustomInputView;
import cn.jiguang.imui.chatinput.extra.input.CustomMenuManager;
import cn.jiguang.imui.chatinput.extra.input.GifSizeFilter;
import cn.jiguang.imui.chatinput.extra.input.Glide4Engine;
import cn.jiguang.imui.chatinput.extra.input.OnMenuClickListenerWrapper;
import cn.jiguang.imui.chatinput.listener.CustomMenuEventListener;
import cn.jiguang.imui.chatinput.menu.Menu;
import cn.jiguang.imui.chatinput.menu.view.MenuFeature;
import cn.jiguang.imui.chatinput.menu.view.MenuItem;

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

        int height = getWindow().getDecorView().getHeight() * 3 / 4;
        /**
         * Should set menu container height once the ChatInputView has been initialized.
         * For perfect display, the height should be equals with soft input height.
         */
        chatInputView.setMenuContainerHeight(587);
        chatInputView.setPendingShowMenu(true);

//        // add Custom Menu View
//        Matisse.from(this)
//                .choose(MimeType.ofImage())
//                .countable(false)
//                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
//                .maxSelectable(9)
//                .originalEnable(true)
//                .maxOriginalSize(10)
//                .imageEngine(new Glide4Engine())
//                .forCallback(new OnResultListener() {
//                    @Override
//                    public void onResult(int i, Intent intent) {
//                        Log.d("123", "i:" + i);
//                        List<Uri> uris = Matisse.obtainResult(intent);
//                        imageEngine.loadImage(imageView.getContext(), imageView.getWidth(), imageView.getHeight(), imageView, uris.get(0));
//                    }
//                });
        chatInputView.initCostomMenu();
        chatInputView.setRecorderQuickTouch(new CustomInputView.OnQuickRecorderListener() {
            @Override
            public void onStartRecorder() {
                Log.d("123", "onStartRecorder");
            }

            @Override
            public void onStopRecorder() {
                Log.d("123", "onStopRecorder");
            }

            @Override
            public boolean onCancelRecorder() {
                Log.d("123", "onCancelRecorder");
                return true;
            }

            @Override
            public void onCancelStopRecorder() {
                Log.d("123", "onCancelStopRecorder");
            }

            @Override
            public void onResumeRecorder() {
                Log.d("123", "onResumeRecorder");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultHelper.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
