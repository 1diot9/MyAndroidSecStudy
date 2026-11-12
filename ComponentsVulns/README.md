# 四大组件漏洞练习

这是一个故意配错组件的练习 App，数据库和文件里只有假笔记、假 PIN。请只安装在自己的模拟器或练习机上，学完卸载。不要上架，也不要拿这个包去调用别人的应用。

用 Android Studio 打开 `ComponentsVulns` 目录。需要同时安装两个调试包：

| 模块 | 包名 | 调试包 |
| --- | --- | --- |
| `:app` | `com.example.componentsvulns` | `app/build/outputs/apk/debug/app-debug.apk` |
| `:imposter` | `com.example.componentsvulns.imposter` | `imposter/build/outputs/apk/debug/imposter-debug.apk` |

`:app` 是被测应用，桌面名称是「组件漏洞演示」。`:imposter` 是仿冒端，只用于 Activity 劫持和 Service 劫持。首页四个按钮分别进入一种组件。每一章先是这个组件的基本用法，实验页顶部有对应按钮；后面的小节才是漏洞。笔记被清空之后，点首页的「恢复演示数据」。

下面先说明清单和 Intent，然后按组件分章。

## 清单和 Intent

组件写在 `app/src/main/AndroidManifest.xml`。系统收到启动页面、启动服务、发送广播或访问 `content://` 的请求时，先查清单，再决定是否把请求交给对应的类。实验页上的对照按钮由本应用自己调用，所以未导出的组件也会成功。要看系统拒绝其他调用方，用各节里的 adb 命令。那些命令的调用方是 adb shell。本工程 `targetSdk` 是 37。

Intent 可以包含组件类名、`action` 和 extras。写了类名的是显式 Intent。只写 `action` 的是隐式 Intent。extras 由构造 Intent 的一方写入。

清单里出现过的属性只在这里说明一次。

| 属性 | 作用 |
| --- | --- |
| `android:name` | 组件类名。以 `.` 开头时，前面补上应用的 namespace。`:app` 的 namespace 是 `com.example.componentsvulns` |
| `android:exported` | 其他应用能否按组件名调用。`true` 允许，`false` 拒绝。本应用调用自己的未导出组件仍然允许。从 Android 12 起，带 `intent-filter` 的组件必须写明它，否则装不上；更早的系统上，带 `intent-filter` 且不写时默认为 `true`。带 `MAIN` 和 `LAUNCHER` 的 Activity 是桌面入口，必须为 `true` |
| `android:label` | 系统显示的标题，例如最近任务和标题栏 |
| `intent-filter` | 声明这个组件能匹配哪些隐式 Intent。`action` 是动作名，`category` 是类别。Activity 要被隐式打开时，通常还要有 `android.intent.category.DEFAULT`。浏览器把链接交给应用时，还要有 `android.intent.category.BROWSABLE`，并且 action 是 `android.intent.action.VIEW` |
| `<data>` | 写在 `intent-filter` 里，限定能匹配的 URI。`android:scheme` 是协议，`android:host` 是主机，`android:path` 是整段路径，`android:pathPrefix` 是路径前缀，`android:pathPattern` 是路径通配。只写 scheme 时，该协议下的 URI 都能匹配；写了 scheme 和 host、没写 path 时，不限制路径 |
| `android:priority` | 写在 `intent-filter` 上。多个组件匹配同一 action 时，数值大的排在前面。不写时按 0 处理 |
| `android:permission` | 调用方必须持有这里写的权限，系统才把请求交给组件。权限本身用清单顶部的 `<permission>` 声明 |
| `android:process` | 组件所在进程。以 `:` 开头表示应用私有进程，崩溃时不影响主进程 |
| `android:launchMode` | 再次启动同一个 Activity 时是否新建实例。`standard` 每次新建；`singleTop` 已在栈顶则不新建；`singleTask` 一个任务栈只留一个实例；`singleInstance` 该实例单独占一个任务栈。不写时是 `standard` |
| `android:taskAffinity` | Activity 所属任务栈的 affinity。不写时等于包名。空字符串表示不使用包名作为 affinity |
| `android:allowTaskReparenting` | 为 `true` 时，affinity 相同的任务栈回到前台，系统可以把这个 Activity 移进那个任务栈 |
| `android:excludeFromRecents` | 为 `true` 时，这个 Activity 不单独出现在最近任务里 |
| `android:windowSoftInputMode` | 软键盘弹出时窗口如何调整。`adjustResize` 会缩小窗口，避免挡住输入框 |
| `android:usesCleartextTraffic` | 写在 `<application>` 上。为 `true` 时允许 WebView 和网络请求访问 `http://`。`targetSdk` 为 28 及以上时，不写这项则明文 HTTP 会被系统拒绝 |
| `android:authorities` | ContentProvider 的地址名，全设备不能重复，组成 `content://authority/路径` |
| `android:readPermission` | 读取该 Provider 所需的权限。不写时，导出的 Provider 不额外要求读权限 |
| `android:writePermission` | 写入该 Provider 所需的权限。不写时，导出的 Provider 不额外要求写权限 |
| `android:grantUriPermissions` | 为 `true` 时，本应用可以把某个 URI 的临时访问权授给其他应用。本工程没有写它 |
| `tools:ignore` | 只告诉 lint 不要报「组件被导出」。它不改变安装后的行为 |

