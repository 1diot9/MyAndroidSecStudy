package com.example.componentsvulns.data;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 准备两个文本文件，用来观察“文件接口有没有把目录限制住”。
 * demo/readme.txt 是这个接口本来打算提供的文件。
 * private/pin.txt 放在旁边，正常的文件接口不应该读到它。
 */
public final class DemoFiles {

    private DemoFiles() {
    }

    public static File demoDir(Context context) {
        return new File(context.getFilesDir(), "demo");
    }

    public static File privateDir(Context context) {
        return new File(context.getFilesDir(), "private");
    }

    public static void writeSamples(Context context) {
        try {
            write(new File(demoDir(context), "readme.txt"),
                    "你正在读取 demo/readme.txt。\n"
                            + "这是文件接口本来打算提供的公开文件。\n");
            write(new File(privateDir(context), "pin.txt"),
                    "你正在读取 private/pin.txt。\n"
                            + "演示 PIN：246810\n"
                            + "请求的 name 是 ../private/pin.txt。解析后的路径在 files/private，不在 files/demo。\n");
        } catch (IOException exception) {
            throw new IllegalStateException("写练习文件失败", exception);
        }
    }

    private static void write(File file, String text) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建目录 " + parent);
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }
}
