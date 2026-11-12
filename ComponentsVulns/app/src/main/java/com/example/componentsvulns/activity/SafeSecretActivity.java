package com.example.componentsvulns.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivitySafeSecretBinding;

/**
 * 【对照】清单里 exported="false"。
 * 本 App 自己可以打开它。其他应用（包括 adb shell）打开它时，系统会拒绝。
 */
public class SafeSecretActivity extends AppCompatActivity {

    public static final String EXTRA_VIA_REDIRECT = "via_redirect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySafeSecretBinding binding = ActivitySafeSecretBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("未导出的页面");

        if (getIntent().getBooleanExtra(EXTRA_VIA_REDIRECT, false)) {
            ResultEcho.show(binding.explain, "你是被导出的 SecretNoteActivity 转发进来的。\n\n"
                    + "这个页面自己的 exported 是 false，其他应用不能直接打开它。"
                    + "可是导出的页面如果愿意启动别人传进来的 Intent，外部应用就可以借那一跳到达这里。\n\n"
                    + "正确做法：导出的页面不要启动外部传来的 Intent。"
                    + "如果业务必须继续跳转，只接受你自己写死的目标，不要接受调用者整包传进来的 Intent。");
        } else {
            ResultEcho.show(binding.explain, "这个页面的 exported 是 false。\n\n"
                    + "你能看到它，是因为打开它的代码跑在本 App 里面。同一个应用访问自己的未导出组件，系统是允许的。\n\n"
                    + "请按说明文档执行：\n"
                    + "adb shell am start -n com.example.componentsvulns/.activity.SafeSecretActivity\n\n"
                    + "这一次调用方是 shell，不是本 App，系统会拒绝。");
        }
    }
}