清单顶部还有三项不是组件属性：

| 项 | 作用 |
| --- | --- |
| `<permission>` 的 `android:protectionLevel` | 定义自定义权限的保护级别。`signature` 表示只有与声明者签名相同的应用才能获得该权限 |
| `<uses-permission>` | 本应用申请某项权限。声明了 `signature` 权限之后，还要申请它，本应用自己才能调用受该权限保护的组件 |
| `<queries>` | 本应用要看见的其他包和 action。Android 11 及以后，没有这项就查询不到其他应用注册的对应组件 |

本练习的数据：

| 数据 | 位置 | 内容 |
| --- | --- | --- |
| 笔记表 `notes` | 应用私有数据库 | `secret=0` 是公开笔记，`secret=1` 是私密笔记 |
| 练习密码 | `LoginActivity` | `123456`，只写在密码页的按钮里 |
| 公开文件 | `files/demo/readme.txt` | 文件接口本来要提供的文件 |
| 旁边的文件 | `files/private/pin.txt` | 假 PIN `246810` |

adb 命令按 PowerShell 来写。含空格的参数整段放进单引号，让设备上的 shell 看到其中的双引号。

## Activity

目录：`app/src/main/java/com/example/componentsvulns/activity/`。

### 基本介绍

Activity 是带界面的组件。系统创建它时调用 `onCreate`，之后按顺序调用 `onStart`、`onResume`。页面被盖住时调用 `onPause`、`onStop`；回到前台时调用 `onRestart`、`onStart`、`onResume`；销毁时调用 `onDestroy`。实验页按钮「查看生命周期回调」打开 `LifecycleDemoActivity`，页面上按这个顺序追加回调名。

显式 Intent 写明目标类。调用方用 `putExtra` 放入字符串，目标页用 `getStringExtra` 读出：

```java
Intent intent = new Intent(this, EchoActivity.class);
intent.putExtra(EchoActivity.EXTRA_MESSAGE, "显式 Intent 写入的 message");
echoLauncher.launch(intent);
```

`EchoActivity` 读到这段文字后，把输入框里的回复放进 `setResult`，然后 `finish()`。调用方用 `registerForActivityResult` 接收。这是现在的写法。`startActivityForResult` / `onActivityResult` 是同一件事的旧接口，已经废弃，本练习不使用它。

隐式 Intent 不写类名，只写 action，并加上 `CATEGORY_DEFAULT`。系统在本应用的清单里找能匹配的 `intent-filter`：

```java
Intent intent = new Intent(EchoActivity.ACTION);
intent.addCategory(Intent.CATEGORY_DEFAULT);
intent.setPackage(getPackageName());
intent.putExtra(EchoActivity.EXTRA_MESSAGE, "隐式 Intent 写入的 message");
```

`EchoActivity` 的 `exported` 是 `false`，filter 里的 action 是 `com.example.componentsvulns.action.DEMO_ECHO`。`setPackage` 把匹配范围定在本应用。这个页面只回显调用方刚写入的字符串，不读取笔记。

`launchMode` 决定再次启动同一个类时，系统是新建实例，还是把已有实例拿到前台。四个子类共用 `LaunchModeActivity`，清单里分别写成：

| 类 | launchMode | 再次启动时 |
| --- | --- | --- |
| `StandardLaunchActivity` | `standard` | 新建实例，`onCreate` 再次执行，实例序号增加 |
| `SingleTopLaunchActivity` | `singleTop` | 已经在栈顶时不新建，执行 `onNewIntent` |
| `SingleTaskLaunchActivity` | `singleTask` | 本任务栈只保留一个实例，再次启动走 `onNewIntent` |
| `SingleInstanceLaunchActivity` | `singleInstance` | 该实例单独占用一个 `taskId` |

每个页面上的「再次启动本页面」都是指向当前类的显式 Intent。这些页面都不导出。

`DeepLinkActivity` 导出，filter 里用 `<data>` 限定链接。浏览器或 adb 打开 `componentsvulns://lab/open?id=1` 时，系统把整段 URI 放进 Intent 的 data，页面上显示 scheme、host、path 和 query。实验页没有对应按钮。

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="componentsvulns"
        android:host="lab"
        android:pathPrefix="/open" />
