package com.example.componentsvulns.imposter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 与 com.example.componentsvulns.service.UpgradeService 声明了同一个 action。
 * extra 的键名必须是 download_url。
 */
public class ImposterUpgradeService extends Service {

    public static final String PREFS = "imposter";
    public static final String KEY_URL = "download_url";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent == null ? null : intent.getStringExtra(KEY_URL);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URL, String.valueOf(url)).apply();
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
