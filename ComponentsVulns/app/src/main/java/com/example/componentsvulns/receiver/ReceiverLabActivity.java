package com.example.componentsvulns.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.componentsvulns.data.DosPayload;
import com.example.componentsvulns.data.LabStore;
import com.example.componentsvulns.data.NotesRepository;
import com.example.componentsvulns.data.ResultEcho;
import com.example.componentsvulns.databinding.ActivityReceiverLabBinding;

/** 广播实验页。按钮按权限绕过、敏感信息泄露、消息伪造、拒绝服务分组。 */
public class ReceiverLabActivity extends AppCompatActivity {

    /** 用来演示“广播内容被别人听到”。不要和命令接收器的 action 混用。 */
    public static final String ACTION_LEAK = "com.example.componentsvulns.action.SECRET_BROADCAST";
    public static final String ACTION_DEMO = "com.example.componentsvulns.action.DEMO_PING";
    public static final String ACTION_ORDERED = "com.example.componentsvulns.action.DEMO_ORDERED";
    private static final String EXTRA_SECRET = "secret";

    private ActivityReceiverLabBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** 本页自己注册的接收器，用来把收到的广播内容显示出来。 */
    private final BroadcastReceiver leakCatcher = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String secret = intent.getStringExtra(EXTRA_SECRET);
            String limit = intent.getPackage();
            String explain;
            if (limit == null) {
                explain = "这条广播没有 setPackage。本页能收到，是因为上面动态注册了同一个 action。\n"
                        + "别的正在运行、并且也监听了这个 action 的应用，可以收到同样的内容。";
            } else {
                explain = "这条广播写了 setPackage=" + limit + "。\n"
                        + "只有这个包名能收到。其他应用即使监听了同一个 action，也收不到。";
            }
            ResultEcho.show(binding.leakResult, explain + "\n\n广播里的内容：\n" + secret);
        }
    };

    /** 基本用法：只接收本应用发出的广播。 */
    private final BroadcastReceiver demoCatcher = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ResultEcho.show(binding.basicResult, "动态接收器 onReceive。\n广播调用了 setPackage，参数是本应用包名。\nsetPackage="
                    + intent.getPackage());
        }
    };

    private final BroadcastReceiver orderedHigh = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String soFar = getResultData() == null ? "" : getResultData();
            setResultData(soFar + "1. priority=10\n");
        }
    };

    private final BroadcastReceiver orderedLow = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String soFar = getResultData() == null ? "" : getResultData();
            setResultData(soFar + "2. priority=1\n");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverLabBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("广播 Receiver");

        binding.basicStatic.setOnClickListener(v -> {
            Intent intent = new Intent(this, DemoStaticReceiver.class);
            intent.putExtra("text", "来自实验页的显式广播");
            sendBroadcast(intent);
            handler.postDelayed(() -> {
                if (!isDestroyed()) {
                    ResultEcho.show(binding.basicResult, LabStore.get(this, LabStore.DEMO_RECEIVER));
                }
            }, 300);
        });
        binding.basicDynamic.setOnClickListener(v -> {
            Intent intent = new Intent(ACTION_DEMO);
            intent.setPackage(getPackageName());
            sendBroadcast(intent);
        });
        binding.basicOrdered.setOnClickListener(v -> sendOrderedBroadcast(
                new Intent(ACTION_ORDERED),
                null,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        ResultEcho.show(binding.basicResult, "有序广播按 priority 从高到低执行：\n" + getResultData());
                    }
                },
                null,
                RESULT_OK,
                null,
                null));

        binding.deleteExported.setOnClickListener(v -> sendCommand(VulnerableCommandReceiver.class, "delete"));
        binding.pingExported.setOnClickListener(v -> sendCommand(VulnerableCommandReceiver.class, "ping"));
        binding.deleteSafe.setOnClickListener(v -> sendCommand(SafeCommandReceiver.class, "delete"));
        binding.pingDynamic.setOnClickListener(v -> sendDynamic("ping"));
        binding.deleteDynamic.setOnClickListener(v -> sendDynamic("delete"));
        binding.leak.setOnClickListener(v -> sendSecret(false));
        binding.leakSafe.setOnClickListener(v -> sendSecret(true));
        binding.forgeMessage.setOnClickListener(v -> sendForged());
        binding.openForged.setOnClickListener(v -> openForged());
        binding.dosEmpty.setOnClickListener(v -> sendDos(false));
        binding.dosType.setOnClickListener(v -> sendDos(true));
        binding.dosCatch.setOnClickListener(v -> {
            Intent empty = new Intent();
            Intent typed = new Intent();
            typed.putExtra(DosPayload.EXTRA_COUNT, "abc");
            ResultEcho.show(binding.dosResult, "空 extras：\n" + DosPayload.describeFailure(empty)
                    + "\n\ncount 为字符串：\n" + DosPayload.describeFailure(typed)
                    + "\n\n上面是本页捕获的结果。前两个按钮会在 :dos 进程里直接调用 DosPayload.consume。");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_LEAK);
        registerLeakCatcher(filter);
        registerNotExported(demoCatcher, new IntentFilter(ACTION_DEMO));
        IntentFilter high = new IntentFilter(ACTION_ORDERED);
        high.setPriority(10);
        IntentFilter low = new IntentFilter(ACTION_ORDERED);
        low.setPriority(1);
        registerNotExported(orderedHigh, high);
        registerNotExported(orderedLow, low);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showCommandResult();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(leakCatcher);
        unregisterReceiver(demoCatcher);
        unregisterReceiver(orderedHigh);
        unregisterReceiver(orderedLow);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * 显式广播：Intent 里写了具体的接收器类。
     * Android 8 之后，只写 action、不写组件的自定义广播，通常不会再交给清单里的接收器。
     * 所以这里指定组件，外部用 adb 时也同样指定组件。
     */
    private void sendCommand(Class<?> receiverClass, String command) {
        Intent intent = new Intent(this, receiverClass);
        intent.setAction(VulnerableCommandReceiver.ACTION);
        intent.putExtra(VulnerableCommandReceiver.EXTRA_COMMAND, command);
        sendBroadcast(intent);
        // 接收器不一定在 sendBroadcast 返回前就执行完，稍后再读结果。
        handler.postDelayed(this::showCommandResult, 300);
    }

    private void showCommandResult() {
        if (!isDestroyed()) {
            ResultEcho.show(binding.commandResult, LabStore.get(this, LabStore.RECEIVER_RESULT));
            ResultEcho.show(binding.forgedResult, LabStore.get(this, LabStore.FORGED_RESULT));
        }
    }

    /** 只写 action，不写组件名。动态注册的接收器可以收到这种广播。 */
    private void sendDynamic(String command) {
        Intent intent = new Intent(DynamicCommandReceiver.ACTION);
        intent.putExtra(VulnerableCommandReceiver.EXTRA_COMMAND, command);
        sendBroadcast(intent);
        handler.postDelayed(this::showCommandResult, 300);
    }

    private void sendForged() {
        Intent intent = new Intent(this, ForgedMessageReceiver.class);
        intent.setAction(ForgedMessageReceiver.ACTION);
        intent.putExtra(ForgedMessageReceiver.EXTRA_TITLE, "账号安全提醒");
        intent.putExtra(ForgedMessageReceiver.EXTRA_BODY, "这是调用方写进 Intent 的正文，接收器没有校验来源。");
        intent.putExtra(ForgedMessageReceiver.EXTRA_URL, "file:///android_asset/forged.html");
        sendBroadcast(intent);
        handler.postDelayed(this::showCommandResult, 300);
    }

    private void openForged() {
        Intent intent = new Intent(this, ForgedWebActivity.class);
        intent.putExtra(ForgedMessageReceiver.EXTRA_TITLE, "手动打开上一条 URL");
        intent.putExtra(ForgedMessageReceiver.EXTRA_BODY, "WebView 会加载接收器保存的 url，并暴露 LabPin.pin()。");
        intent.putExtra(ForgedMessageReceiver.EXTRA_URL, LabStore.get(this, LabStore.FORGED_URL, ""));
        startActivity(intent);
    }

    private void sendDos(boolean wrongType) {
        Intent intent = new Intent(this, DosReceiver.class);
        intent.setAction(DosReceiver.ACTION);
        if (wrongType) {
            intent.putExtra(DosPayload.EXTRA_COUNT, "abc");
        }
        sendBroadcast(intent);
    }

    /**
     * 发送一条 action 为 ACTION_LEAK 的广播，extra secret 是私密笔记。
     * limited 为 false 时不调用 setPackage，正在监听该 action 的其他应用可以收到 extras。
     * limited 为 true 时 setPackage 为本应用包名，系统只投递给这个包。
     * 这与 VulnerableCommandReceiver 是两处代码，action 也不相同。
     */
    private void sendSecret(boolean limited) {
        Intent intent = new Intent(ACTION_LEAK);
        intent.putExtra(EXTRA_SECRET, NotesRepository.get(this).dumpPrivate());
        if (limited) {
            intent.setPackage(getPackageName());
        }
        sendBroadcast(intent);
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerNotExported(BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLeakCatcher(IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33 起必须声明是否导出。本页用来显示自己刚发出的 extras。
            // 同一应用发出的广播，RECEIVER_NOT_EXPORTED 也能收到。
            // 其他应用要收到未 setPackage 的广播，需要自己在运行并监听同一 action。
            registerReceiver(leakCatcher, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(leakCatcher, filter);
        }
    }
}
