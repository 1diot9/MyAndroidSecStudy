package com.example.componentsvulns.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityLaunchModeBinding;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基本用法：四个子类在清单里分别使用 standard、singleTop、singleTask、singleInstance。
 * 「再次启动」使用显式 Intent 打开当前这个类。
 */
public class LaunchModeActivity extends AppCompatActivity {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private int sequence;
    private int newIntentCount;
    private ActivityLaunchModeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sequence = SEQUENCE.incrementAndGet();
        binding = ActivityLaunchModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getClass().getSimpleName());
        binding.again.setOnClickListener(v -> startActivity(new Intent(this, getClass())));
        render();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        newIntentCount++;
        render();
    }

    private void render() {
        ResultEcho.show(binding.status, "类：" + getClass().getSimpleName()
                + "\n实例序号：" + sequence
                + "\ntaskId：" + getTaskId()
                + "\nonNewIntent 次数：" + newIntentCount
                + "\n\nstandard：每次启动都执行 onCreate，实例序号增加。\n"
                + "singleTop：本页已在栈顶时只执行 onNewIntent，实例序号不变。\n"
                + "singleTask：本任务栈里只保留一个实例。再次启动走 onNewIntent。\n"
                + "singleInstance：这个实例单独占用一个 taskId。");
    }
}
