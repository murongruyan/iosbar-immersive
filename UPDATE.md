# 更新日志

## 0.4.1 (2026-08-20)

- 修复旧版 overlay 包仅删除文件、未清理 PackageManager 注册，导致 SystemUI 读取错误导航栏资源并触发 LSPosed 安全模式的问题。
- 安装/卸载时仅清理 `/data` 下的旧 overlay 包，跳过系统分区原厂包，避免误伤系统手势资源。
- 安全模式恢复说明：切回手势导航后再刷入本版，避免三键导航状态被误认为模块功能。

## 0.4.0 (2026-08-20)

- 移除 framework/SystemUI RRO 和 metamodule 挂载，改为只 Hook `com.android.systemui`。
- 保留手势 provider，仅将应用布局的导航栏 inset 设为 0。
- 关闭瞬时导航栏 scrim，修复横屏右下角定位、透明框和整屏蒙层。
- 横条几何在 attach、layout、方向切换和绘制阶段重新应用。
- 升级安装时清理 0.3.x 旧 overlay、脚本及 `disable` 标记。

## 0.3.3

- 旧版 RRO/metamodule 方案，已废弃。不要在 Android 16 上继续使用。
