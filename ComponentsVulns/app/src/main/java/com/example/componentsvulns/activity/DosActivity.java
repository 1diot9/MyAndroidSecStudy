package com.example.componentsvulns.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.DosPayload;

/**
 * 拒绝服务：导出的 Activity 在 onCreate 里直接解析 extras，没有 try/catch。
 * 进程是 :dos，崩溃时主进程里的实验页还在。
 */
public class DosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DosPayload.consume(getIntent());
        setTitle("拒绝服务页面没有崩溃");
    }
}
