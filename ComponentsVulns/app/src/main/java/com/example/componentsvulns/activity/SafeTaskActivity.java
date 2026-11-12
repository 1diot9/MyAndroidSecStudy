package com.example.componentsvulns.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivitySafeSecretBinding;

/** taskAffinity 为空字符串，页面留在自己的任务栈里，不使用应用包名作为 affinity。 */
public class SafeTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySafeSecretBinding binding = ActivitySafeSecretBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("独立任务栈的页面");
        ResultEcho.show(binding.explain, "这个页面对应的清单属性是 android:taskAffinity=\"\"。\n\n"
                + "affinity 为空时，其他应用不能再把 taskAffinity 写成 com.example.componentsvulns 来加入这个页面所在的任务栈。\n\n"
                + "私有页面同时不要为了被外部复用而设置 singleTask。桌面入口 MainActivity 仍然使用默认 affinity，因为启动图标需要它。");
    }
}
