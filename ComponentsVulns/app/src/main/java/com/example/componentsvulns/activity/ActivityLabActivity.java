package com.example.componentsvulns.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.R;
import com.example.componentsvulns.data.DosPayload;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityActivityLabBinding;

/** Activity 实验页。先是基本用法，后面按漏洞分组。 */
public class ActivityLabActivity extends AppCompatActivity {

    private ActivityActivityLabBinding binding;

    private final ActivityResultLauncher<Intent> echoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                String reply = null;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    reply = result.getData().getStringExtra(EchoActivity.EXTRA_REPLY);
                }
                ResultEcho.show(binding.basicResult, reply == null
                        ? "EchoActivity 没有返回字符串。resultCode=" + result.getResultCode()
                        : "上一页收到的返回值：\n" + reply);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivityLabBinding.inflate(getLayoutInflater());
//        setContentView(R.layout.activity_activity_lab);
        setContentView(binding.getRoot());
        setTitle("页面 Activity");

        binding.basicExplicit.setOnClickListener(v -> echoLauncher.launch(explicitEcho()));
        binding.basicImplicit.setOnClickListener(v -> echoLauncher.launch(implicitEcho()));
        binding.basicLifecycle.setOnClickListener(v ->
                startActivity(new Intent(this, LifecycleDemoActivity.class)));
        binding.basicStandard.setOnClickListener(v ->
                startActivity(new Intent(this, StandardLaunchActivity.class)));
        binding.basicSingleTop.setOnClickListener(v ->
                startActivity(new Intent(this, SingleTopLaunchActivity.class)));
        binding.basicSingleTask.setOnClickListener(v ->
                startActivity(new Intent(this, SingleTaskLaunchActivity.class)));
        binding.basicSingleInstance.setOnClickListener(v ->
                startActivity(new Intent(this, SingleInstanceLaunchActivity.class)));

        binding.openLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
        binding.openDirect.setOnClickListener(v ->
                startActivity(new Intent(this, SecretNoteActivity.class)));
        binding.openImplicit.setOnClickListener(v -> {
            Intent intent = new Intent(ImplicitSecretActivity.ACTION);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        });
        binding.openSafe.setOnClickListener(v ->
                startActivity(new Intent(this, SafeSecretActivity.class)));
        binding.openRedirect.setOnClickListener(v -> {
            Intent inner = new Intent(this, SafeSecretActivity.class);
            Intent outer = new Intent(this, SecretNoteActivity.class);
            outer.putExtra(SecretNoteActivity.EXTRA_FORWARD, inner);
            startActivity(outer);
        });
        binding.openHijackable.setOnClickListener(v ->
                startActivity(new Intent(this, HijackableActivity.class)));
        binding.openSafeTask.setOnClickListener(v ->
                startActivity(new Intent(this, SafeTaskActivity.class)));
        binding.dosEmpty.setOnClickListener(v ->
                startActivity(new Intent(this, DosActivity.class)));
        binding.dosType.setOnClickListener(v -> {
            Intent intent = new Intent(this, DosActivity.class);
            intent.putExtra(DosPayload.EXTRA_COUNT, "abc");
            startActivity(intent);
        });
        binding.dosCatch.setOnClickListener(v -> {
            Intent empty = new Intent();
            Intent typed = new Intent();
            typed.putExtra(DosPayload.EXTRA_COUNT, "abc");
            ResultEcho.show(binding.dosResult, "空 extras：\n" + DosPayload.describeFailure(empty)
                    + "\n\ncount 为字符串：\n" + DosPayload.describeFailure(typed)
                    + "\n\n上面是本页捕获的结果。前两个按钮会在 :dos 进程里直接调用 DosPayload.consume，进程会崩溃。");
        });
    }

    private Intent explicitEcho() {
        Intent intent = new Intent(this, EchoActivity.class);
        intent.putExtra(EchoActivity.EXTRA_MESSAGE, "显式 Intent 写入的 message");
        return intent;
    }

    private Intent implicitEcho() {
        Intent intent = new Intent(EchoActivity.ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setPackage(getPackageName());
        intent.putExtra(EchoActivity.EXTRA_MESSAGE, "隐式 Intent 写入的 message");
        return intent;
    }
}