</intent-filter>
```

`pathPrefix="/open"` 要求路径以 `/open` 开头，所以 `componentsvulns://lab` 匹配不上，`componentsvulns://lab/open` 和 `componentsvulns://lab/open/extra` 可以。浏览器地址栏经常把自定义协议当成搜索；在地址栏输入下面这条 intent 链接，或在网页里放 `componentsvulns://lab/open?id=1` 的超链接。

```powershell
adb shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d "componentsvulns://lab/open?id=1"
```

浏览器地址栏：

```text
intent://lab/open?id=1#Intent;scheme=componentsvulns;package=com.example.componentsvulns;end
```

### 越权绕过

密码比较写在 `LoginActivity` 的按钮里。比较通过后才打开 `SecretNoteActivity`，并放入 `from_login=true`。`LoginActivity` 的 `exported` 是 `false`。

`SecretNoteActivity` 单独导出：

```xml
<activity
    android:name=".activity.SecretNoteActivity"
    android:exported="true" />
```

其他应用可以用显式 Intent 指定这个类。系统只创建它并调用 `onCreate`，不会先创建 `LoginActivity`。`from_login` 只改变横幅，随后总会执行 `dumpAll()`：

```java
boolean fromLogin = getIntent().getBooleanExtra(EXTRA_FROM_LOGIN, false);
if (fromLogin) {
    binding.banner.setText("你是通过密码页进来的。这是正常路径。");
} else {
    binding.banner.setText("你没有经过密码页。...");
}
binding.notes.setText(NotesRepository.get(this).dumpAll());
```

同一类问题的另一个入口是 `ImplicitSecretActivity`。它导出，并且用 `intent-filter` 声明 action `com.example.componentsvulns.action.OPEN_SECRET`。调用方只写这个 action 和 `CATEGORY_DEFAULT`，不写类名，系统也会打开它并显示全部笔记。

`SafeSecretActivity` 的 `exported` 是 `false`。本应用可以打开它。adb shell 打开它时，系统拒绝。

```powershell
adb shell am start -n com.example.componentsvulns/.activity.SecretNoteActivity
adb shell am start -a com.example.componentsvulns.action.OPEN_SECRET
adb shell am start -n com.example.componentsvulns/.activity.SafeSecretActivity
```

前两条会打开笔记页。第三条会被拒绝。

### Intent 重定向

这是本工程里的另一处 Activity 问题，和上一节的「跳过密码」不是同一次启动。

`SecretNoteActivity` 读取 extra `forward`。这个值的类型是 Intent。读到之后，本应用调用 `startActivity(forward)`。第二次启动的调用方是本应用，所以本应用里 `exported="false"` 的 `SafeSecretActivity` 也能被打开。

包名判断只比较 `forward` 的包名是不是 `com.example.componentsvulns`，挡不住本包里的未导出页面。实验页按钮「把未导出页面放进 forward」构造的就是这种嵌套 Intent。`am start` 不方便携带 Parcelable 类型的 extra，这个路径用实验页按钮看。

导出页面如果还要继续跳转，目标类应由本应用写死，不使用调用方传入的整份 Intent。

### Activity 劫持

`HijackableActivity` 的 `exported` 是 `false`，`launchMode` 是 `singleTask`，没有写 `taskAffinity`。未写 `taskAffinity` 时，任务栈的 affinity 是应用包名 `com.example.componentsvulns`。

仿冒端 `HijackActivity` 的清单是：

```xml
<activity
    android:name=".HijackActivity"
    android:allowTaskReparenting="true"
    android:excludeFromRecents="true"
    android:exported="false"
    android:taskAffinity="com.example.componentsvulns" />
```

`allowTaskReparenting="true"` 表示：当 affinity 相同的任务栈回到前台时，系统可以把这个页面移进那个任务栈。移进去之后，用户看到的是仿冒页面，目标页面的内容被盖住。`excludeFromRecents="true"` 使它不出现在最近任务里。

操作：

1. 安装 `:app` 和 `:imposter`。
2. 打开「组件漏洞仿冒端」，点「打开劫持页」。
3. 回到「组件漏洞演示」，打开「singleTask 且未固定 affinity 的页面」。

若系统执行了重归属，会先看到红底的仿冒页面。Android 10 之后对跨应用移动任务栈有额外限制，新版本上可能仍然只看到本应用的说明页。要检查的是目标页面的 `launchMode`，以及没有把 `taskAffinity` 设成空字符串。

对照是 `SafeTaskActivity`，清单里 `android:taskAffinity=""`。桌面入口 `MainActivity` 仍使用默认 affinity，否则启动图标无法按包名回到自己的任务栈。

