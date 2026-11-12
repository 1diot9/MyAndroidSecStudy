package com.example.componentsvulns.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityDeepLinkBinding;

/**
 * 浏览器 Deep Link 入口。清单里的 intent-filter 声明 VIEW、BROWSABLE，并用 data 限定 URI。
 * 匹配的链接形如 componentsvulns://lab/open?id=1。没有实验页按钮，从浏览器或 adb 打开。
 */
public class DeepLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDeepLinkBinding binding = ActivityDeepLinkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Deep Link");
        ResultEcho.show(binding.uri, describe(getIntent()));
    }

    private static String describe(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null) {
            return "这次启动没有 data URI。\n浏览器打开 componentsvulns://lab/open?id=1 时，系统会把整段链接放进 Intent 的 data。";
        }
        return "data = " + uri
                + "\nscheme = " + uri.getScheme()
                + "\nhost = " + uri.getHost()
                + "\npath = " + uri.getPath()
                + "\nquery = " + uri.getQuery();
    }
}
