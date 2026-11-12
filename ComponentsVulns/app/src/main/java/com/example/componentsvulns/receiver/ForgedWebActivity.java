package com.example.componentsvulns.receiver;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityForgedWebBinding;

/**
 * 由伪造广播带进来的 URL 决定 WebView 加载什么。
 * JavaScript 已打开，并且向页面暴露了 LabPin.pin()。
 */
public class ForgedWebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityForgedWebBinding binding = ActivityForgedWebBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("伪造消息");

        String title = getIntent().getStringExtra(ForgedMessageReceiver.EXTRA_TITLE);
        String body = getIntent().getStringExtra(ForgedMessageReceiver.EXTRA_BODY);
        String url = getIntent().getStringExtra(ForgedMessageReceiver.EXTRA_URL);
        ResultEcho.show(binding.heading, title == null ? "（没有标题）" : title);
        ResultEcho.show(binding.message, body == null ? "" : body);

        WebView web = binding.web;
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.addJavascriptInterface(new LabPin(), "LabPin");
        if (url != null && !url.isEmpty()) {
            web.loadUrl(url);
        }
    }

    /** 练习数据。页面里的脚本可以调用 LabPin.pin()。 */
    public static final class LabPin {
        @JavascriptInterface
        public String pin() {
            return "246810";
        }
    }
}