### 拒绝服务

`DosActivity` 导出，进程是 `:dos`。`onCreate` 直接调用 `DosPayload.consume`，没有 try/catch。

```java
public static String consume(Intent intent) {
    if (intent == null) {
        throw new NullPointerException("Intent 为空");
    }
    if (intent.hasExtra(EXTRA_COUNT)) {
        int count = intent.getIntExtra(EXTRA_COUNT, 0);
    }
    String payload = intent.getStringExtra(EXTRA_PAYLOAD);
    return "payload.length=" + payload.length();
}
```

不传 `payload` 时，`length()` 抛出 `NullPointerException`。把 `count` 放成字符串 `"abc"` 时，`getIntExtra` 抛出 `ClassCastException`。异常没有被捕获，`:dos` 进程退出。主进程里的实验页还在。

实验页按钮「在本页捕获同样的异常」调用的是 `DosPayload.describeFailure`，只把异常类型显示出来，不启动 `DosActivity`。

```powershell
adb shell am start -n com.example.componentsvulns/.activity.DosActivity
adb shell am start -n com.example.componentsvulns/.activity.DosActivity --es count abc
```

## Service

目录：`app/src/main/java/com/example/componentsvulns/service/`。

### 基本介绍

Service 没有界面。本练习里有两种正常启动方式，组件都是 `exported="false"`。

启动式服务由 `startService` 开始，由 `stopService` 结束。系统调用 `onStartCommand`。`DemoStartedService` 返回 `START_NOT_STICKY`：进程被系统杀掉后，不会为了这条启动请求再把服务拉起来。不调用 `stopService` 的话，Activity 关闭后服务仍然存在。`onBind` 返回 `null`，因为这个类不提供绑定。

```java
startService(new Intent(this, DemoStartedService.class));
stopService(new Intent(this, DemoStartedService.class));
```

绑定式服务由 `bindService` 开始。`onBind` 返回 `Binder`，调用方在 `onServiceConnected` 里调用它的方法。`unbindService` 结束这次绑定。Activity 销毁时如果还绑着，也要先解绑。

```java
public class LocalBinder extends Binder {
    public String status() {
        return "onBind 返回了 LocalBinder。";
    }
}

demoBound = bindService(new Intent(this, DemoBoundService.class), connection, BIND_AUTO_CREATE);
```

`DemoBoundService` 和实验页在同一个进程，所以可以把 `IBinder` 转成 `LocalBinder` 直接调用。两种服务都用显式 Intent，类名写在 `Intent` 的构造方法里。只写 action、不写类名也不写包名的启动方式放在后面的「Service 劫持」里，不作为基本用法。

后面几个漏洞示例里的 `onBind` 返回 `null`，那些类只演示 `startService`。

### 权限提升

`VulnerableExportService` 导出，没有 `android:permission`。其他应用可以启动它，并在 extra `command` 里放入 `delete` 或其他字符串。

`delete` 会清空本应用的 `notes` 表，其他值会读出全部行。数据库属于本应用，这两次调用使用的是本应用对数据库的访问能力。调用方自己的应用里没有这张表。

`startService` 返回之后，调用方的那次调用已经结束。系统回调 `onStartCommand` 时，`Binder.getCallingUid()` 得到的是本进程，不能用来识别当初是哪个应用调用了 `startService`。限制调用方要写在清单里。

`SafeExportService` 的 Java 也会按 `command` 执行，但 `exported="false"`。adb shell 启动它时到不了 `onStartCommand`。

`SignatureProtectedService` 仍然 `exported="true"`，同时要求权限 `com.example.componentsvulns.permission.USE_PROTECTED_SERVICE`。这个权限的 `protectionLevel` 是 `signature`。本应用自己声明并申请了它，实验页按钮可以启动。adb shell 的签名不同，启动会被拒绝。

```powershell
adb shell am start-service -n com.example.componentsvulns/.service.VulnerableExportService --es command dump
adb shell am start-service -n com.example.componentsvulns/.service.VulnerableExportService --es command delete
adb shell am start-service -n com.example.componentsvulns/.service.SafeExportService --es command dump
adb shell am start-service -n com.example.componentsvulns/.service.SignatureProtectedService
```

第一条会读出笔记。第二条会清空笔记。后两条会被拒绝。执行过 `delete` 之后，回首页恢复演示数据。

### 消息伪造

`UpgradeService` 导出。它把 extra `download_url` 原样保存，并当作升级地址显示。`onStartCommand` 不调用 `UpgradeService.isAllowed`。

```java
public static boolean isAllowed(String url) {
    Uri uri = Uri.parse(url);
    return "https".equalsIgnoreCase(uri.getScheme())
            && ALLOWED_HOST.equalsIgnoreCase(uri.getHost());
}
```

