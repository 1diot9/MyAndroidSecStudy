package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;

/**
 * 【对照】清单里 exported="false"。
 * 本 App 自己启动它会成功。adb shell 从外部启动它时，系统会拒绝，代码不会执行到这里。
 */
public class SafeExportService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent == null ? null : intent.getStringExtra(VulnerableExportService.EXTRA_COMMAND);
        Log.i(App.TAG, "未导出的 Service 被本 App 启动，command=" + command);
        NotesRepository repository = NotesRepository.get(this);
        String result;
        if ("delete".equals(command)) {
            repository.deleteAll();
            result = "【未导出的服务】\n清空执行了。因为这次是本 App 自己启动的。\n"
                    + "请再用说明文档里的 adb 命令启动 SafeExportService，那一次应该被系统拒绝。";
        } else {
            result = "【未导出的服务】\n读笔记执行了。因为这次是本 App 自己启动的。\n"
                    + "请再用说明文档里的 adb 命令启动 SafeExportService，那一次应该被系统拒绝。\n\n"
                    + repository.dumpAll();
        }
        LabStore.put(this, LabStore.SERVICE_RESULT, result);
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
