package com.example.componentsvulns.data;

import android.content.Intent;

/**
 * Activity、Service、BroadcastReceiver 的拒绝服务示例共用这一段解析。
 * 三个组件各自在入口方法里直接调用 {@link #consume(Intent)}，没有 try/catch。
 */
public final class DosPayload {

    public static final String EXTRA_PAYLOAD = "payload";
    public static final String EXTRA_COUNT = "count";

    private DosPayload() {
    }

    /**
     * payload 缺失时，{@code String.length()} 抛出 NullPointerException。
     * count 的运行时类型是 String 时，{@code getIntExtra} 抛出 ClassCastException。
     */
    public static String consume(Intent intent) {
        if (intent == null) {
            throw new NullPointerException("Intent 为空");
        }
        if (intent.hasExtra(EXTRA_COUNT)) {
            int count = intent.getIntExtra(EXTRA_COUNT, 0);
            if (count < 0) {
                throw new IllegalArgumentException("count 为负数：" + count);
            }
        }
        String payload = intent.getStringExtra(EXTRA_PAYLOAD);
        return "payload.length=" + payload.length();
    }

    /** 实验页用来显示异常类型。有漏洞的组件不调用这个方法。 */
    public static String describeFailure(Intent intent) {
        try {
            return consume(intent);
        } catch (RuntimeException exception) {
            String message = exception.getMessage();
            if (message == null || message.isEmpty()) {
                return exception.getClass().getSimpleName();
            }
            return exception.getClass().getSimpleName() + "：" + message;
        }
    }
}