`ALLOWED_HOST` 是 `updates.example.invalid`。实验页使用的地址是 `http://evil.example/update.apk`。按钮「把任意 URL 交给升级服务」会启动服务并记下这个地址。按钮「先检查 host 再决定是否启动」在启动前调用 `isAllowed`，这个地址会被拒绝，服务不会启动。

本练习只把 URL 显示出来，不会按它下载或安装包。

```powershell
adb shell am start-service -n com.example.componentsvulns/.service.UpgradeService --es download_url "http://evil.example/update.apk"
```

结果在 Service 实验页的「消息伪造」一栏。

### Service 劫持

`UpgradeService` 的 `intent-filter` 声明了 action `com.example.componentsvulns.action.APP_UPGRADE`，`priority` 是 0。仿冒端的 `ImposterUpgradeService` 声明同一个 action，`priority` 是 100，并且导出。

从 `targetSdk` 21 起，`startService` 的 Intent 必须带组件名或包名。实验页按钮「直接 startService 隐式 Intent」会收到 `IllegalArgumentException`。这是系统后来加上的限制。

有漏洞的写法是先 `queryIntentServices`，再把结果中的第一个设为组件并 `startService`：

```java
Intent intent = new Intent(UpgradeService.ACTION);
List<ResolveInfo> matches = getPackageManager().queryIntentServices(intent, 0);
ResolveInfo chosen = matches.get(0);
intent.setComponent(new ComponentName(
        chosen.serviceInfo.packageName, chosen.serviceInfo.name));
startService(intent);
```

匹配顺序按 `intent-filter` 的 `priority` 从高到低。仿冒端是 100，本应用是 0，所以安装仿冒端之后，第一个是 `com.example.componentsvulns.imposter`。`priority` 相同时，顺序还和系统的匹配规则有关，不能只靠安装先后保证结果。本练习把仿冒端的 priority 写高，是为了让实验结果稳定。

Android 11 及以后，应用默认看不到其他包注册的组件。`:app` 的清单用 `<queries>` 声明了这个 action 和仿冒端包名，查询才能看到它。

对照按钮在查询前调用 `intent.setPackage(getPackageName())`，结果里只剩本应用的 `UpgradeService`。

操作：安装两个包，在 Service 实验页点「查询 action 并启动第一个 Service」，再打开仿冒端。仿冒端首页会显示它收到的 `download_url`。

### 拒绝服务

`DosService` 导出，进程是 `:dos`。`onStartCommand` 直接调用 `DosPayload.consume`，没有 try/catch。空 extras 导致 `NullPointerException`，字符串类型的 `count` 导致 `ClassCastException`，`:dos` 进程退出。

```powershell
adb shell am start-service -n com.example.componentsvulns/.service.DosService
adb shell am start-service -n com.example.componentsvulns/.service.DosService --es count abc
```

实验页里的「在本页捕获同样的异常」不启动这个服务。

## BroadcastReceiver

目录：`app/src/main/java/com/example/componentsvulns/receiver/`。

### 基本介绍

BroadcastReceiver 在收到广播时执行 `onReceive`。发送方调用 `sendBroadcast` 或 `sendOrderedBroadcast`，系统再把 Intent 交给匹配的接收器。发送和接收不必在同一个生命周期里等待对方结束。

注册有两种：

- 静态注册：类写在清单里。`DemoStaticReceiver` 的 `exported` 是 `false`，没有 `intent-filter`。实验页用显式 Intent 指定这个类再发送，`onReceive` 读出 extra `text`。
- 动态注册：页面在 `onStart` 里调用 `registerReceiver`，在 `onStop` 里 `unregisterReceiver`。页面不在前台时，这个接收器不存在。Android 13 起，应用内使用的接收器传入 `RECEIVER_NOT_EXPORTED`。发送时再 `setPackage(getPackageName())`，系统只把广播交给本应用。

```java
Intent intent = new Intent(ACTION_DEMO);
intent.setPackage(getPackageName());
sendBroadcast(intent);
```

普通广播用 `sendBroadcast`，接收器之间没有固定的先后顺序。有序广播用 `sendOrderedBroadcast`。实验页为 `ACTION_ORDERED` 注册了两个动态接收器，`priority` 分别是 10 和 1。每个接收器读取 `getResultData()`，把自己的序号接上去，再 `setResultData`。priority 高的先执行。最后的结果接收器把整段文字显示在「基本用法」区域。

粘性广播 `sendStickyBroadcast` 已废弃，并且要系统签名才发得出。本练习不演示它。携带私密笔记、又不指定接收包名的发送方式放在后面的「敏感信息泄露」里。

### 敏感信息泄露

