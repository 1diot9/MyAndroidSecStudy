package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.componentsvulns.data.DosPayload;

/** 拒绝服务：导出的 Receiver 在 onReceive 里直接解析 extras。进程是 :dos。 */
public class DosReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.example.componentsvulns.action.DOS";

    @Override
    public void onReceive(Context context, Intent intent) {
        DosPayload.consume(intent);
    }
}
