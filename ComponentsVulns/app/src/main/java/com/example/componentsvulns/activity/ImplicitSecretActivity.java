package com.example.componentsvulns.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivitySecretNoteBinding;

/**
 * 越权绕过的另一种入口：清单里有 intent-filter，并且 exported="true"。
 * 调用方只写 action，不写类名，只要 action 和 DEFAULT category 能匹配，系统就会打开本页。
 * 本工程 targetSdk 大于 30，有 intent-filter 的组件必须手写 exported；
 * targetSdk 小于 31 时，带 intent-filter 且不写 exported 的 Activity 默认就是导出的。
 */
public class ImplicitSecretActivity extends AppCompatActivity {

    public static final String ACTION = "com.example.componentsvulns.action.OPEN_SECRET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySecretNoteBinding binding = ActivitySecretNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("隐式打开的私密页");
        ResultEcho.show(binding.banner, "这个页面由 action " + ACTION + " 匹配打开，没有经过 LoginActivity。");
        ResultEcho.show(binding.notes, NotesRepository.get(this).dumpAll());
    }
}
