# MyAndroidSecStudy

安卓安全学习用的练习工程和样本。之后的笔记、漏洞练习和配套 APK 都放在这个仓库里。

带 `gradlew` 的子目录是一个独立的 Android 工程，进入该目录即可编译。`apks/` 存放现成样本，这些文件不用编译。某个练习怎么用，看该目录自己的 README。

当前已有 `ComponentsVulns`（四大组件漏洞练习）和 `Demo`（入门示例）。

## 环境

1. 安装 **JDK**，并确认命令行里能运行 `java -version`。Gradle 需要的最低版本写在工程的 `gradle/wrapper/gradle-wrapper.properties`。若同目录还有 `gradle/gradle-daemon-jvm.properties`，其中的 `toolchainVersion` 是这次编译要使用的 JDK，首次运行可能自动下载。
2. 安装 **Android SDK**。需要的 Platform 版本看模块 `build.gradle.kts` 里的 `compileSdk`。模块含 `externalNativeBuild` 或 `src/main/cpp` 时，还要安装 **NDK** 和 **CMake**，版本以该模块的构建脚本为准。用 Android Studio 的 SDK Manager 安装这些组件。
3. 让 Gradle 找到 SDK。设置环境变量 `ANDROID_HOME` 指向 SDK 根目录，或在该工程目录新建 `local.properties`（已忽略，不要提交）：

   ```properties
   sdk.dir=C\:\\Users\\yourname\\AppData\\Local\\Android\\Sdk
   ```

   Windows 上 SDK 常在 `%LOCALAPPDATA%\Android\Sdk`。macOS 常在 `/Users/yourname/Library/Android/sdk`，Linux 常在 `/home/yourname/Android/Sdk`。路径按本机实际位置填写。
4. 首次编译需要能访问网络，以便下载 Gradle 发行版和 Maven 依赖。命令行若提示未接受 SDK 许可协议，在 SDK 的 `cmdline-tools` 里执行 `sdkmanager --licenses` 并同意。

## 用命令行编译

进入含有 `gradlew` 的工程目录。Windows 执行 `gradlew.bat`，macOS 和 Linux 执行 `./gradlew`。Linux 或 macOS 提示没有执行权限时，先运行 `chmod +x gradlew`。

编译该工程里的全部模块：

```bat
cd <工程目录>
gradlew.bat assembleDebug
```

```bash
cd <工程目录>
./gradlew assembleDebug
```

只编译一个模块时，模块名见该工程 `settings.gradle.kts` 里的 `include`：

```bat
gradlew.bat :<模块名>:assembleDebug
```

以 `ComponentsVulns` 为例，它的 `settings.gradle.kts` 包含 `:app`、`:imposter` 和 `:youku-poc`。只编译主应用：

```bat
cd ComponentsVulns
gradlew.bat :app:assembleDebug
```

调试 APK 在：

```text
<工程目录>/<模块目录>/build/outputs/apk/debug/<模块目录名>-debug.apk
```

上面这条命令的产物是 `ComponentsVulns/app/build/outputs/apk/debug/app-debug.apk`。

## 用 Android Studio 编译

用 Android Studio 打开含有 `gradlew` 的那个工程目录。等待 Gradle Sync 结束，缺少的 SDK、NDK 或 CMake 按提示安装。然后选择 **Build > Build APK(s)**，或在 Gradle 面板运行对应模块的 `assembleDebug`。

Android Studio 需要能加载该工程的 Android Gradle Plugin。插件版本在工程的 `gradle/libs.versions.toml` 里，键名是 `agp`。

## 安装到设备

设备打开 USB 调试后，把上一步得到的 APK 装上去：

```bat
adb install -r <apk路径>
```

`adb` 在 SDK 的 `platform-tools` 目录。`-r` 表示覆盖安装已有的同包名应用。
