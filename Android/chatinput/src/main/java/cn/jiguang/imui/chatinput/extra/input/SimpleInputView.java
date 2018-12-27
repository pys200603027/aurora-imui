package cn.jiguang.imui.chatinput.extra.input;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.jiguang.imui.chatinput.R;


/**
 * 聊天简单输入功能
 */
public class SimpleInputView extends RelativeLayout {
    EditText editText;
    TextView sendView;
    OnSendClickListener sendClickListener;
    OnSendTouchListener onSendTouchListener;

    public SimpleInputView(Context context) {
        super(context);
        initView(context);
    }

    public SimpleInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.im_item_input_view, this);
        editText = findViewById(R.id.et_input);
        sendView = findViewById(R.id.send);
        sendView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();

                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                editText.setText("");
                if (sendClickListener != null) {
                    sendClickListener.onSendText(text);
                }
            }
        });
        //输入框时滑动到最后一行
        editText.setOnTouchListener((v, event) -> {
            if (onSendTouchListener != null) {
                onSendTouchListener.onEditTouch();
            }
            return false;
        });
    }

    public void setOnSendClickListener(OnSendClickListener sendClickListener) {
        this.sendClickListener = sendClickListener;
    }

    public void setOnSendTouchListener(OnSendTouchListener onSendTouchListener) {
        this.onSendTouchListener = onSendTouchListener;
    }

    public interface OnSendClickListener {
        void onSendText(String o);
    }

    public interface OnSendTouchListener {
        void onEditTouch();
    }
}
