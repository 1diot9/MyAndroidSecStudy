package com.example.componentsvulns.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 把 Service、Receiver 的执行结果存下来。
 * 它们没有自己的页面，实验页通过这里把结果显示出来。
 */
public final class LabStore {

    public static final String DEMO_SERVICE = "demo_service";
    public static final String DEMO_RECEIVER = "demo_receiver";
    public static final String SERVICE_RESULT = "service_result";
    public static final String PROTECTED_RESULT = "protected_result";
    public static final String UPGRADE_RESULT = "upgrade_result";
    public static final String HIJACK_RESULT = "hijack_result";
    public static final String DOS_RESULT = "dos_result";
    public static final String RECEIVER_RESULT = "receiver_result";
    public static final String FORGED_RESULT = "forged_result";
    public static final String FORGED_URL = "forged_url";
    private static final String FILE = "lab_store";
    private static final String EMPTY = "还没有结果。先点本页按钮，或按说明文档里的 adb 命令操作后，再回到本页。";

    private LabStore() {
    }

    public static void put(Context context, String key, String value) {
        preferences(context).edit().putString(key, value).apply();
    }

    public static String get(Context context, String key) {
        return get(context, key, EMPTY);
    }

    public static String get(Context context, String key, String defaultValue) {
        return preferences(context).getString(key, defaultValue);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
