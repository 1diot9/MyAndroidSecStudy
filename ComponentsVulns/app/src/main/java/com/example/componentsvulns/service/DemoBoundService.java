package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/** 基本用法：bindService 拿到本进程内的 Binder，调用方直接调用其中的方法。不导出。 */
public class DemoBoundService extends Service {
    private static final String TAG = "DemoBoundService";

    private final LocalBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DemoBoundService getService(){
            return DemoBoundService.this;
        }
        public String status() {
            return "onBind 返回了 LocalBinder。\n调用方与服务在同一个进程，可以直接调用 status()。\nunbindService 之后，这次绑定结束。";
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "DemoBoundService onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "DemoBoundService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DemoBoundService onDestroy");
        super.onDestroy();
    }

    // 模拟业务方法
    public int getUserId(){
        return 233;
    }
}
