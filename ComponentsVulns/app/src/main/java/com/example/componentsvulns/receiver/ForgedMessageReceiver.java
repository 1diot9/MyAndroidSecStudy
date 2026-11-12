package com.example.componentsvulns.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.componentsvulns.data.LabStore;

/**
 * 消息伪造：导出的接收器把 title、body、url 原样收下。
 * url 会交给 ForgedWebActivity 里启用了 JavaScript 的 WebView。
 */
public class ForgedMessageReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.example.componentsvulns.action.FORGED_MESSAGE";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_BODY = "body";
    public static final String EXTRA_URL = "url";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(EXTRA_TITLE);
        String body = intent.getStringExtra(EXTRA_BODY);
        String url = intent.getStringExtra(EXTRA_URL);
        LabStore.put(context, LabStore.FORGED_URL, url == null ? "" : url);
        StringBuilder result = new StringBuilder();
        result.append("【导出的接收器】未校验消息内容。\n标题：").append(title);
        result.append("\n正文：").append(body);
        result.append("\nURL：").append(url);
        Intent page = new Intent(context, ForgedWebActivity.class);
        page.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        page.putExtra(EXTRA_TITLE, title);
        page.putExtra(EXTRA_BODY, body);
        page.putExtra(EXTRA_URL, url);
        try {
            context.startActivity(page);
            result.append("\n已打开 ForgedWebActivity。");
        } catch (RuntimeException exception) {
            result.append("\n当前不能从广播直接打开页面：")
                    .append(exception.getClass().getSimpleName());
            String message = exception.getMessage();
            if (message != null && !message.isEmpty()) {
                result.append("：").append(message);
            }
            result.append("\n实验页里的按钮仍会按保存的 URL 打开 WebView。");
        }
        LabStore.put(context, LabStore.FORGED_RESULT, result.toString());
    }
}
