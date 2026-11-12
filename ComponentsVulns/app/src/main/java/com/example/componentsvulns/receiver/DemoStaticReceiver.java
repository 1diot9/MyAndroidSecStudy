package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.componentsvulns.data.LabStore;

/** 基本用法：写在清单里的接收器。exported="false"，只接受本应用的显式广播。 */
public class DemoStaticReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String text = intent.getStringExtra("text");
        LabStore.put(context, LabStore.DEMO_RECEIVER,
                "清单接收器 onReceive。\n这次广播写了 " + DemoStaticReceiver.class.getSimpleName() + "。\nextra text=" + text);
    }
}
