package com.example.componentsvulns.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.databinding.ActivityLoginBinding;

/**
 * 正常路径上的密码页。密码写在界面上，因为这是练习，不是真正的登录功能。
 * 它本身 exported="false"，不是本课的漏洞点。
 * 漏洞是：后面的 SecretNoteActivity 被导出了，不经过这一页也能打开。
 */
public class LoginActivity extends AppCompatActivity {

    /** 写在界面上的练习密码。不要在真实应用里把密码写死在代码里。 */
    static final String DEMO_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("先输入密码");

        binding.open.setOnClickListener(v -> {
            String input = binding.password.getText().toString().trim();
            if (!DEMO_PASSWORD.equals(input)) {
                Toast.makeText(this, "密码不对。练习密码是 123456。", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent next = new Intent(this, SecretNoteActivity.class);
            next.putExtra(SecretNoteActivity.EXTRA_FROM_LOGIN, true);
            startActivity(next);
        });
    }
}
