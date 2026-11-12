package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.componentsvulns.data.DosPayload;

/** 拒绝服务：导出的 Service 在 onStartCommand 里直接解析 extras。进程是 :dos。 */
public class DosService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DosPayload.consume(intent);
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
