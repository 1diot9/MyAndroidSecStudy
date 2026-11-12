package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;

/**
 * 消息伪造：导出的 Service 把 extra download_url 当作升级地址保存。
 * 这里不调用 {@link #isAllowed(String)}。调用方可以传入任意 URL。
 * 同一个 action 也用于 Service 劫持实验：其他应用可以声明相同的 action。
 */
public class UpgradeService extends Service {

    public static final String ACTION = "com.example.componentsvulns.action.APP_UPGRADE";
    public static final String EXTRA_URL = "download_url";
    public static final String ALLOWED_HOST = "updates.example.invalid";

    /** 对照路径使用。有漏洞的 onStartCommand 不会调用它。 */
    public static boolean isAllowed(String url) {
        if (url == null) {
            return false;
        }
        Uri uri = Uri.parse(url);
        return "https".equalsIgnoreCase(uri.getScheme()) && ALLOWED_HOST.equalsIgnoreCase(uri.getHost());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent == null ? null : intent.getStringExtra(EXTRA_URL);
        Log.i(App.TAG, "UpgradeService 收到 download_url=" + url);
        LabStore.put(this, LabStore.UPGRADE_RESULT,
                "【本应用的 UpgradeService】\n没有检查 host，已把下面的字符串当作升级地址：\n" + url);
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
