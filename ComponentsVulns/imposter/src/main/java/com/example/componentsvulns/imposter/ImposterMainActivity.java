package com.example.componentsvulns.imposter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 仿冒端。只和 ComponentsVulns 练习包里的演示 action、任务栈 affinity 交互。
 */
public class ImposterMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imposter_main);
        findViewById(R.id.open_hijack).setOnClickListener(v ->
                startActivity(new Intent(this, HijackActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.widget.TextView result = findViewById(R.id.upgrade_result);
        String url = getSharedPreferences(ImposterUpgradeService.PREFS, MODE_PRIVATE)
                .getString(ImposterUpgradeService.KEY_URL, null);
        String message = url == null
                ? "还没有收到升级广播或服务启动。在「组件漏洞演示」里执行 Service 劫持后回到这里。"
                : "仿冒服务收到的 download_url：\n" + url;
        result.setText(message);
        Log.d("ComponentsVulns", message);
    }
}
