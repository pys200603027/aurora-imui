package water.android.io.chatinputextra.input;

import java.util.List;

import cn.jiguang.imui.chatinput.listener.OnMenuClickListener;
import cn.jiguang.imui.chatinput.model.FileItem;

public class OnMenuClickListenerWrapper implements OnMenuClickListener {

    @Override
    public boolean onSendTextMessage(CharSequence charSequence) {
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
        return false;
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
