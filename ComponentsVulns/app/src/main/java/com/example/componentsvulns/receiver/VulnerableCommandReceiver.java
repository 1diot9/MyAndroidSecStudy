package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.componentsvulns.App;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;

/**
 * 导出的清单接收器。发送方用显式 Intent 指定本类后，onReceive 在本应用进程中执行。
 * extra command 为 delete 时，deleteAll() 删除的是本应用的笔记。
 * 对照是 SafeCommandReceiver：exported="false"，外部显式广播到不了 onReceive。
 * Android 8 起，只写 action、不写本类的自定义广播，通常不会投递给这个清单接收器。
 */
public class VulnerableCommandReceiver extends BroadcastReceiver {

    /** 清单 intent-filter 里的 action 必须和这个字符串完全一致。 */
    public static final String ACTION = "com.example.componentsvulns.action.NOTE_COMMAND";
    public static final String EXTRA_COMMAND = "command";

    @Override
    public void onReceive(Context context, Intent intent) {
        String command = intent.getStringExtra(EXTRA_COMMAND);
        Log.i(App.TAG, "导出的 Receiver 收到 command=" + command);
        NotesRepository repository = NotesRepository.get(context);
        String result;
        if ("delete".equals(command)) {
            repository.deleteAll();
            result = "【导出的接收器】\n收到 delete，笔记已清空。没有检查发送方是谁。\n回首页可以恢复演示数据。";
        } else {
            result = "【导出的接收器】\n收到指令：" + command + "\n没有执行删除。发送方同样没有被检查。";
        }
        LabStore.put(context, LabStore.RECEIVER_RESULT, result);
    }
}