发送代码在 `ReceiverLabActivity.sendSecret`。action 是 `com.example.componentsvulns.action.SECRET_BROADCAST`，和下面命令接收器的 action 不同。

```java
Intent intent = new Intent(ACTION_LEAK);
intent.putExtra(EXTRA_SECRET, NotesRepository.get(this).dumpPrivate());
if (limited) {
    intent.setPackage(getPackageName());
}
sendBroadcast(intent);
```

`dumpPrivate()` 只包含 `secret=1` 的行。`limited` 为 false 时，Intent 没有组件名，也没有 `setPackage`。正在运行、并且监听这个 action 的其他应用可以收到 extra `secret`。

`limited` 为 true 时，`setPackage` 的参数是本应用包名。系统只把这则广播投递给这个包。

实验页自己动态注册了一个接收器，用来把 extra 显示在屏幕上。Android 13 起注册时要带 `RECEIVER_EXPORTED` 或 `RECEIVER_NOT_EXPORTED`。本页使用 `RECEIVER_EXPORTED`。同一应用发出的广播，两种标志都能收到。其他应用要收到未 `setPackage` 的这则广播，需要自己处于运行状态并监听同一 action。从 Android 8 起，这种未指定组件的自定义广播通常不会投递给其他应用清单里的接收器。

`setPackage` 限制发送时的接收范围。`RECEIVER_NOT_EXPORTED` 限制这个接收器能否接收其他应用发来的广播。

### 权限绕过

清单里的 `VulnerableCommandReceiver` 导出，action 是 `com.example.componentsvulns.action.NOTE_COMMAND`。`onReceive` 在本应用进程中执行。extra `command` 为 `delete` 时，`deleteAll()` 删除的是本应用的笔记。接收器没有检查发送方。

`SafeCommandReceiver` 的 `exported` 是 `false`，也没有 `intent-filter`。本应用用显式 Intent 发给它会执行。adb shell 发给它会被拒绝。

从 Android 8 起，只写 action、不指定组件的自定义广播，通常不会投递给清单里的接收器。外部要让 `VulnerableCommandReceiver` 执行，Intent 里要写上这个类名。

另一处是 `DynamicCommandReceiver`。它没有清单项，由 `App.onCreate` 动态注册。Android 13 之前，不带 flag 的 `registerReceiver` 默认可接收其他应用的广播。本工程在 Android 13 及以后传入 `RECEIVER_EXPORTED`，把这个行为保留下来。它同样在 `command=delete` 时清空笔记。进程退出后，这个接收器就不存在了。动态接收器没有清单组件名，外部用隐式广播即可，Android 8 对清单接收器的那条限制套不到它身上。

```powershell
adb shell am broadcast -n com.example.componentsvulns/.receiver.VulnerableCommandReceiver -a com.example.componentsvulns.action.NOTE_COMMAND --es command delete
adb shell am broadcast -n com.example.componentsvulns/.receiver.SafeCommandReceiver -a com.example.componentsvulns.action.NOTE_COMMAND --es command delete
adb shell am broadcast -a com.example.componentsvulns.action.DYNAMIC_COMMAND --es command ping
```

第一条会清空笔记。第二条会被拒绝。第三条要先打开应用，让进程完成动态注册；`ping` 不会删数据。把 `ping` 换成 `delete` 会清空笔记。

### 消息伪造

`ForgedMessageReceiver` 导出。它把 extra `title`、`body`、`url` 原样保存，并尝试打开 `ForgedWebActivity`。

`ForgedWebActivity` 未导出。它的 WebView 打开 JavaScript，允许 `file` 访问，并向页面暴露 `LabPin.pin()`。这个方法返回练习 PIN `246810`。加载哪个地址由广播里的 `url` 决定。实验页传入 `file:///android_asset/forged.html`，该页会调用 `LabPin.pin()` 并把结果显示出来。

清单申请了 `INTERNET`，`<application>` 上 `usesCleartextTraffic="true"`，所以 `http://` 和 `https://` 都可以被 WebView 打开。调用方可以改变 `url` 字符串，WebView 会按这个字符串调用 `loadUrl`。从后台广播里 `startActivity` 可能被系统拒绝；实验页按钮「按保存的 URL 打开 WebView」会再用保存的地址打开一次。

```powershell
adb shell am broadcast -n com.example.componentsvulns/.receiver.ForgedMessageReceiver -a com.example.componentsvulns.action.FORGED_MESSAGE --es title "账号安全提醒" --es body "调用方写入的正文" --es url "file:///android_asset/forged.html"
```

发送时如果应用在前台，页面可能会直接打开。否则回 Receiver 实验页，看「消息伪造」一栏，再点打开 WebView 的按钮。

### 拒绝服务

