package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.componentsvulns.data.LabStore;

/** 基本用法：startService 进入 onStartCommand，stopService 进入 onDestroy。不导出。 */
public class DemoStartedService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LabStore.put(this, LabStore.DEMO_SERVICE,
                "onStartCommand：服务已启动。\n返回 START_NOT_STICKY。进程被杀掉后，系统不会自动再启动它。\n点「stopService」会进入 onDestroy。");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        LabStore.put(this, LabStore.DEMO_SERVICE, "onDestroy：stopService 之后，这个启动式服务结束。");
        super.onDestroy();
    }
}
