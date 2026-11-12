package com.example.componentsvulns.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivitySecretNoteBinding;

/**
 * 两处 Activity 问题都在这个类里。
 *
 * 密码比较写在 LoginActivity 的按钮监听器中。本类 exported="true"，
 * 外部用显式 Intent 启动时，系统只调用本类的 onCreate，不会先执行 LoginActivity。
 * extra from_login 只改变横幅文字，dumpAll() 仍会执行。
 *
 * extra forward 是调用方传入的另一个 Intent。本类对它调用 startActivity 时，
 * 调用方是本应用，因此可以打开本应用里 exported="false" 的 SafeSecretActivity。
 * 包名判断只拒绝指向其他包的 Intent。
 */
public class SecretNoteActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_LOGIN = "from_login";
    /** 调用者可以放进一个 Intent，本页面会尝试启动它。 */
    public static final String EXTRA_FORWARD = "forward";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySecretNoteBinding binding = ActivitySecretNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("私密笔记");

        boolean fromLogin = getIntent().getBooleanExtra(EXTRA_FROM_LOGIN, false);
        if (fromLogin) {
            binding.banner.setBackgroundColor(0xFFE8F5E9);
            binding.banner.setTextColor(0xFF1B5E20);
            ResultEcho.show(binding.banner, "你是通过密码页进来的。这是正常路径。");
        } else {
            ResultEcho.show(binding.banner, "你没有经过密码页。这个 Activity 在清单里是 exported=\"true\"，其他应用只要知道类名就能直接打开它。");
        }
        ResultEcho.show(binding.notes, NotesRepository.get(this).dumpAll());
        if (savedInstanceState == null) {
            openForwardIfPresent();
        }
    }

    /**
     * 取出 extra forward 并 startActivity。这次启动由本应用发起，
     * 所以本应用中 exported="false" 的页面也能被打开。
     * 包名判断用来避免练习包启动其他应用，挡不住本包里的 SafeSecretActivity。
     */
    private void openForwardIfPresent() {
        Intent forward = readForward(getIntent());
        if (forward == null) {
            return;
        }
        ComponentName who = forward.getComponent();
        if (who == null || !getPackageName().equals(who.getPackageName())) {
            Toast.makeText(this,
                    "练习护栏：只转发指向本 App 的页面。真实漏洞里常常没有这道护栏。",
                    Toast.LENGTH_LONG).show();
            return;
        }
        forward.putExtra(SafeSecretActivity.EXTRA_VIA_REDIRECT, true);
        startActivity(forward);
    }

    private static Intent readForward(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(EXTRA_FORWARD, Intent.class);
        }
        return readForwardBeforeTiramisu(intent);
    }

    @SuppressWarnings("deprecation")
    private static Intent readForwardBeforeTiramisu(Intent intent) {
        return intent.getParcelableExtra(EXTRA_FORWARD);
    }
}