`DosReceiver` 导出，进程是 `:dos`，action 是 `com.example.componentsvulns.action.DOS`。`onReceive` 直接调用 `DosPayload.consume`。空 extras 或字符串类型的 `count` 会使 `:dos` 进程退出。

```powershell
adb shell am broadcast -n com.example.componentsvulns/.receiver.DosReceiver -a com.example.componentsvulns.action.DOS
adb shell am broadcast -n com.example.componentsvulns/.receiver.DosReceiver -a com.example.componentsvulns.action.DOS --es count abc
```

## ContentProvider

目录：`app/src/main/java/com/example/componentsvulns/provider/`。

### 基本介绍

ContentProvider 用 URI 向调用方提供数据。调用方不直接打开数据库文件，而是通过 `ContentResolver` 调用 `insert`、`query`、`update`、`delete`。这些方法的名字和 Provider 里的同名方法对应。实验页用 `getContentResolver()` 取得 `ContentResolver`。

`DemoNotesProvider` 不导出。authority 是 `com.example.componentsvulns.demonotes`。URI 分成三段：

```text
content://com.example.componentsvulns.demonotes/scratch
scheme     authority                              path
```

`content://` 表示这是一个 Provider 地址。数据放在 `scratch` 表，和后面漏洞示例使用的 `notes` 表分开。插入时把列值放进 `ContentValues`，查询使用 `db.query` 的参数列表，不把外部字符串拼进 SQL：

```java
ContentValues values = new ContentValues();
values.put("title", "练习条目");
Uri inserted = getContentResolver().insert(DemoNotesProvider.URI, values);
```

实验页的四个按钮依次是 insert、query、update、delete。update 把已有行的 `title` 改成「已修改」。这个 Provider 只供本应用调用。

其他应用使用同一套 `ContentResolver` 接口。后面的 `VulnerableNotesProvider` 把这套接口导出，并且在查询和写入时拼接 `selection`。它的两个地址是：

- 查询：`content://com.example.componentsvulns.vulnnotes/notes`
- 文件：`content://com.example.componentsvulns.vulnnotes/file?name=文件名`

`SafeNotesProvider` 的查询固定为 `secret=?`，绑定参数是 `"0"`，不使用调用方的 `selection`。`delete` 和 `update` 直接返回 0。读文件前用 `PathGuard` 检查最终路径。

### 信息泄露

不传 `selection` 时，`query` 拼出的 SQL 没有 `WHERE`，返回整张 `notes` 表，私密行也在里面。任意应用只要知道 authority 就可以查询。

```powershell
adb shell 'content query --uri content://com.example.componentsvulns.vulnnotes/notes'
adb shell 'content query --uri content://com.example.componentsvulns.safenotes/notes'
```

第一条返回全部行。第二条会被拒绝。

导出和后面的 SQL 拼接是两处设置。`exported="false"` 挡住其他应用。改成参数绑定之后，如果组件仍然导出且没有权限，其他应用仍能拿到这条固定查询返回的公开行。

### SQL 注入

`query` 把 `selection` 接在 `WHERE` 后面，再交给 `rawQuery`。绑定参数数组传入 `null`：

```java
public static String vulnerableNotesQuery(String selection) {
    String sql = "SELECT _id, title, body, secret FROM notes";
    if (selection != null && !selection.trim().isEmpty()) {
        sql = sql + " WHERE " + selection;
    }
    return sql + " ORDER BY _id ASC";
}
```

`selection` 为 `secret=0` 时，只选出公开行。`selection` 为 `secret=0 OR 1=1` 时，`OR 1=1` 使条件恒为真，私密行也会被选出。`rawQuery` 执行的是这一条 `SELECT`。

`delete` 和 `update` 使用同样的拼接：

```text
UPDATE notes SET body='已篡改' WHERE secret=1
DELETE FROM notes WHERE secret=0 OR 1=1
```

`SafeNotesProvider.query` 不把调用方的字符串放进 SQL 文本。`?` 对应的值放在绑定参数数组里。

```powershell
adb shell 'content query --uri content://com.example.componentsvulns.vulnnotes/notes --where "secret=0"'
adb shell 'content query --uri content://com.example.componentsvulns.vulnnotes/notes --where "secret=0 OR 1=1"'
adb shell 'content update --uri content://com.example.componentsvulns.vulnnotes/notes --where "secret=1"'
adb shell 'content delete --uri content://com.example.componentsvulns.vulnnotes/notes --where "secret=0 OR 1=1"'
```

`update` 会把私密行的正文改成「已篡改」。`delete` 会删掉全部行。之后回首页恢复演示数据。实验页把同一个删除条件交给 `SafeNotesProvider` 时，返回 0，表的内容不变。

### 目录遍历

`openFile` 从查询参数 `name` 取文件名。空值时用 `readme.txt`。然后：

