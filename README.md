# iOS-style immersive gesture bar

这是面向 Android 16/realme UI 的 LSPosed + KernelSU 模块。它只向
`com.android.systemui` 注入 Hook，不替换 framework、SystemUI 或导航模式 RRO，
因此不会改动显示模块使用的 framework 资源映射。

## 功能

- 保留系统手势导航和手势触摸区域，只移除应用布局收到的 `navigationBars` inset。
- 关闭 SystemUI 瞬时导航栏的半透明 scrim，避免上划后整屏蒙层。
- 调整 Oplus 手势横条的宽度、高度、圆角和底部距离，并在旋转/布局变化后重新应用。
- 横屏窗口固定为底部水平居中，避免横条跑到右下角或带大块透明窗口。

## 安装

1. 确认设备已安装 LSPosed API 102，并仅给模块启用 `com.android.systemui` 作用域。
2. 在 KernelSU/Magisk 的模块安装入口刷入 `iosbar-immersive-v*.zip`。
   该 ZIP 是标准 recovery-flashable 模块包，也可以在 recovery 环境通过
   `adb sideload` 或从 `/sdcard` 选择安装；不要解压后手动复制到 `/system`。
3. 重启一次，让 LSPosed 清除之前的安全模式状态并重新加载 SystemUI。

模块升级时会主动清理 0.3.x 遗留的 `system/`、`overlay-src/`、metamodule 脚本和
`disable` 标记，并卸载旧版三个 overlay 包在 `/data` 下的副本。清理逻辑明确跳过
`/system`、`/product`、`/system_ext`、`/vendor` 的原厂包，不会禁用系统自带手势资源。
不要继续刷旧版 0.3.2/0.3.3 ZIP。

如果已经进入 LSPosed 安全模式：先在 KernelSU/Magisk 的安全模式入口禁用旧横条模块，
启动后选择系统手势导航，再刷入本版并重启。安全模式的三键导航是 LSPosed 的保护状态，
不是本模块主动切换的导航模式。

## 回滚

在 KernelSU/Magisk 中禁用或卸载本模块，然后重启。卸载脚本会移除
`com.iosbar.navhook` Hook APK 以及本模块目录内的旧载荷；它不会删除系统原厂
`NavigationBarModeGestural` overlay。若系统仍显示三键，进入系统导航设置重新选择
“手势导航”，并再次重启。

## 兼容范围

当前针对 realme GT8 Pro（RMX5200）、Android 16、LSPosed API 102 验证。其他 Oplus
版本如果没有 `OplusNavigationHandle` 或 `NavigationBarTransitions` 对应方法，Hook
会失败关闭，不会安装 RRO。首次刷入后应检查 LSPosed 日志中出现：

```text
installed NavigationBar inset hook methods=1
installed NavigationBarTransitions background hook methods=1
```

## 构建

本地需要 JDK 17、Android SDK 36 和 Gradle 9.5.1。设置 `ANDROID_HOME`（或
`ANDROID_SDK_ROOT`）后执行：

```powershell
.\build_module.ps1 -OutputDirectory .\dist
```

输出 ZIP 包含标准 recovery 元数据、`module.prop`、安装/卸载脚本和
`runtime/iosbar-navhook.apk`。

## 开源协议

本项目以 GPL-3.0-only 发布，完整条款见 [LICENSE](LICENSE)。提交到 `main` 或
推送 `v*` 标签会触发 `.github/workflows/build.yml`；标签构建会运行静态检查并将
标准 ZIP 和当前版本更新日志发布到 GitHub Release。

## 打赏

如果好用，请支持我！

![收款码](222.png)
