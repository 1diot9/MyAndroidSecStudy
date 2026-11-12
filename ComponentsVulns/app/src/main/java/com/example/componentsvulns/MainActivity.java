package com.example.componentsvulns;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.activity.ActivityLabActivity;
import com.example.componentsvulns.data.DemoFiles;
import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.databinding.ActivityMainBinding;
import com.example.componentsvulns.provider.ProviderLabActivity;
import com.example.componentsvulns.receiver.ReceiverLabActivity;
import com.example.componentsvulns.service.ServiceLabActivity;

/**
 * 首页。四个按钮按 Activity、Service、BroadcastReceiver、ContentProvider 进入实验页。
 * 本页 exported="true"，因为它带有桌面启动入口。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.openActivity.setOnClickListener(v ->
                startActivity(new Intent(this, ActivityLabActivity.class)));
        binding.openProvider.setOnClickListener(v ->
                startActivity(new Intent(this, ProviderLabActivity.class)));
        binding.openService.setOnClickListener(v ->
                startActivity(new Intent(this, ServiceLabActivity.class)));
        binding.openReceiver.setOnClickListener(v ->
                startActivity(new Intent(this, ReceiverLabActivity.class)));
        binding.reset.setOnClickListener(v -> {
            NotesRepository.get(this).reset();
            DemoFiles.writeSamples(this);
            Toast.makeText(this, "演示笔记和练习文件已恢复", Toast.LENGTH_SHORT).show();
        });
    }
}
