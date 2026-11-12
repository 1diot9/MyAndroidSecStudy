package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;

/**
 * 权限绕过：在代码里动态注册，没有清单项。
 * Android 13 之前，不带 flag 的 registerReceiver 默认可接收其他应用的广播。
 * 本工程在 Android 13 及以后显式传入 RECEIVER_EXPORTED，用来保留这个可被外部调用的行为。
 * 进程退出后，这个接收器就不在了。
 */
public class DynamicCommandReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.example.componentsvulns.action.DYNAMIC_COMMAND";

    @Override
    public void onReceive(Context context, Intent intent) {
        String command = intent.getStringExtra(VulnerableCommandReceiver.EXTRA_COMMAND);
        Log.i(App.TAG, "动态 Receiver 收到 command=" + command);
        String result;
        if ("delete".equals(command)) {
            NotesRepository.get(context).deleteAll();
            result = "【动态注册的接收器】\n收到 delete，笔记已清空。注册时允许接收外部广播，没有再校验发送方。";
        } else {
            result = "【动态注册的接收器】\n收到指令：" + command + "\n没有执行删除。发送方没有被检查。";
        }
        LabStore.put(context, LabStore.RECEIVER_RESULT, result);
    }
}
