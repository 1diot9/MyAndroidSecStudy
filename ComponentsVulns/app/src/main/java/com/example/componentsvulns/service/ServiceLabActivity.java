package com.example.componentsvulns.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.DosPayload;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityServiceLabBinding;

import java.util.List;

/** Service 实验页。按钮按权限提升、消息伪造、劫持、拒绝服务分组。 */
public class ServiceLabActivity extends AppCompatActivity {

    private static final String SAMPLE_URL = "http://evil.example/update.apk";

    private ActivityServiceLabBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ServiceConnection demoConnection;
    private boolean demoBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServiceLabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("服务 Service");

        binding.basicStart.setOnClickListener(v -> {
            startService(new Intent(this, DemoStartedService.class));
            handler.postDelayed(this::showBasic, 200);
        });
        binding.basicStop.setOnClickListener(v -> {
            stopService(new Intent(this, DemoStartedService.class));
            handler.postDelayed(this::showBasic, 200);
        });
        binding.basicBind.setOnClickListener(v -> bindDemo());
        binding.basicUnbind.setOnClickListener(v -> unbindDemo());

        binding.dumpExported.setOnClickListener(v -> start(VulnerableExportService.class, "dump"));
        binding.deleteExported.setOnClickListener(v -> start(VulnerableExportService.class, "delete"));
        binding.dumpSafe.setOnClickListener(v -> start(SafeExportService.class, "dump"));
        binding.startProtected.setOnClickListener(v -> {
            startService(new Intent(this, SignatureProtectedService.class));
            handler.postDelayed(this::showPrivilege, 200);
        });
        binding.forgeUpgrade.setOnClickListener(v -> startUpgrade(new Intent(this, UpgradeService.class)));
        binding.checkUpgrade.setOnClickListener(v -> {
            if (!UpgradeService.isAllowed(SAMPLE_URL)) {
                LabStore.put(this, LabStore.UPGRADE_RESULT,
                        "已拒绝启动。允许的地址必须是 https，且 host 为 "
                                + UpgradeService.ALLOWED_HOST + "。\n传入的是 " + SAMPLE_URL);
                showForge();
                return;
            }
            startUpgrade(new Intent(this, UpgradeService.class));
        });
        binding.implicitDirect.setOnClickListener(v -> {
            Intent intent = new Intent(UpgradeService.ACTION);
            intent.putExtra(UpgradeService.EXTRA_URL, SAMPLE_URL);
            try {
                startService(intent);
                LabStore.put(this, LabStore.HIJACK_RESULT, "隐式 startService 没有抛出异常。");
            } catch (RuntimeException exception) {
                LabStore.put(this, LabStore.HIJACK_RESULT,
                        exception.getClass().getSimpleName() + "：\n" + exception.getMessage()
                                + "\n\ntargetSdk 大于等于 21 时，系统拒绝没有组件名也没有包名的 Service Intent。"
                                + "下一个按钮先 queryIntentServices，再显式启动列表中的第一个，其他应用因此仍能接到请求。");
            }
            showHijack();
        });
        binding.resolveFirst.setOnClickListener(v -> startFirstMatch(false));
        binding.resolveOwn.setOnClickListener(v -> startFirstMatch(true));
        binding.dosEmpty.setOnClickListener(v ->
                startService(new Intent(this, DosService.class)));
        binding.dosType.setOnClickListener(v -> {
            Intent intent = new Intent(this, DosService.class);
            intent.putExtra(DosPayload.EXTRA_COUNT, "abc");
            startService(intent);
        });
        binding.dosCatch.setOnClickListener(v -> {
            Intent empty = new Intent();
            Intent typed = new Intent();
            typed.putExtra(DosPayload.EXTRA_COUNT, "abc");
            LabStore.put(this, LabStore.DOS_RESULT,
                    "空 extras：\n" + DosPayload.describeFailure(empty)
                            + "\n\ncount 为字符串：\n" + DosPayload.describeFailure(typed));
            showDos();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPrivilege();
        showForge();
        showHijack();
        showDos();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (demoBound && demoConnection != null) {
            unbindService(demoConnection);
            demoBound = false;
        }
        super.onDestroy();
    }

    private void showBasic() {
        ResultEcho.show(binding.basicResult, LabStore.get(this, LabStore.DEMO_SERVICE));
    }

    private void bindDemo() {
        if (demoBound) {
            return;
        }
        demoConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DemoBoundService.LocalBinder binder = (DemoBoundService.LocalBinder) service;
                DemoBoundService demoBoundService = binder.getService();
                Log.d("bindDemo", String.valueOf(demoBoundService.getUserId()));
                ResultEcho.show(binding.basicResult, binder.status());
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                demoBound = false;
            }
        };
        demoBound = bindService(new Intent(this, DemoBoundService.class), demoConnection, BIND_AUTO_CREATE);
    }

    private void unbindDemo() {
        if (!demoBound || demoConnection == null) {
            ResultEcho.show(binding.basicResult, "当前没有绑定。");
            return;
        }
        unbindService(demoConnection);
        demoBound = false;
        ResultEcho.show(binding.basicResult, "已调用 unbindService。");
    }

    private void start(Class<?> serviceClass, String command) {
        Intent intent = new Intent(this, serviceClass);
        intent.putExtra(VulnerableExportService.EXTRA_COMMAND, command);
        startService(intent);
        handler.postDelayed(this::showPrivilege, 200);
    }

    private void startUpgrade(Intent intent) {
        intent.putExtra(UpgradeService.EXTRA_URL, SAMPLE_URL);
        startService(intent);
        handler.postDelayed(this::showForge, 200);
    }

    private void startFirstMatch(boolean limitToSelf) {
        Intent intent = new Intent(UpgradeService.ACTION);
        intent.putExtra(UpgradeService.EXTRA_URL, SAMPLE_URL);
        if (limitToSelf) {
            intent.setPackage(getPackageName());
        }
        List<ResolveInfo> matches = getPackageManager().queryIntentServices(intent, 0);
        StringBuilder report = new StringBuilder();
        if (limitToSelf) {
            report.append("已 setPackage(").append(getPackageName()).append(")。\n");
        } else {
            report.append("没有 setPackage，按 priority 从高到低匹配。\n");
        }
        if (matches.isEmpty()) {
            report.append("没有匹配的 Service。安装 imposter 模块后再试劫持按钮。");
            LabStore.put(this, LabStore.HIJACK_RESULT, report.toString());
            showHijack();
            return;
        }
        for (ResolveInfo info : matches) {
            report.append(info.serviceInfo.packageName)
                    .append('/')
                    .append(info.serviceInfo.name)
                    .append(" priority=")
                    .append(info.priority)
                    .append('\n');
        }
        ResolveInfo chosen = matches.get(0);
        ComponentName component = new ComponentName(chosen.serviceInfo.packageName, chosen.serviceInfo.name);
        intent.setComponent(component);
        startService(intent);
        report.append("\n启动了第一个：\n").append(component.flattenToShortString());
        if (!getPackageName().equals(component.getPackageName())) {
            report.append("\n打开「组件漏洞仿冒端」可以看到它收到的 URL。");
        }
        LabStore.put(this, LabStore.HIJACK_RESULT, report.toString());
        handler.postDelayed(this::showHijack, 200);
    }

    private void showPrivilege() {
        String exported = LabStore.get(this, LabStore.SERVICE_RESULT, "");
        String signature = LabStore.get(this, LabStore.PROTECTED_RESULT, "");
        if (exported.isEmpty() && signature.isEmpty()) {
            return;
        }
        StringBuilder text = new StringBuilder();
        if (!exported.isEmpty()) {
            text.append("导出的服务\n").append(exported);
        }
        if (!signature.isEmpty()) {
            if (text.length() > 0) {
                text.append("\n\n");
            }
            text.append("signature 权限\n").append(signature);
        }
        ResultEcho.show(binding.privilegeResult, text);
    }

    private void showForge() {
        showStored(binding.forgeResult, LabStore.UPGRADE_RESULT);
    }

    private void showHijack() {
        showStored(binding.hijackResult, LabStore.HIJACK_RESULT);
    }

    private void showDos() {
        showStored(binding.dosResult, LabStore.DOS_RESULT);
    }

    private void showStored(TextView view, String key) {
        String value = LabStore.get(this, key, "");
        if (!value.isEmpty()) {
            ResultEcho.show(view, value);
        }
    }
}
