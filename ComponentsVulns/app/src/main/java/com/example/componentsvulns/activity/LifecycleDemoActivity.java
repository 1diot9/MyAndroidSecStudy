package com.example.componentsvulns.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityLifecycleBinding;

/** 基本用法：把生命周期回调按发生顺序记在页面上。 */
public class LifecycleDemoActivity extends AppCompatActivity {

    private final StringBuilder log = new StringBuilder();
    private ActivityLifecycleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLifecycleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("生命周期");
        binding.again.setOnClickListener(v ->
                startActivity(new Intent(this, LifecycleDemoActivity.class)));
        mark("onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mark("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mark("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mark("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        mark("onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mark("onRestart");
    }

    @Override
    protected void onDestroy() {
        mark("onDestroy");
        super.onDestroy();
    }

    private void mark(String name) {
        log.append(name).append('\n');
        if (binding != null) {
            ResultEcho.show(binding.log, log.toString());
        }
    }
}
