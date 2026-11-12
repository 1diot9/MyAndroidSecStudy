package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.componentsvulns.data.LabStore;

/**
 * 权限提升的对照。组件仍然 exported="true"，但清单要求 signature 级别权限。
 * 本应用在清单里声明并申请了该权限，按钮可以启动它。adb shell 没有这个签名，启动会被拒绝。
 */
public class SignatureProtectedService extends Service {

    public static final String PERMISSION = "com.example.componentsvulns.permission.USE_PROTECTED_SERVICE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LabStore.put(this, LabStore.PROTECTED_RESULT,
                "【signature 权限保护的服务】\n系统已经允许这次启动。调用方持有 " + PERMISSION + "。");
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
