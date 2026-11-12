package com.example.componentsvulns.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;

/**
 * 导出的启动式 Service，且没有 android:permission。
 * 其他应用可以 startService，并在 extra command 里传入 delete 或其他字符串。
 * onStartCommand 在本应用进程中执行，读写的是本应用的 notes 表。
 *
 * startService 返回后，调用方的那次调用已经结束。系统回调 onStartCommand 时，
 * Binder.getCallingUid() 得到的是本进程，不能用来识别当初的调用方。
 * 限制调用方要写在清单里：exported="false"，或 android:permission。
 * 对照是 SafeExportService，Java 仍会按 command 执行，差别在清单。
 */
public class VulnerableExportService extends Service {

    public static final String EXTRA_COMMAND = "command";

    @Override
    public IBinder onBind(Intent intent) {
        // 本课演示的是启动式 Service。
        // 绑定式 Service 还会在 onBind 里把接口交出去，导出之后别人可以直接调用里面的方法，问题是同一类。
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent == null ? null : intent.getStringExtra(EXTRA_COMMAND);
        Log.i(App.TAG, "导出的 Service 被启动，command=" + command);
        NotesRepository repository = NotesRepository.get(this);
        String result;
        if ("delete".equals(command)) {
            repository.deleteAll();
            result = "【导出的服务】\n执行了清空。没有校验调用者身份。\n笔记已经删掉，回首页可以恢复演示数据。";
        } else if ("dump".equals(command)){
            result = "【导出的服务】\n读出了全部笔记。没有校验调用者身份。\n\n" + repository.dumpAll();
        } else {
            result = "【导出的服务】\n传入了非标准命令或空命令：" + command;
        }
        Log.d("VulnerableExportService", result);
        LabStore.put(this, LabStore.SERVICE_RESULT, result);
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