```java
File target = new File(DemoFiles.demoDir(getContext()), name);
return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
```

`DemoFiles.demoDir` 是 `files/demo`。`name` 为 `readme.txt` 时，打开的是 `files/demo/readme.txt`。`name` 为 `../private/pin.txt` 时，解析后的路径是 `files/private/pin.txt`。这个文件仍在本应用的 `files` 目录里，本应用可以读自己的文件，所以调用方能得到假 PIN。

`PathGuard.requireInside` 先 `getCanonicalFile()`，再检查最终路径是不是仍以 `files/demo/` 为前缀。不是就抛出 `SecurityException`。`SafeNotesProvider.openFile` 在打开文件前调用它。

本应用没有外部存储权限。最终路径一旦离开本应用的数据目录，系统会拒绝打开。同样的 `new File(目录, 调用方给出的名字)` 如果写在持有更多文件权限的应用里，解析后能到达的路径更多。

```powershell
adb shell 'content read --uri "content://com.example.componentsvulns.vulnnotes/file?name=readme.txt"'
adb shell 'content read --uri "content://com.example.componentsvulns.vulnnotes/file?name=../private/pin.txt"'
adb shell 'content read --uri "content://com.example.componentsvulns.safenotes/file?name=../private/pin.txt"'
```

第一条读到 demo 说明。第二条读到 `pin.txt`。第三条的 Provider 未导出，adb shell 会被拒绝，`openFile` 不会执行。实验页把同一个文件名交给 `SafeNotesProvider` 时，调用方是本应用，会进入 `openFile`，然后被 `PathGuard` 拒绝。

## 源文件

| 组件 | 内容 | 代码 | 说明 |
| --- | --- | --- | --- |
| Activity | 基本用法 | `EchoActivity`，`LifecycleDemoActivity`，`LaunchModeActivity` 的四个子类，`DeepLinkActivity` | 显式/隐式 Intent、返回值、生命周期、launchMode、带 data 的浏览器 Deep Link |
| Service | 基本用法 | `DemoStartedService`，`DemoBoundService` | `startService` / `stopService`，`bindService` / `unbindService` |
| BroadcastReceiver | 基本用法 | `DemoStaticReceiver`，`ReceiverLabActivity` 里的动态接收器和有序广播 | 静态注册、动态注册、`sendBroadcast`、`sendOrderedBroadcast` |
| ContentProvider | 基本用法 | `DemoNotesProvider` | 不导出的 `insert` / `query` / `update` / `delete` |

| 组件 | 漏洞 | 有漏洞的实现 | 对照 |
| --- | --- | --- | --- |
| Activity | 越权绕过 | `SecretNoteActivity`，`ImplicitSecretActivity` | `SafeSecretActivity` |
| Activity | Intent 重定向 | `SecretNoteActivity.openForwardIfPresent` | 不启动调用方传入的 Intent |
| Activity | 劫持 | `HijackableActivity`；仿冒端 `HijackActivity` | `SafeTaskActivity` 的 `taskAffinity=""` |
| Activity | 拒绝服务 | `DosActivity`，`DosPayload.consume` | 实验页里的 `describeFailure` |
| Service | 权限提升 | `VulnerableExportService` | `SafeExportService`，`SignatureProtectedService` |
| Service | 消息伪造 | `UpgradeService.onStartCommand` | `UpgradeService.isAllowed` |
| Service | 劫持 | `ServiceLabActivity.startFirstMatch(false)`；仿冒端 `ImposterUpgradeService` | `setPackage` 之后再启动 |
| Service | 拒绝服务 | `DosService` | 实验页里的 `describeFailure` |
| BroadcastReceiver | 敏感信息泄露 | `ReceiverLabActivity.sendSecret(false)` | 同方法里的 `setPackage` |
| BroadcastReceiver | 权限绕过 | `VulnerableCommandReceiver`，`DynamicCommandReceiver` | `SafeCommandReceiver` |
| BroadcastReceiver | 消息伪造 | `ForgedMessageReceiver`，`ForgedWebActivity` | 不把调用方的 URL 交给 WebView |
| BroadcastReceiver | 拒绝服务 | `DosReceiver` | 实验页里的 `describeFailure` |
| ContentProvider | 信息泄露 | 导出的 `VulnerableNotesProvider.query` | `SafeNotesProvider` 不导出 |
| ContentProvider | SQL 注入 | `SqlTexts` 的 query、update、delete | `SafeNotesProvider` 的参数绑定，写入方法返回 0 |
| ContentProvider | 目录遍历 | `VulnerableNotesProvider.openFile` | `PathGuard` |

单元测试 `SqlTextsTest` 和 `PathGuardTest` 不启动模拟器。
