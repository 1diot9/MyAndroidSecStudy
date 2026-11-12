package com.example.componentsvulns;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.example.componentsvulns.data.DemoFiles;
import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.receiver.DynamicCommandReceiver;

/**
 * 进程启动时准备好练习用的假数据。
 * ContentProvider 可能比 Application 更早初始化，所以 Provider 里也会再调用一次同样的准备方法。
 */
public class App extends Application {

    /** Logcat 里用这个词过滤，就能只看本练习打出来的日志。 */
    public static final String TAG = "ComponentsVulns";

    private final DynamicCommandReceiver dynamicCommandReceiver = new DynamicCommandReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        DemoFiles.writeSamples(this);
        NotesRepository.get(this).ensureCreated();
        registerDynamicCommandReceiver();
        Log.i(TAG, "练习数据已准备好");
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerDynamicCommandReceiver() {
        IntentFilter filter = new IntentFilter(DynamicCommandReceiver.ACTION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(dynamicCommandReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(dynamicCommandReceiver, filter);
        }
    }
}
