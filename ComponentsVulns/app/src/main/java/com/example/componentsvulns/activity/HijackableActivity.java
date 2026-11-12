package com.example.componentsvulns.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivitySafeSecretBinding;

/**
 * 本页 launchMode 是 singleTask，没有写 taskAffinity。
 * 未写 taskAffinity 时，所在任务栈的 affinity 是包名 com.example.componentsvulns。
 * 其他应用可以把页面的 taskAffinity 设成这个包名，再用 allowTaskReparenting 进入这个任务栈。
 */
public class HijackableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySafeSecretBinding binding = ActivitySafeSecretBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("可被插入的页面");
        ResultEcho.show(binding.explain, "这个页面 exported=\"false\"，launchMode=\"singleTask\"，没有 android:taskAffinity。\n\n"
                + "任务栈 affinity 因此是 com.example.componentsvulns。"
                + "仿冒端的 HijackActivity 使用同一个 taskAffinity，并设置 allowTaskReparenting=\"true\"、excludeFromRecents=\"true\"。\n\n"
                + "操作：安装 imposter 模块，打开仿冒端并进入劫持页，再回到本应用打开这一页。"
                + "系统若把仿冒页面移进本任务栈，你会先看到仿冒页面，而不是这段说明。\n\n"
                + "Android 10 之后系统限制了跨应用移动任务栈，新版本上可能仍然只看到本页。"
                + "要检查的配置是这里的 launchMode，以及没有把 taskAffinity 设成空字符串。对照页面是 SafeTaskActivity。");
    }
}
