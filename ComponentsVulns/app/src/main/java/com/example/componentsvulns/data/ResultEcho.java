package com.example.componentsvulns.data;

import android.util.Log;
import android.widget.TextView;

import com.example.componentsvulns.App;

/** 把实验结果显示到界面上，并同步写入 Logcat。过滤词是 ComponentsVulns。 */
public final class ResultEcho {

    private ResultEcho() {
    }

    public static void show(TextView view, CharSequence text) {
        String message = text == null ? "" : text.toString();
        view.setText(message);
        Log.d(App.TAG, message);
    }
}
