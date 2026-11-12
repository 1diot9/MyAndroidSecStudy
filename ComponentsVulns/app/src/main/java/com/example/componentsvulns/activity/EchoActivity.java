package com.example.componentsvulns.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityEchoBinding;

/**
 * 基本用法：读取显式或隐式 Intent 里的字符串，并用 setResult 把另一段字符串交还给调用方。
 * 不读取笔记。exported="false"，只有本应用能打开。
 */
public class EchoActivity extends AppCompatActivity {

    public static final String ACTION = "com.example.componentsvulns.action.DEMO_ECHO";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_REPLY = "reply";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityEchoBinding binding = ActivityEchoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("回显页面");

        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        boolean implicit = ACTION.equals(getIntent().getAction());
        ResultEcho.show(binding.received, (implicit ? "这次 Intent 只写了 action。\n" : "这次 Intent 写了本类的类名。\n")
                + "getStringExtra(\"" + EXTRA_MESSAGE + "\") = "
                + (message == null ? "（没有这条 extra）" : message));
        binding.send.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_REPLY, binding.reply.getText().toString());
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
