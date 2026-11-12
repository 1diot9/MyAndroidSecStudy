package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;

/**
 * 【对照】清单里 exported="false"，也没有 intent-filter。
 * 本 App 用显式 Intent 发给它会成功。外部应用和 adb shell 发给它会被系统拒绝。
 */
public class SafeCommandReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String command = intent.getStringExtra(VulnerableCommandReceiver.EXTRA_COMMAND);
        Log.i(App.TAG, "未导出的 Receiver 收到 command=" + command);
        String result;
        if ("delete".equals(command)) {
            NotesRepository.get(context).deleteAll();
            result = "【未导出的接收器】\n清空执行了。因为这次是本 App 自己发出的显式广播。\n"
                    + "请再用说明文档里的 adb 命令发给 SafeCommandReceiver，那一次应该被系统拒绝。";
        } else {
            result = "【未导出的接收器】\n收到指令：" + command
                    + "\n这次能收到，是因为发送代码在本 App 内部。";
        }
        LabStore.put(context, LabStore.RECEIVER_RESULT, result);
    }
}
